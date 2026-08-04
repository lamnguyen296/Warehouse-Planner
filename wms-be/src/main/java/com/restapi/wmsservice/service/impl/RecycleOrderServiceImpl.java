package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.RecycleOrderRequest;
import com.restapi.wmsservice.dto.response.RecycleOrderResponse;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.PlanningDetail;
import com.restapi.wmsservice.entity.RecycleOrder;
import com.restapi.wmsservice.entity.Bom;
import com.restapi.wmsservice.enums.RecycleStatus;
import com.restapi.wmsservice.enums.ItemType;
import com.restapi.wmsservice.enums.ItemStatus;
import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.RecycleOrderMapper;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.BomRepository;
import com.restapi.wmsservice.repository.PlanningDetailRepository;
import com.restapi.wmsservice.repository.RecycleOrderRepository;
import com.restapi.wmsservice.service.RecycleOrderService;
import com.restapi.wmsservice.service.InventoryOperationsService;
import com.restapi.wmsservice.service.PlanningCompletionService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecycleOrderServiceImpl implements RecycleOrderService {

    RecycleOrderRepository recycleOrderRepository;
    PlanningDetailRepository planningDetailRepository;
    ItemRepository itemRepository;
    BomRepository bomRepository;
    RecycleOrderMapper recycleOrderMapper;
    InventoryOperationsService inventoryOperationsService;
    PlanningCompletionService planningCompletionService;
    NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public RecycleOrderResponse create(RecycleOrderRequest request) {
        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        }

        Item fromItem = itemRepository.findById(request.getFromItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Item toItem = itemRepository.findById(request.getToItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        if (fromItem.getItemType() != ItemType.RAW_COMPONENT || toItem.getItemType() != ItemType.FINISHED_COMPONENT) {
            throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
        }
        if (fromItem.getStatus() != ItemStatus.ACTIVE || toItem.getStatus() != ItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
        }
        Bom conversion = validateConversionBom(fromItem, toItem);
        validatePlanningItem(planningDetail, toItem);
        Integer expectedYield = validatePlannedQuantity(planningDetail, request, fromItem, toItem, null);

        RecycleOrder order = recycleOrderMapper.toEntity(request);

        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        order.setOrderNo("RC-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        order.setPlanningDetail(planningDetail);
        order.setFromItem(fromItem);
        order.setToItem(toItem);
        order.setExpectedYield(expectedYield);
        order.setConversionRatio(conversion.getQuantity());
        order.setStatus(RecycleStatus.PENDING);
        order = recycleOrderRepository.save(order);
        if (planningDetail != null) {
            inventoryOperationsService.reserveRecycleInput(order.getId());
        }
        return recycleOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public RecycleOrderResponse get(Long id) {
        return recycleOrderRepository.findById(id)
                .map(recycleOrderMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecycleOrderResponse> getAll() {
        return recycleOrderRepository.findAll().stream()
                .map(recycleOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecycleOrderResponse update(Long id, RecycleOrderRequest request) {
        RecycleOrder order = recycleOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));

        if (order.getStatus() != RecycleStatus.PENDING) {
            throw new AppException(ErrorCode.RECYCLE_ORDER_INVALID_STATUS);
        }
        validatePlanningLink(order.getPlanningDetail(), request.getPlanningDetailId());

        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        }

        Item fromItem = itemRepository.findById(request.getFromItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Item toItem = itemRepository.findById(request.getToItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        if (fromItem.getItemType() != ItemType.RAW_COMPONENT || toItem.getItemType() != ItemType.FINISHED_COMPONENT) {
            throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
        }
        if (fromItem.getStatus() != ItemStatus.ACTIVE || toItem.getStatus() != ItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
        }
        Bom conversion = validateConversionBom(fromItem, toItem);
        validatePlanningItem(planningDetail, toItem);
        Integer expectedYield = validatePlannedQuantity(planningDetail, request, fromItem, toItem, id);

        inventoryOperationsService.releaseRecycleInput(order.getId());
        recycleOrderMapper.updateEntity(order, request);
        order.setPlanningDetail(planningDetail);
        order.setFromItem(fromItem);
        order.setToItem(toItem);
        order.setExpectedYield(expectedYield);
        order.setConversionRatio(conversion.getQuantity());
        order = recycleOrderRepository.save(order);
        if (planningDetail != null) {
            inventoryOperationsService.reserveRecycleInput(order.getId());
        }
        return recycleOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RecycleOrder order = recycleOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));
        if (order.getStatus() != RecycleStatus.PENDING) {
            throw new AppException(ErrorCode.RECYCLE_ORDER_INVALID_STATUS);
        }
        inventoryOperationsService.releaseRecycleInput(id);
        recycleOrderRepository.deleteById(id);
    }

    // ── Business Flow (Phase 6 – Flow 3) ──────────────────────────────────

    @Override
    @Transactional
    public RecycleOrderResponse startRecycle(Long id) {
        RecycleOrder order = recycleOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));

        if (order.getStatus() != RecycleStatus.PENDING) {
            throw new AppException(ErrorCode.RECYCLE_ORDER_INVALID_STATUS);
        }

        inventoryOperationsService.reserveRecycleInput(id);

        order.setStatus(RecycleStatus.IN_PROGRESS);
        order.setStartTime(LocalDateTime.now());
        order = recycleOrderRepository.save(order);
        log.info("Recycle started [orderId={}, no={}]", order.getId(), order.getOrderNo());
        publishRecycleEvent(order, NotificationType.RECYCLE_STARTED,
                "Recycle started", " is now in progress.");
        return recycleOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public RecycleOrderResponse cancelRecycle(Long id) {
        RecycleOrder order = recycleOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));

        if (order.getStatus() != RecycleStatus.PENDING && order.getStatus() != RecycleStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.RECYCLE_ORDER_INVALID_STATUS);
        }

        inventoryOperationsService.releaseRecycleInput(id);
        order.setStatus(RecycleStatus.FAILED);
        order.setFinishTime(LocalDateTime.now());
        order = recycleOrderRepository.save(order);
        log.info("Recycle cancelled [orderId={}, no={}]", order.getId(), order.getOrderNo());
        publishRecycleEvent(order, NotificationType.RECYCLE_FAILED,
                "Recycle stopped", " was stopped.");
        tryCompletePlanning(order.getPlanningDetail());
        return recycleOrderMapper.toResponse(order);
    }

    private void publishRecycleEvent(RecycleOrder order, NotificationType type,
                                     String title, String messageSuffix) {
        notificationEventPublisher.publish(type, title, order.getOrderNo() + messageSuffix,
                "RecycleOrder", order.getId(), planningRequester(order.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.RECYCLE_READ));
    }

    private Set<String> planningRequester(PlanningDetail detail) {
        if (detail == null || detail.getPlanning() == null
                || detail.getPlanning().getWorkshopRequest() == null) {
            return Set.of();
        }
        String username = detail.getPlanning().getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    private void tryCompletePlanning(PlanningDetail detail) {
        if (detail != null && detail.getPlanning() != null) {
            planningCompletionService.tryComplete(detail.getPlanning().getId());
        }
    }

    private Bom validateConversionBom(Item fromItem, Item toItem) {
        return bomRepository.findByParentItemIdAndChildItemId(fromItem.getId(), toItem.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND));
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

    private Integer validatePlannedQuantity(PlanningDetail planningDetail,
                                            RecycleOrderRequest request,
                                            Item fromItem,
                                            Item toItem,
                                            Long excludedOrderId) {
        long maximumYield = (long) request.getQuantity()
                * bomRepository.findByParentItemIdAndChildItemId(fromItem.getId(), toItem.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND))
                .getQuantity();
        if (maximumYield <= 0 || maximumYield > Integer.MAX_VALUE) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
        if (planningDetail == null) {
            return (int) maximumYield;
        }
        if (planningDetail.getPlanning().getStatus()
                != com.restapi.wmsservice.enums.PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        Bom conversion = bomRepository.findByParentItemIdAndChildItemId(fromItem.getId(), toItem.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND));
        int committedYield = recycleOrderRepository.findByPlanningDetailId(planningDetail.getId()).stream()
                .filter(existing -> !existing.getId().equals(excludedOrderId))
                .filter(existing -> existing.getStatus() != RecycleStatus.FAILED)
                .mapToInt(existing -> existing.getStatus() == RecycleStatus.COMPLETED
                        ? (existing.getActualYield() == null ? 0 : existing.getActualYield())
                        : (existing.getExpectedYield() == null ? 0 : existing.getExpectedYield()))
                .sum();
        int remainingYield = planningDetail.getRecycleQuantity() - committedYield;
        if (remainingYield <= 0) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }
        int requiredInput = (int) (((long) remainingYield + conversion.getQuantity() - 1)
                / conversion.getQuantity());
        if (request.getQuantity() > requiredInput) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }
        return (int) maximumYield;
    }
}
