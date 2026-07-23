package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.AssemblyOrderRequest;
import com.restapi.wmsservice.dto.response.AssemblyOrderResponse;
import com.restapi.wmsservice.entity.AssemblyOrder;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.PlanningDetail;
import com.restapi.wmsservice.enums.AssemblyStatus;
import com.restapi.wmsservice.enums.ItemType;
import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.AssemblyOrderMapper;
import com.restapi.wmsservice.repository.AssemblyOrderRepository;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.PlanningDetailRepository;
import com.restapi.wmsservice.service.AssemblyOrderService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssemblyOrderServiceImpl implements AssemblyOrderService {

    AssemblyOrderRepository assemblyOrderRepository;
    PlanningDetailRepository planningDetailRepository;
    ItemRepository itemRepository;
    AssemblyOrderMapper assemblyOrderMapper;
    NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public AssemblyOrderResponse create(AssemblyOrderRequest request) {
        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        }

        Item setItem = itemRepository.findById(request.getSetItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        if (setItem.getItemType() != ItemType.SET) {
            throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
        }
        validatePlanningItem(planningDetail, setItem);
        validatePlannedQuantity(planningDetail, request.getQuantity(), null);

        AssemblyOrder order = assemblyOrderMapper.toEntity(request);

        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        order.setAssemblyNo("AS-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        order.setPlanningDetail(planningDetail);
        order.setSetItem(setItem);
        order.setStatus(AssemblyStatus.PENDING);

        return assemblyOrderMapper.toResponse(assemblyOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public AssemblyOrderResponse get(Long id) {
        return assemblyOrderRepository.findById(id)
                .map(assemblyOrderMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssemblyOrderResponse> getAll() {
        return assemblyOrderRepository.findAll().stream()
                .map(assemblyOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssemblyOrderResponse update(Long id, AssemblyOrderRequest request) {
        AssemblyOrder order = assemblyOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));

        if (order.getStatus() != AssemblyStatus.PENDING) {
            throw new AppException(ErrorCode.ASSEMBLY_ORDER_INVALID_STATUS);
        }
        validatePlanningLink(order.getPlanningDetail(), request.getPlanningDetailId());

        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        }

        Item setItem = itemRepository.findById(request.getSetItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        if (setItem.getItemType() != ItemType.SET) {
            throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
        }
        validatePlanningItem(planningDetail, setItem);
        validatePlannedQuantity(planningDetail, request.getQuantity(), id);

        assemblyOrderMapper.updateEntity(order, request);
        order.setPlanningDetail(planningDetail);
        order.setSetItem(setItem);

        return assemblyOrderMapper.toResponse(assemblyOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AssemblyOrder order = assemblyOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));
        if (order.getStatus() != AssemblyStatus.PENDING) {
            throw new AppException(ErrorCode.ASSEMBLY_ORDER_INVALID_STATUS);
        }
        assemblyOrderRepository.deleteById(id);
    }

    // ── Business Flow (Phase 6 – Flow 3) ──────────────────────────────────

    @Override
    @Transactional
    public AssemblyOrderResponse startAssembly(Long id) {
        AssemblyOrder order = assemblyOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));

        if (order.getStatus() != AssemblyStatus.PENDING) {
            throw new AppException(ErrorCode.ASSEMBLY_ORDER_INVALID_STATUS);
        }

        order.setStatus(AssemblyStatus.IN_PROGRESS);
        order = assemblyOrderRepository.save(order);
        log.info("Assembly started [orderId={}, no={}]", order.getId(), order.getAssemblyNo());
        publishAssemblyEvent(order, NotificationType.ASSEMBLY_STARTED,
                "Assembly started", " is now in progress.");
        return assemblyOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public AssemblyOrderResponse cancelAssembly(Long id) {
        AssemblyOrder order = assemblyOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));

        if (order.getStatus() != AssemblyStatus.PENDING && order.getStatus() != AssemblyStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.ASSEMBLY_ORDER_INVALID_STATUS);
        }

        order.setStatus(AssemblyStatus.FAILED);
        order = assemblyOrderRepository.save(order);
        log.info("Assembly cancelled [orderId={}, no={}]", order.getId(), order.getAssemblyNo());
        publishAssemblyEvent(order, NotificationType.ASSEMBLY_FAILED,
                "Assembly stopped", " was stopped.");
        return assemblyOrderMapper.toResponse(order);
    }

    private void publishAssemblyEvent(AssemblyOrder order, NotificationType type,
                                      String title, String messageSuffix) {
        notificationEventPublisher.publish(type, title, order.getAssemblyNo() + messageSuffix,
                "AssemblyOrder", order.getId(), planningRequester(order.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.ASSEMBLY_READ));
    }

    private Set<String> planningRequester(PlanningDetail detail) {
        if (detail == null || detail.getPlanning() == null
                || detail.getPlanning().getWorkshopRequest() == null) {
            return Set.of();
        }
        String username = detail.getPlanning().getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    private void validatePlanningItem(PlanningDetail planningDetail, Item item) {
        if (planningDetail != null && !planningDetail.getItem().getId().equals(item.getId())) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }
    }

    private void validatePlanningLink(PlanningDetail currentPlanningDetail, Long requestedPlanningDetailId) {
        if (currentPlanningDetail != null
                && !currentPlanningDetail.getId().equals(requestedPlanningDetailId)) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }
    }

    private void validatePlannedQuantity(PlanningDetail planningDetail,
                                         int quantity,
                                         Long excludedOrderId) {
        if (planningDetail == null) {
            return;
        }
        if (planningDetail.getPlanning().getStatus()
                != com.restapi.wmsservice.enums.PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        int committedQuantity = assemblyOrderRepository.findByPlanningDetailId(planningDetail.getId()).stream()
                .filter(existing -> !existing.getId().equals(excludedOrderId))
                .filter(existing -> existing.getStatus() != AssemblyStatus.FAILED)
                .mapToInt(AssemblyOrder::getQuantity)
                .sum();
        int assemblyRequired = planningDetail.getRequiredQuantity()
                - Math.min(planningDetail.getRequiredQuantity(), planningDetail.getAvailableQuantity());
        if ((long) committedQuantity + quantity > assemblyRequired) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }
    }
}
