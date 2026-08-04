package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.BomRequest;
import com.restapi.wmsservice.dto.response.BomResponse;
import com.restapi.wmsservice.entity.Bom;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.BomMapper;
import com.restapi.wmsservice.repository.BomRepository;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.PlanningRepository;
import com.restapi.wmsservice.enums.PlanningStatus;
import com.restapi.wmsservice.service.BomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BomServiceImpl implements BomService {

    BomRepository bomRepository;
    ItemRepository itemRepository;
    PlanningRepository planningRepository;
    BomMapper bomMapper;

    @Override
    @Transactional
    public BomResponse create(BomRequest request) {
        ensureBomIsMutable();
        if (request.getParentItemId().equals(request.getChildItemId())) {
            throw new AppException(ErrorCode.BOM_PARENT_CHILD_SAME);
        }
        if (bomRepository.existsByParentItemIdAndChildItemId(request.getParentItemId(), request.getChildItemId())) {
            throw new AppException(ErrorCode.BOM_RELATION_EXISTED);
        }
        Item parent = itemRepository.findById(request.getParentItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Item child = itemRepository.findById(request.getChildItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        validateItemRelation(parent, child);
        if (wouldCreateCycle(request.getParentItemId(), request.getChildItemId(), null)) {
            throw new AppException(ErrorCode.BOM_CYCLE_DETECTED);
        }

        Bom bom = bomMapper.toBom(request);
        bom.setParentItem(parent);
        bom.setChildItem(child);

        if (bom.getPriority() == null) {
            bom.setPriority(0);
        }

        bom = bomRepository.save(bom);
        return bomMapper.toBomResponse(bom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomResponse> getAll() {
        return bomRepository.findAll().stream()
                .map(bomMapper::toBomResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomResponse> getByParentItemId(Long parentItemId) {
        return bomRepository.findByParentItemId(parentItemId).stream()
                .map(bomMapper::toBomResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BomResponse getById(Long id) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND));
        return bomMapper.toBomResponse(bom);
    }

    @Override
    @Transactional
    public BomResponse update(Long id, BomRequest request) {
        ensureBomIsMutable();
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND));

        if (request.getParentItemId().equals(request.getChildItemId())) {
            throw new AppException(ErrorCode.BOM_PARENT_CHILD_SAME);
        }
        
        // If changing parent or child, check constraint
        if (!bom.getParentItem().getId().equals(request.getParentItemId()) || 
            !bom.getChildItem().getId().equals(request.getChildItemId())) {
            if (bomRepository.existsByParentItemIdAndChildItemId(request.getParentItemId(), request.getChildItemId())) {
                throw new AppException(ErrorCode.BOM_RELATION_EXISTED);
            }
        }
        if (wouldCreateCycle(request.getParentItemId(), request.getChildItemId(), id)) {
            throw new AppException(ErrorCode.BOM_CYCLE_DETECTED);
        }

        Item parent = itemRepository.findById(request.getParentItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Item child = itemRepository.findById(request.getChildItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        validateItemRelation(parent, child);

        bomMapper.updateBom(bom, request);
        bom.setParentItem(parent);
        bom.setChildItem(child);
        
        if (bom.getPriority() == null) {
            bom.setPriority(0);
        }

        bom = bomRepository.save(bom);
        return bomMapper.toBomResponse(bom);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ensureBomIsMutable();
        if (!bomRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOM_NOT_FOUND);
        }
        bomRepository.deleteById(id);
    }

    private boolean wouldCreateCycle(Long parentItemId, Long childItemId, Long ignoredBomId) {
        return canReach(childItemId, parentItemId, ignoredBomId, new HashSet<>());
    }

    private void validateItemRelation(Item parent, Item child) {
        if (parent.getStatus() != com.restapi.wmsservice.enums.ItemStatus.ACTIVE
                || child.getStatus() != com.restapi.wmsservice.enums.ItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
        }
        boolean validParent = parent.getItemType() == com.restapi.wmsservice.enums.ItemType.SET
                || parent.getItemType() == com.restapi.wmsservice.enums.ItemType.FINISHED_COMPONENT
                || parent.getItemType() == com.restapi.wmsservice.enums.ItemType.RAW_COMPONENT;
        if (!validParent || child.getItemType() != com.restapi.wmsservice.enums.ItemType.FINISHED_COMPONENT) {
            throw new AppException(ErrorCode.INVALID_BOM_ITEM_RELATION);
        }
    }

    private void ensureBomIsMutable() {
        if (planningRepository.existsByStatusIn(List.of(
                PlanningStatus.PLANNING,
                PlanningStatus.APPROVED,
                PlanningStatus.EXECUTING))) {
            throw new AppException(ErrorCode.BOM_LOCKED_BY_ACTIVE_PLANNING);
        }
    }

    private boolean canReach(Long currentItemId, Long targetItemId, Long ignoredBomId, Set<Long> visitedItemIds) {
        if (currentItemId.equals(targetItemId)) {
            return true;
        }
        if (!visitedItemIds.add(currentItemId)) {
            return false;
        }

        for (Bom bom : bomRepository.findByParentItemId(currentItemId)) {
            if (ignoredBomId != null && ignoredBomId.equals(bom.getId())) {
                continue;
            }
            if (canReach(bom.getChildItem().getId(), targetItemId, ignoredBomId, visitedItemIds)) {
                return true;
            }
        }
        return false;
    }
}
