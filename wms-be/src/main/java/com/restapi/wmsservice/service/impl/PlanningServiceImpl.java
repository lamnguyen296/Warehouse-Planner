package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.PlanningRequest;
import com.restapi.wmsservice.dto.response.PlanningDetailResponse;
import com.restapi.wmsservice.dto.response.PlanningResponse;
import com.restapi.wmsservice.entity.*;
import com.restapi.wmsservice.enums.*;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.PlanningDetailMapper;
import com.restapi.wmsservice.mapper.PlanningMapper;
import com.restapi.wmsservice.repository.*;
import com.restapi.wmsservice.service.PlanningService;
import com.restapi.wmsservice.service.InventoryOperationsService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import com.restapi.wmsservice.service.BomExplosionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanningServiceImpl implements PlanningService {

    PlanningRepository planningRepository;
    WorkshopRequestRepository workshopRequestRepository;
    ItemRepository itemRepository;
    BomRepository bomRepository;
    InventoryRepository inventoryRepository;
    PlanningMapper planningMapper;
    PlanningDetailMapper planningDetailMapper;
    PurchaseRequestRepository purchaseRequestRepository;
    RecycleOrderRepository recycleOrderRepository;
    AssemblyOrderRepository assemblyOrderRepository;
    TransferOrderRepository transferOrderRepository;
    InventoryReservationRepository reservationRepository;
    InventoryOperationsService inventoryOperationsService;
    BomExplosionService bomExplosionService;
    NotificationEventPublisher notificationEventPublisher;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningResponse createPlanning(PlanningRequest request) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));

        Planning planning = planningMapper.toPlanning(request);
        planning.setWorkshopRequest(workshopRequest);
        planning.setStatus(PlanningStatus.DRAFT);

        long version = planningRepository.countByWorkshopRequestId(request.getRequestId()) + 1;
        planning.setPlanningNo(workshopRequest.getRequestNo() + "-V" + version);

        final Planning finalPlanning = planning;
        if (request.getDetails() != null) {
            List<PlanningDetail> details = request.getDetails().stream().map(detailRequest -> {
                Item item = itemRepository.findById(detailRequest.getItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
                PlanningDetail detail = planningDetailMapper.toDetail(detailRequest);
                detail.setItem(item);
                detail.setPlanning(finalPlanning);
                return detail;
            }).collect(Collectors.toList());
            planning.setDetails(details);
        }

        planning = planningRepository.save(planning);
        log.info("Created Planning [id={}, no={}]", planning.getId(), planning.getPlanningNo());
        return mapToResponse(planning);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanningResponse getPlanning(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanningResponse> getAllPlannings() {
        return planningRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanningResponse updatePlanning(Long id, PlanningRequest request) {
        Planning planning = findOrThrow(id);

        // Chỉ cho phép cập nhật DRAFT
        if (planning.getStatus() != PlanningStatus.DRAFT) {
            throw new AppException(ErrorCode.PLANNING_NOT_EDITABLE);
        }

        WorkshopRequest workshopRequest = workshopRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));

        planningMapper.updatePlanning(planning, request);
        planning.setWorkshopRequest(workshopRequest);

        final Planning finalPlanning = planning;
        if (request.getDetails() != null) {
            planning.getDetails().clear();
            List<PlanningDetail> details = request.getDetails().stream().map(detailRequest -> {
                Item item = itemRepository.findById(detailRequest.getItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
                PlanningDetail detail = planningDetailMapper.toDetail(detailRequest);
                detail.setItem(item);
                detail.setPlanning(finalPlanning);
                return detail;
            }).collect(Collectors.toList());
            planning.getDetails().addAll(details);
        }

        planning = planningRepository.save(planning);
        return mapToResponse(planning);
    }

    @Override
    @Transactional
    public void deletePlanning(Long id) {
        Planning planning = findOrThrow(id);
        if (planning.getStatus() != PlanningStatus.DRAFT) {
            throw new AppException(ErrorCode.PLANNING_NOT_EDITABLE);
        }
        planningRepository.deleteById(id);
        log.info("Deleted Planning [id={}]", id);
    }

    // ── Business Flow (Phase 6 – Planning Engine) ─────────────────────────

    /**
     * MRP Planning Engine:
     *  1. Validate WorkshopRequest status = APPROVED
     *  2. Guard: không có Planning đang PLANNING
     *  3. BOM Explosion (WITH RECURSIVE CTE) → aggregate requiredQty per item
     *  4. Check inventory trên COMPONENT_WAREHOUSE
     *  5. Xác định PlanningAction và phân chia quantities
     *  6. Persist Planning + PlanningDetails
     */
    @Override
    @Transactional
    public PlanningResponse runPlanningEngine(Long workshopRequestId) {
        // ── Step 1: Load và validate WorkshopRequest ──────────────────────
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(workshopRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));

        if (workshopRequest.getStatus() != RequestStatus.APPROVED) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_APPROVED);
        }

        // ── Step 2: Guard – không cho chạy song song ──────────────────────
        List<Planning> runningPlannings = planningRepository
                .findByWorkshopRequestIdAndStatusIn(workshopRequestId, activePlanningStatuses());
        if (!runningPlannings.isEmpty()) {
            throw new AppException(ErrorCode.PLANNING_ALREADY_RUNNING);
        }

        // ── Step 3: BOM Explosion + aggregate required quantities ─────────
        // Key: itemId, Value: tổng số cần (accumulated across multiple SET types)
        Map<Long, Integer> requiredQtyMap = new LinkedHashMap<>();
        List<PlanningDetail> planningDetails = new ArrayList<>();

        for (WorkshopRequestDetail requestDetail : workshopRequest.getDetails()) {
            Item setItem = requestDetail.getItem();
            if (setItem.getStatus() != ItemStatus.ACTIVE) {
                throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
            }
            int setQtyNeeded = requestDetail.getQuantity();
            int availableSetQty = inventoryRepository.sumAvailableQuantityByItemAndWarehouseType(
                    setItem.getId(), WarehouseType.SET_WAREHOUSE);
            int assembleQty = Math.max(0, setQtyNeeded - availableSetQty);

            PlanningDetail setDetail = new PlanningDetail();
            setDetail.setItem(setItem);
            setDetail.setRequiredQuantity(setQtyNeeded);
            setDetail.setAvailableQuantity(availableSetQty);
            setDetail.setRecycleQuantity(0);
            setDetail.setPurchaseQuantity(0);
            setDetail.setReservedQuantity(0);
            setDetail.setAction(assembleQty == 0 ? PlanningAction.USE_AVAILABLE : PlanningAction.ASSEMBLE);
            planningDetails.add(setDetail);

            if (assembleQty == 0) {
                continue;
            }

            List<Bom> directBoms = bomRepository.findByParentItemId(setItem.getId());
            if (directBoms.isEmpty()) {
                throw new AppException(ErrorCode.PLANNING_ENGINE_NO_BOM);
            }
            Set<Long> path = new HashSet<>();
            path.add(setItem.getId());
            explodeLeafComponents(setItem, assembleQty, requiredQtyMap, path);

            // BOM explosion dùng WITH RECURSIVE CTE (Native Query)

                // SET không có BOM → chỉ mark ASSEMBLE (tự ghép)

            // Với mỗi BOM node, nhân quantity × setQtyNeeded và gộp
        }

        if (requiredQtyMap.isEmpty() && planningDetails.isEmpty()) {
            throw new AppException(ErrorCode.PLANNING_ENGINE_NO_BOM);
        }

        // ── Step 4–5: Kiểm tra inventory + xác định action ────────────────
        // Load toàn bộ Item info cần thiết trong 1 query
        List<Long> itemIds = new ArrayList<>(requiredQtyMap.keySet());
        Map<Long, Item> itemMap = itemRepository.findAllById(itemIds)
                .stream().collect(Collectors.toMap(Item::getId, i -> i));

        Map<Long, Integer> rawAvailabilityLedger = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : requiredQtyMap.entrySet()) {
            Long itemId = entry.getKey();
            int requiredQty = entry.getValue();

            Item item = itemMap.get(itemId);
            if (item == null) {
                log.warn("Item [id={}] from BOM not found in item table, skipping.", itemId);
                continue;
            }

            PlanningDetail detail = buildPlanningDetail(item, requiredQty, rawAvailabilityLedger);
            planningDetails.add(detail);
        }

        // ── Step 6: Tạo Planning entity ────────────────────────────────────
        long versionNumber = planningRepository.countByWorkshopRequestId(workshopRequestId) + 1;
        String planningNo = workshopRequest.getRequestNo() + "-V" + versionNumber;

        Planning planning = new Planning();
        planning.setWorkshopRequest(workshopRequest);
        planning.setPlanningNo(planningNo);
        planning.setStatus(PlanningStatus.PLANNING);

        // Link mỗi detail về planning
        for (PlanningDetail detail : planningDetails) {
            detail.setPlanning(planning);
        }
        planning.setDetails(planningDetails);

        planning = planningRepository.save(planning);
        log.info("Planning Engine completed [planningNo={}, details={}]",
                planning.getPlanningNo(), planning.getDetails().size());
        notificationEventPublisher.publish(NotificationType.PLANNING_CREATED,
                "Planning created",
                planning.getPlanningNo() + " is waiting for approval.",
                "Planning", planning.getId(), planningRequester(planning),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_APPROVE));

        return mapToResponse(planning);
    }

    @Override
    @Transactional
    public PlanningResponse approvePlanning(Long id) {
        Planning planning = planningRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));

        if (planning.getStatus() != PlanningStatus.PLANNING) {
            throw new AppException(ErrorCode.INVALID_REQUEST_STATUS_TRANSITION);
        }

        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(
                        planning.getWorkshopRequest().getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        if (planningRepository.existsByWorkshopRequestIdAndStatusInAndIdNot(
                workshopRequest.getId(), activePlanningStatuses(), planning.getId())) {
            throw new AppException(ErrorCode.PLANNING_ALREADY_RUNNING);
        }

        planning.setStatus(PlanningStatus.APPROVED);
        planning = planningRepository.save(planning);
        log.info("Approved Planning [id={}, no={}]", planning.getId(), planning.getPlanningNo());
        notificationEventPublisher.publish(NotificationType.PLANNING_APPROVED,
                "Planning approved",
                planning.getPlanningNo() + " is ready for execution.",
                "Planning", planning.getId(), planningRequester(planning),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_EXECUTE));
        return mapToResponse(planning);
    }

    @Override
    @Transactional
    public PlanningResponse startExecution(Long id) {
        Planning planning = planningRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
        if (planning.getStatus() != PlanningStatus.APPROVED) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(
                        planning.getWorkshopRequest().getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        if (workshopRequest.getStatus() != RequestStatus.APPROVED
                || !planningRepository.findByWorkshopRequestIdAndStatus(
                        workshopRequest.getId(), PlanningStatus.EXECUTING).isEmpty()) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }

        planning.setStatus(PlanningStatus.EXECUTING);
        workshopRequest.setStatus(RequestStatus.IN_PROGRESS);
        planningRepository.save(planning);

        for (PlanningDetail detail : planning.getDetails()) {
            int reservable = Math.min(detail.getRequiredQuantity(), detail.getAvailableQuantity());
            if (reservable > 0) {
                inventoryOperationsService.reserveInventory(detail.getId(), null, reservable);
            }

            if (detail.getPurchaseQuantity() > 0) {
                createPlannedPurchase(detail, detail.getPurchaseQuantity());
            }
            if (detail.getRecycleQuantity() > 0) {
                createPlannedRecycleOrders(detail);
            }
            if (detail.getItem().getItemType() == ItemType.SET
                    && detail.getAction() == PlanningAction.ASSEMBLE) {
                createPlannedAssembly(detail);
            }
        }

        log.info("Planning execution started [id={}, no={}]", planning.getId(), planning.getPlanningNo());
        notificationEventPublisher.publish(NotificationType.PLANNING_EXECUTION_STARTED,
                "Planning execution started",
                planning.getPlanningNo() + " is now executing.",
                "Planning", planning.getId(), planningRequester(planning),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_READ));
        return mapToResponse(planning);
    }

    @Override
    @Transactional
    public PlanningResponse failPlanning(Long id) {
        Planning planning = planningRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
        if (planning.getStatus() != PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }

        boolean nonCancellablePurchase = planning.getDetails().stream()
                .flatMap(detail -> purchaseRequestRepository.findByPlanningDetailId(detail.getId()).stream())
                .anyMatch(request -> request.getStatus() == PurchaseStatus.ORDERED
                        || request.getStatus() == PurchaseStatus.PARTIAL_RECEIVED
                        || request.getStatus() == PurchaseStatus.RECEIVED);
        boolean transferInProgress = transferOrderRepository.findByPlanningId(id).stream()
                .anyMatch(transfer -> transfer.getStatus() == TransferStatus.IN_TRANSIT
                        || transfer.getStatus() == TransferStatus.COMPLETED);
        if (nonCancellablePurchase || transferInProgress) {
            throw new AppException(ErrorCode.PLANNING_HAS_NON_CANCELLABLE_OPERATIONS);
        }

        for (PlanningDetail detail : planning.getDetails()) {
            purchaseRequestRepository.findByPlanningDetailId(detail.getId()).stream()
                    .filter(request -> request.getStatus() == PurchaseStatus.PENDING
                            || request.getStatus() == PurchaseStatus.APPROVED)
                    .forEach(request -> request.setStatus(PurchaseStatus.CANCELLED));
            recycleOrderRepository.findByPlanningDetailId(detail.getId()).stream()
                    .filter(order -> order.getStatus() == RecycleStatus.PENDING
                            || order.getStatus() == RecycleStatus.IN_PROGRESS)
                    .forEach(order -> order.setStatus(RecycleStatus.FAILED));
            assemblyOrderRepository.findByPlanningDetailId(detail.getId()).stream()
                    .filter(order -> order.getStatus() == AssemblyStatus.PENDING
                            || order.getStatus() == AssemblyStatus.IN_PROGRESS)
                    .forEach(order -> order.setStatus(AssemblyStatus.FAILED));
        }
        transferOrderRepository.findByPlanningId(id).stream()
                .filter(transfer -> transfer.getStatus() == TransferStatus.PENDING)
                .forEach(transfer -> transfer.setStatus(TransferStatus.CANCELLED));

        List<InventoryReservation> reservations = reservationRepository
                .findByPlanningAndStatusForUpdate(id, ReservationStatus.RESERVED);
        for (InventoryReservation reservation : reservations) {
            inventoryOperationsService.releaseReservation(reservation.getId());
        }

        planning.setStatus(PlanningStatus.FAILED);
        planning.getWorkshopRequest().setStatus(RequestStatus.APPROVED);
        planningRepository.save(planning);
        notificationEventPublisher.publish(NotificationType.PLANNING_FAILED,
                "Planning failed",
                planning.getPlanningNo() + " was stopped and can be planned again.",
                "Planning", planning.getId(), planningRequester(planning),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_READ));
        return mapToResponse(planning);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanningResponse> getByWorkshopRequest(Long workshopRequestId) {
        return planningRepository.findByWorkshopRequestId(workshopRequestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanningResponse> getByStatus(PlanningStatus status) {
        return planningRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Planning findOrThrow(Long id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
    }

    private Set<String> planningRequester(Planning planning) {
        String username = planning.getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    /**
     * Xác định PlanningAction và tính phân bổ quantities cho một item.
     *
     * Logic quyết định (theo tài liệu):
     *
     *  - Nếu item là SET → action = ASSEMBLE (ghép SET từ components)
     *
     *  - Nếu item là FINISHED_COMPONENT:
     *    1. Lấy availableQty trên COMPONENT_WAREHOUSE
     *    2. Nếu available >= required → USE_AVAILABLE
     *    3. Nếu available < required:
     *       - Kiểm tra RAW_COMPONENT tương ứng (qua BOM ngược)
     *       - Nếu có RAW và đủ raw → RECYCLE (phần thiếu)
     *       - Nếu không đủ raw → PURCHASE (phần thiếu)
     *
     *  - Nếu item là RAW_COMPONENT:
     *    1. Check available trên COMPONENT_WAREHOUSE
     *    2. Nếu đủ → USE_AVAILABLE (dùng raw trực tiếp, sẽ recycle riêng)
     *    3. Nếu không đủ → PURCHASE
     */
    private PlanningDetail buildPlanningDetail(Item item,
                                                int requiredQty,
                                                Map<Long, Integer> rawAvailabilityLedger) {
        PlanningDetail detail = new PlanningDetail();
        detail.setItem(item);
        detail.setRequiredQuantity(requiredQty);

        if (item.getItemType() == ItemType.SET) {
            // SET → cần ASSEMBLE
            detail.setAvailableQuantity(
                    inventoryRepository.sumAvailableQuantityByItemAndWarehouseType(
                            item.getId(), WarehouseType.SET_WAREHOUSE));
            detail.setRecycleQuantity(0);
            detail.setPurchaseQuantity(0);
            detail.setReservedQuantity(0);
            detail.setAction(PlanningAction.ASSEMBLE);
            return detail;
        }

        // FINISHED_COMPONENT hoặc RAW_COMPONENT
        int availableOnComponentWh = inventoryRepository.sumAvailableQuantityByItemAndWarehouseType(
                item.getId(), WarehouseType.COMPONENT_WAREHOUSE);

        detail.setAvailableQuantity(availableOnComponentWh);

        int shortage = Math.max(0, requiredQty - availableOnComponentWh);

        if (shortage == 0) {
            // Đủ tồn kho → USE_AVAILABLE
            detail.setRecycleQuantity(0);
            detail.setPurchaseQuantity(0);
            detail.setReservedQuantity(0);
            detail.setAction(PlanningAction.USE_AVAILABLE);
            return detail;
        }

        // Thiếu → phân tích thêm
        if (item.getItemType() == ItemType.FINISHED_COMPONENT) {
            // Tìm RAW_COMPONENT có thể recycle thành FINISHED_COMPONENT này qua BOM ngược
            // BOM ngược: child = finished_component → parent = raw_component (nếu có)
            List<Bom> rawBoms = bomRepository.findByChildItemId(item.getId()).stream()
                    .filter(bom -> bom.getParentItem().getItemType() == ItemType.RAW_COMPONENT)
                    .filter(bom -> bom.getParentItem().getStatus() == ItemStatus.ACTIVE)
                    .sorted(Comparator
                            .comparing((Bom bom) -> Optional.ofNullable(bom.getPriority()).orElse(0))
                            .thenComparing(Bom::getId))
                    .toList();
            int recyclableOutput = 0;
            for (Bom rawBom : rawBoms) {
                int remainingOutput = shortage - recyclableOutput;
                if (remainingOutput <= 0) {
                    break;
                }
                Long rawItemId = rawBom.getParentItem().getId();
                int rawAvailable = rawAvailabilityLedger.computeIfAbsent(rawItemId,
                        id -> inventoryRepository.sumAvailableQuantityByItemAndWarehouseType(
                                id, WarehouseType.COMPONENT_WAREHOUSE));
                int ratio = rawBom.getQuantity();
                int inputNeeded = (int) (((long) remainingOutput + ratio - 1) / ratio);
                int allocatedInput = Math.min(rawAvailable, inputNeeded);
                int allocatedOutput = Math.min(remainingOutput, checkedMultiply(allocatedInput, ratio));
                rawAvailabilityLedger.put(rawItemId, rawAvailable - allocatedInput);
                recyclableOutput += allocatedOutput;
            }

            // recycle phần có raw, purchase phần còn thiếu
            int recycleQty = recyclableOutput;
            int purchaseQty = shortage - recycleQty;

            detail.setRecycleQuantity(recycleQty);
            detail.setPurchaseQuantity(purchaseQty);
            detail.setReservedQuantity(0);
            // Nếu có thể recycle toàn bộ → RECYCLE, còn lại → PURCHASE (kể cả khi vừa recycle vừa purchase)
            if (recycleQty > 0 && purchaseQty > 0) {
                detail.setAction(PlanningAction.RECYCLE_AND_PURCHASE);
            } else if (recycleQty > 0) {
                detail.setAction(PlanningAction.RECYCLE);
            } else {
                detail.setAction(PlanningAction.PURCHASE);
            }

        } else {
            // RAW_COMPONENT thiếu → PURCHASE
            detail.setRecycleQuantity(0);
            detail.setPurchaseQuantity(shortage);
            detail.setReservedQuantity(0);
            detail.setAction(PlanningAction.PURCHASE);
        }

        return detail;
    }

    private void explodeLeafComponents(Item parentItem,
                                       int parentRequiredQty,
                                       Map<Long, Integer> requiredQtyMap,
                                       Set<Long> path) {
        List<Bom> children = bomRepository.findByParentItemId(parentItem.getId());
        if (children.isEmpty()) {
            requiredQtyMap.merge(parentItem.getId(), parentRequiredQty, this::checkedAdd);
            return;
        }

        for (Bom bom : children) {
            Item childItem = bom.getChildItem();
            if (childItem.getStatus() != ItemStatus.ACTIVE) {
                throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
            }
            if (path.contains(childItem.getId())) {
                throw new AppException(ErrorCode.BOM_CYCLE_DETECTED);
            }

            int childRequiredQty = checkedMultiply(parentRequiredQty, bom.getQuantity());
            path.add(childItem.getId());
            explodeLeafComponents(childItem, childRequiredQty, requiredQtyMap, path);
            path.remove(childItem.getId());
        }
    }

    private int checkedMultiply(int left, int right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    private int checkedAdd(int left, int right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    private void createPlannedPurchase(PlanningDetail detail, int quantity) {
        if (purchaseRequestRepository.existsByPlanningDetailId(detail.getId())) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        PurchaseRequest request = new PurchaseRequest();
        request.setRequestNo(buildExecutionNumber("PR"));
        request.setPlanningDetail(detail);
        request.setStatus(PurchaseStatus.PENDING);

        PurchaseRequestDetail requestDetail = new PurchaseRequestDetail();
        requestDetail.setPurchaseRequest(request);
        requestDetail.setItem(detail.getItem());
        requestDetail.setQuantity(quantity);
        requestDetail.setReceivedQuantity(0);
        request.getDetails().add(requestDetail);
        purchaseRequestRepository.save(request);
    }

    private void createPlannedRecycleOrders(PlanningDetail detail) {
        if (recycleOrderRepository.existsByPlanningDetailId(detail.getId())) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        List<Bom> conversions = bomRepository.findByChildItemId(detail.getItem().getId()).stream()
                .filter(bom -> bom.getParentItem().getItemType() == ItemType.RAW_COMPONENT)
                .filter(bom -> bom.getParentItem().getStatus() == ItemStatus.ACTIVE)
                .sorted(Comparator
                        .comparing((Bom bom) -> Optional.ofNullable(bom.getPriority()).orElse(0))
                        .thenComparing(Bom::getId))
                .toList();

        int remainingOutput = detail.getRecycleQuantity();
        for (Bom conversion : conversions) {
            if (remainingOutput <= 0) {
                break;
            }
            int rawAvailable = inventoryRepository.sumAvailableQuantityByItemAndWarehouseType(
                    conversion.getParentItem().getId(), WarehouseType.COMPONENT_WAREHOUSE);
            int ratio = conversion.getQuantity();
            int inputNeeded = (int) (((long) remainingOutput + ratio - 1) / ratio);
            int inputQuantity = Math.min(rawAvailable, inputNeeded);
            if (inputQuantity <= 0) {
                continue;
            }

            int expectedYield = checkedMultiply(inputQuantity, ratio);
            RecycleOrder order = new RecycleOrder();
            order.setOrderNo(buildExecutionNumber("RC"));
            order.setPlanningDetail(detail);
            order.setFromItem(conversion.getParentItem());
            order.setToItem(detail.getItem());
            order.setQuantity(inputQuantity);
            order.setExpectedYield(expectedYield);
            order.setConversionRatio(ratio);
            order.setStatus(RecycleStatus.PENDING);
            order = recycleOrderRepository.save(order);
            inventoryOperationsService.reserveRecycleInput(order.getId());
            remainingOutput -= Math.min(remainingOutput, expectedYield);
        }
        if (remainingOutput > 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }
    }

    private void createPlannedAssembly(PlanningDetail detail) {
        if (assemblyOrderRepository.existsByPlanningDetailId(detail.getId())) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        int quantity = detail.getRequiredQuantity() - Math.min(
                detail.getRequiredQuantity(), detail.getAvailableQuantity());
        if (quantity <= 0) {
            return;
        }
        AssemblyOrder order = new AssemblyOrder();
        order.setAssemblyNo(buildExecutionNumber("AS"));
        order.setPlanningDetail(detail);
        order.setSetItem(detail.getItem());
        order.setQuantity(quantity);
        order.setStatus(AssemblyStatus.PENDING);
        populateAssemblySnapshot(order);
        assemblyOrderRepository.save(order);
    }

    private void populateAssemblySnapshot(AssemblyOrder order) {
        List<BomExplosionService.ComponentRequirement> requirements =
                bomExplosionService.explodeLeafComponents(order.getSetItem(), order.getQuantity());
        if (requirements.isEmpty()
                || (requirements.size() == 1
                && requirements.get(0).item().getId().equals(order.getSetItem().getId()))) {
            throw new AppException(ErrorCode.PLANNING_ENGINE_NO_BOM);
        }
        for (BomExplosionService.ComponentRequirement requirement : requirements) {
            AssemblyOrderComponent component = new AssemblyOrderComponent();
            component.setAssemblyOrder(order);
            component.setItem(requirement.item());
            component.setRequiredQuantity(requirement.quantity());
            order.getComponents().add(component);
        }
    }

    private List<PlanningStatus> activePlanningStatuses() {
        return List.of(PlanningStatus.PLANNING, PlanningStatus.APPROVED, PlanningStatus.EXECUTING);
    }

    private String buildExecutionNumber(String prefix) {
        return prefix + "-" + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map Planning entity → PlanningResponse DTO.
     * Details được load sẵn qua @EntityGraph → không phát sinh thêm query.
     */
    private PlanningResponse mapToResponse(Planning planning) {
        PlanningResponse response = planningMapper.toResponse(planning);
        if (planning.getDetails() != null) {
            List<PlanningDetailResponse> detailResponses = planning.getDetails().stream()
                    .map(planningDetailMapper::toResponse)
                    .collect(Collectors.toList());
            response.setDetails(detailResponses);
        }
        return response;
    }
}
