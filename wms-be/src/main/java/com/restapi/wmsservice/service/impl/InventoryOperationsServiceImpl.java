package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.response.InventoryReservationResponse;
import com.restapi.wmsservice.entity.*;
import com.restapi.wmsservice.enums.*;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.*;
import com.restapi.wmsservice.service.InventoryOperationsService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryOperationsServiceImpl implements InventoryOperationsService {

    // ── Repositories ──────────────────────────────────────────────────────
    InventoryRepository inventoryRepository;
    InventoryReservationRepository reservationRepository;
    InventoryTransactionRepository transactionRepository;
    PlanningDetailRepository planningDetailRepository;
    RecycleOrderRepository recycleOrderRepository;
    AssemblyOrderRepository assemblyOrderRepository;
    PurchaseRequestRepository purchaseRequestRepository;
    BomRepository bomRepository;
    WarehouseRepository warehouseRepository;
    LocationRepository locationRepository;
    ItemRepository itemRepository;
    NotificationEventPublisher notificationEventPublisher;

    // ═══════════════════════════════════════════════════════════════════════
    // 1. RESERVE INVENTORY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Reserve tồn kho cho PlanningDetail.
     *
     * Transaction boundary: toàn bộ operation trong 1 transaction.
     * Optimistic Locking: @Version trên Inventory entity → JPA ném
     * ObjectOptimisticLockingFailureException nếu có concurrent update.
     */
    @Override
    @Transactional
    public List<InventoryReservationResponse> reserveInventory(Long planningDetailId,
                                                               Long warehouseId,
                                                               int quantity) {
        if (quantity <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        PlanningDetail planningDetail = planningDetailRepository.findByIdForUpdate(planningDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));

        if (planningDetail.getPlanning().getStatus() != PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        int remainingRequired = planningDetail.getRequiredQuantity() - planningDetail.getReservedQuantity();
        if (quantity > remainingRequired) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }

        Item item = planningDetail.getItem();

        // Tìm inventory record phù hợp (warehouse cụ thể hoặc COMPONENT_WAREHOUSE đầu tiên có đủ hàng)
        List<Inventory> candidates = findReservationCandidates(item, warehouseId);
        int totalAvailable = candidates.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        if (totalAvailable < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }

        List<InventoryReservationResponse> responses = new ArrayList<>();
        int remaining = quantity;
        for (Inventory inventory : candidates) {
            if (remaining <= 0) {
                break;
            }
            int reserveQty = Math.min(remaining, inventory.getAvailableQuantity());
            if (reserveQty <= 0) {
                continue;
            }
            responses.add(doReserve(planningDetail, inventory, item, reserveQty));
            remaining -= reserveQty;
        }

        return responses;
    }

    private InventoryReservationResponse doReserve(PlanningDetail planningDetail,
                                                    Inventory inventory,
                                                    Item item,
                                                    int quantity) {
        // Guard: double-check available quantity
        if (inventory.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }

        // Cập nhật inventory numbers
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        // JPA sẽ ném ObjectOptimisticLockingFailureException nếu @Version mismatch
        inventoryRepository.save(inventory);

        planningDetail.setReservedQuantity(planningDetail.getReservedQuantity() + quantity);
        planningDetailRepository.save(planningDetail);

        // Tạo Reservation record (TTL 24h mặc định)
        InventoryReservation reservation = new InventoryReservation();
        reservation.setPlanningDetail(planningDetail);
        reservation.setItem(item);
        reservation.setWarehouse(inventory.getWarehouse());
        reservation.setInventory(inventory);
        reservation.setQuantity(quantity);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiredTime(LocalDateTime.now().plusHours(24));
        reservation = reservationRepository.save(reservation);

        log.info("Reserved [item={}, qty={}, warehouse={}]",
                item.getCode(), quantity, inventory.getWarehouse().getCode());

        return mapReservationToResponse(reservation);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. RELEASE RESERVATION
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void releaseReservation(Long reservationId) {
        InventoryReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new AppException(ErrorCode.RESERVATION_ALREADY_RELEASED);
        }

        // Tìm và hoàn trả lại inventory
        Inventory inventory = reservation.getInventory();
        if (inventory == null) {
            inventory = findInventoryOrThrow(reservation.getItem().getId(), reservation.getWarehouse().getId());
        }
        if (inventory.getReservedQuantity() < reservation.getQuantity()) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
        inventory.setAvailableQuantity(inventory.getTotalQuantity() - inventory.getReservedQuantity());
        inventoryRepository.save(inventory);

        PlanningDetail planningDetail = planningDetailRepository
                .findByIdForUpdate(reservation.getPlanningDetail().getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        planningDetail.setReservedQuantity(Math.max(0,
                planningDetail.getReservedQuantity() - reservation.getQuantity()));
        planningDetailRepository.save(planningDetail);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        log.info("Released reservation [id={}, item={}, qty={}]",
                reservationId, reservation.getItem().getCode(), reservation.getQuantity());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. COMPLETE RECYCLE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Recycle: RAW_COMPONENT → FINISHED_COMPONENT.
     *
     * Inventory movements:
     *   DEBIT:  fromItem (RAW) trên COMPONENT_WAREHOUSE  -= quantity
     *   CREDIT: toItem (FINISHED) trên COMPONENT_WAREHOUSE += actualYield
     *
     * Atomic trong 1 transaction. Nếu bất kỳ bước nào fail → rollback toàn bộ.
     */
    @Override
    @Transactional
    public void completeRecycle(Long recycleOrderId, int actualYield) {
        if (actualYield < 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        RecycleOrder order = recycleOrderRepository.findByIdForUpdate(recycleOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.RECYCLE_ORDER_NOT_FOUND));

        // Validate status
        if (order.getStatus() != RecycleStatus.PENDING && order.getStatus() != RecycleStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.RECYCLE_ORDER_INVALID_STATUS);
        }

        Item fromItem = order.getFromItem();   // RAW_COMPONENT
        Item toItem   = order.getToItem();     // FINISHED_COMPONENT
        int  inputQty = order.getQuantity();
        Bom conversionBom = bomRepository
                .findByParentItemIdAndChildItemId(fromItem.getId(), toItem.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BOM_NOT_FOUND));
        long maximumYield = (long) inputQty * conversionBom.getQuantity();
        if (actualYield < 0 || actualYield > maximumYield) {
            throw new AppException(ErrorCode.RECYCLE_YIELD_EXCEEDS_EXPECTED);
        }
        if (order.getExpectedYield() != null && actualYield > order.getExpectedYield()) {
            throw new AppException(ErrorCode.RECYCLE_YIELD_EXCEEDS_EXPECTED);
        }

        // Tìm COMPONENT_WAREHOUSE
        Warehouse componentWarehouse = findActiveWarehouseByType(WarehouseType.COMPONENT_WAREHOUSE);

        // ── DEBIT: trừ RAW từ COMPONENT_WAREHOUSE ────────────────────────
        List<Inventory> fromInventories = inventoryRepository.findByItemIdAndWarehouseId(fromItem.getId(), componentWarehouse.getId());
        int totalFromAvailable = fromInventories.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        if (totalFromAvailable < inputQty) {
            throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }

        // ── CREDIT: cộng FINISHED vào COMPONENT_WAREHOUSE ─────────────────
        // (we just use the first available location or null for the finished component)
        Location toLocation = null;
        if (!fromInventories.isEmpty() && fromInventories.get(0).getLocation() != null) {
            toLocation = fromInventories.get(0).getLocation();
        }
        Inventory toInventory = findOrCreateInventory(toItem, componentWarehouse, toLocation);
        toInventory.setAvailableQuantity(toInventory.getAvailableQuantity() + actualYield);
        toInventory.setTotalQuantity(toInventory.getTotalQuantity() + actualYield);
        inventoryRepository.save(toInventory);
        reserveProducedInventory(order.getPlanningDetail(), toInventory, actualYield);

        // ── Cập nhật RecycleOrder ──────────────────────────────────────────
        order.setActualYield(actualYield);
        order.setStatus(RecycleStatus.COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        recycleOrderRepository.save(order);

        // ── Ghi InventoryTransaction ───────────────────────────────────────
        InventoryTransaction txn = buildTransaction(
                TransactionType.RECYCLING,
                componentWarehouse,   // from = COMPONENT_WAREHOUSE (RAW side)
                componentWarehouse,   // to   = COMPONENT_WAREHOUSE (FINISHED side)
                "RecycleOrder",
                recycleOrderId);

        // FIFO Deduction for DEBIT
        int remaining = inputQty;
        for (Inventory inv : fromInventories) {
            if (remaining <= 0) break;
            int deduct = Math.min(remaining, inv.getAvailableQuantity());
            inv.setAvailableQuantity(inv.getAvailableQuantity() - deduct);
            inv.setTotalQuantity(inv.getTotalQuantity() - deduct);
            inventoryRepository.save(inv);
            
            InventoryTransactionDetail debitDetail = buildTxnDetail(txn, fromItem, inv.getLocation(), null, deduct);
            txn.getDetails().add(debitDetail);
            
            remaining -= deduct;
        }

        InventoryTransactionDetail creditDetail = buildTxnDetail(txn, toItem,
                null, toLocation, actualYield);
        txn.getDetails().add(creditDetail);
        transactionRepository.save(txn);

        log.info("Recycle completed [orderId={}, from={} qty={}, to={} yield={}]",
                recycleOrderId, fromItem.getCode(), inputQty, toItem.getCode(), actualYield);
        notificationEventPublisher.publish(NotificationType.RECYCLE_COMPLETED,
                "Recycle completed",
                order.getOrderNo() + " produced " + actualYield + " " + toItem.getCode() + ".",
                "RecycleOrder", order.getId(), planningRequester(order.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.RECYCLE_READ));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. RECEIVE GOODS (Purchase)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Nhận hàng mua vào kho COMPONENT_WAREHOUSE.
     *
     * Hỗ trợ nhận từng phần (partial receipt).
     * Inventory movement:
     *   CREDIT: item trên warehouseId/locationId += receivedQty
     *
     * Idempotent-safe: nếu inventory record chưa tồn tại thì tạo mới (upsert).
     */
    @Override
    @Transactional
    public void receiveGoods(Long purchaseRequestId, Long purchaseDetailId,
                             int receivedQty, Long warehouseId, Long locationId) {
        if (receivedQty <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(purchaseRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        // Validate PurchaseRequest status
        if (purchaseRequest.getStatus() != PurchaseStatus.ORDERED
                && purchaseRequest.getStatus() != PurchaseStatus.PARTIAL_RECEIVED) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }

        // Tìm detail cụ thể
        PurchaseRequestDetail detail = purchaseRequest.getDetails().stream()
                .filter(d -> d.getId().equals(purchaseDetailId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));

        // Guard: không nhận vượt quá số đã đặt
        int alreadyReceived = detail.getReceivedQuantity() == null ? 0 : detail.getReceivedQuantity();
        if (alreadyReceived + receivedQty > detail.getQuantity()) {
            throw new AppException(ErrorCode.RECEIVE_QUANTITY_EXCEEDS_ORDERED);
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (warehouse.getType() != WarehouseType.COMPONENT_WAREHOUSE) {
            throw new AppException(ErrorCode.INVALID_WAREHOUSE_TYPE);
        }
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_NOT_CONFIGURED);
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        if (!location.getWarehouse().getId().equals(warehouse.getId())) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }

        Item item = detail.getItem();

        // ── Upsert Inventory ───────────────────────────────────────────────
        Inventory inventory = inventoryRepository
                .findByItemIdAndWarehouseIdAndLocationId(item.getId(), warehouseId, locationId)
                .orElseGet(() -> createNewInventory(item, warehouse, location));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + receivedQty);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + receivedQty);
        inventoryRepository.save(inventory);
        reserveProducedInventory(purchaseRequest.getPlanningDetail(), inventory, receivedQty);

        // ── Cập nhật PurchaseRequestDetail ────────────────────────────────
        detail.setReceivedQuantity(alreadyReceived + receivedQty);

        // ── Cập nhật PurchaseRequest status ───────────────────────────────
        boolean allReceived = purchaseRequest.getDetails().stream()
                .allMatch(d -> {
                    int recv = d.getReceivedQuantity() == null ? 0 : d.getReceivedQuantity();
                    return recv >= d.getQuantity();
                });
        purchaseRequest.setStatus(allReceived ? PurchaseStatus.RECEIVED : PurchaseStatus.PARTIAL_RECEIVED);
        purchaseRequestRepository.save(purchaseRequest);

        // ── Ghi InventoryTransaction ───────────────────────────────────────
        InventoryTransaction txn = buildTransaction(
                TransactionType.PURCHASE,
                null,         // from = null (mua từ bên ngoài)
                warehouse,
                "PurchaseRequest",
                purchaseRequestId);

        InventoryTransactionDetail txnDetail = buildTxnDetail(txn, item, null, location, receivedQty);
        txn.getDetails().add(txnDetail);
        transactionRepository.save(txn);

        log.info("Received goods [purchaseRequest={}, item={}, qty={}, warehouse={}]",
                purchaseRequest.getRequestNo(), item.getCode(), receivedQty, warehouse.getCode());
        notificationEventPublisher.publish(NotificationType.PURCHASE_RECEIVED,
                allReceived ? "Purchase fully received" : "Purchase partially received",
                purchaseRequest.getRequestNo() + " received " + receivedQty + " " + item.getCode() + ".",
                "PurchaseRequest", purchaseRequest.getId(),
                planningRequester(purchaseRequest.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PURCHASE_READ));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. COMPLETE ASSEMBLY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Assembly: FINISHED_COMPONENT list → SET item.
     *
     * Inventory movements:
     *   DEBIT:  mỗi component trên COMPONENT_WAREHOUSE -= (bomQty × assemblyQty)
     *   CREDIT: SET item trên SET_WAREHOUSE += assemblyQty
     *
     * Validate tất cả components đủ hàng TRƯỚC khi deduct bất kỳ component nào
     * → đảm bảo không có partial state.
     */
    @Override
    @Transactional
    public void completeAssembly(Long assemblyOrderId, Long warehouseId, Long locationId) {
        AssemblyOrder order = assemblyOrderRepository.findByIdForUpdate(assemblyOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_ORDER_NOT_FOUND));

        // Validate status
        if (order.getStatus() != AssemblyStatus.PENDING && order.getStatus() != AssemblyStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.ASSEMBLY_ORDER_INVALID_STATUS);
        }

        Item setItem      = order.getSetItem();
        int  assemblyQty  = order.getQuantity();

        Warehouse componentWarehouse = findActiveWarehouseByType(WarehouseType.COMPONENT_WAREHOUSE);
        
        Warehouse setWarehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (setWarehouse.getType() != WarehouseType.SET_WAREHOUSE) {
            throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
        }
        if (setWarehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_NOT_CONFIGURED);
        }
        
        Location setLocation = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        if (!setLocation.getWarehouse().getId().equals(setWarehouse.getId())) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }

        // ── BOM explosion: lấy danh sách components cần dùng ──────────────
        Map<Long, ComponentRequirement> componentRequirements = explodeLeafComponentRequirements(setItem, assemblyQty);
        if (componentRequirements.isEmpty()) {
            throw new AppException(ErrorCode.PLANNING_ENGINE_NO_BOM);
        }

        Map<Long, List<InventoryReservation>> reservationsByItem = new LinkedHashMap<>();

        // ── Validate TRƯỚC: tất cả components phải đủ tồn kho ─────────────
        // Nếu bất kỳ component nào thiếu → ném exception, không deduct gì cả
        for (ComponentRequirement requirement : componentRequirements.values()) {
            Item component     = requirement.item();
            int  requiredTotal = requirement.quantity();

            List<Inventory> componentInventories = inventoryRepository
                    .findByItemIdAndWarehouseId(component.getId(), componentWarehouse.getId());

            int totalAvailable = componentInventories.stream()
                    .mapToInt(Inventory::getAvailableQuantity)
                    .sum();

            List<InventoryReservation> reservations = findAssemblyReservations(
                    order, component, componentWarehouse);
            reservationsByItem.put(component.getId(), reservations);
            int totalReserved = reservations.stream()
                    .mapToInt(InventoryReservation::getQuantity)
                    .sum();

            if ((long) totalAvailable + totalReserved < requiredTotal) {
                log.warn("Assembly [orderId={}] insufficient stock for component [{}]: need={}, have={}",
                        assemblyOrderId, component.getCode(), requiredTotal, totalAvailable + totalReserved);
                throw new AppException(ErrorCode.ASSEMBLY_INSUFFICIENT_COMPONENTS);
            }
        }

        // ── DEBIT: trừ từng component khỏi COMPONENT_WAREHOUSE ────────────
        InventoryTransaction txn = buildTransaction(
                TransactionType.ASSEMBLY,
                componentWarehouse,
                setWarehouse,
                "AssemblyOrder",
                assemblyOrderId);

        for (ComponentRequirement requirement : componentRequirements.values()) {
            Item component     = requirement.item();
            int  requiredTotal = requirement.quantity();
            int  remaining     = requiredTotal;

            for (InventoryReservation reservation : reservationsByItem.getOrDefault(component.getId(), List.of())) {
                if (remaining <= 0) break;
                int consumeQty = Math.min(remaining, reservation.getQuantity());
                Inventory reservedInventory = consumeReservation(reservation, consumeQty);

                InventoryTransactionDetail debitDetail = buildTxnDetail(txn, component,
                        reservedInventory.getLocation(), null, consumeQty);
                txn.getDetails().add(debitDetail);
                remaining -= consumeQty;
            }

            // Deduct từng inventory record theo FIFO (first-available)
            List<Inventory> componentInventories = inventoryRepository
                    .findByItemIdAndWarehouseId(component.getId(), componentWarehouse.getId());

            for (Inventory inv : componentInventories) {
                if (remaining <= 0) break;
                int deduct = Math.min(remaining, inv.getAvailableQuantity());
                if (deduct <= 0) continue;
                inv.setAvailableQuantity(inv.getAvailableQuantity() - deduct);
                inv.setTotalQuantity(inv.getTotalQuantity() - deduct);
                inventoryRepository.save(inv);

                InventoryTransactionDetail debitDetail = buildTxnDetail(txn, component,
                        inv.getLocation(), null, deduct);
                txn.getDetails().add(debitDetail);

                remaining -= deduct;
            }
        }

        // ── CREDIT: cộng SET vào SET_WAREHOUSE ───────────────────────────
        // Tìm hoặc tạo inventory record cho SET item
        Inventory setInventory = findOrCreateInventory(setItem, setWarehouse, setLocation);
        setInventory.setTotalQuantity(setInventory.getTotalQuantity() + assemblyQty);
        setInventory.setAvailableQuantity(setInventory.getAvailableQuantity() + assemblyQty);
        inventoryRepository.save(setInventory);
        reserveProducedInventory(order.getPlanningDetail(), setInventory, assemblyQty);

        InventoryTransactionDetail creditDetail = buildTxnDetail(txn, setItem,
                null, setLocation, assemblyQty);
        txn.getDetails().add(creditDetail);
        transactionRepository.save(txn);

        // ── Cập nhật AssemblyOrder ─────────────────────────────────────────
        order.setStatus(AssemblyStatus.COMPLETED);
        assemblyOrderRepository.save(order);

        log.info("Assembly completed [orderId={}, setItem={}, qty={}]",
                assemblyOrderId, setItem.getCode(), assemblyQty);
        notificationEventPublisher.publish(NotificationType.ASSEMBLY_COMPLETED,
                "Assembly completed",
                order.getAssemblyNo() + " produced " + assemblyQty + " " + setItem.getCode() + ".",
                "AssemblyOrder", order.getId(), planningRequester(order.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.ASSEMBLY_READ));
    }

    @Override
    @Transactional
    public int releaseExpiredReservations() {
        List<InventoryReservation> expired = reservationRepository.findExpiredForUpdate(
                ReservationStatus.RESERVED, LocalDateTime.now());
        for (InventoryReservation reservation : expired) {
            releaseReservation(reservation.getId());
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }
        return expired.size();
    }

    private List<InventoryReservation> findAssemblyReservations(AssemblyOrder order,
                                                                 Item component,
                                                                 Warehouse componentWarehouse) {
        if (order.getPlanningDetail() == null) {
            return List.of();
        }
        return reservationRepository.findForConsumption(
                order.getPlanningDetail().getPlanning().getId(),
                component.getId(),
                componentWarehouse.getId(),
                ReservationStatus.RESERVED);
    }

    private Set<String> planningRequester(PlanningDetail detail) {
        if (detail == null || detail.getPlanning() == null
                || detail.getPlanning().getWorkshopRequest() == null) {
            return Set.of();
        }
        String username = detail.getPlanning().getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    private Inventory consumeReservation(InventoryReservation reservation, int quantity) {
        Inventory inventory = reservation.getInventory();
        if (inventory == null) {
            inventory = inventoryRepository
                    .findByItemIdAndWarehouseId(
                            reservation.getItem().getId(), reservation.getWarehouse().getId())
                    .stream()
                    .filter(candidate -> candidate.getReservedQuantity() >= quantity)
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ASSEMBLY_INSUFFICIENT_COMPONENTS));
            reservation.setInventory(inventory);
        }

        if (inventory.getReservedQuantity() < quantity || inventory.getTotalQuantity() < quantity) {
            throw new AppException(ErrorCode.ASSEMBLY_INSUFFICIENT_COMPONENTS);
        }

        inventory.setTotalQuantity(inventory.getTotalQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getTotalQuantity() - inventory.getReservedQuantity());
        inventoryRepository.save(inventory);

        PlanningDetail planningDetail = planningDetailRepository
                .findByIdForUpdate(reservation.getPlanningDetail().getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        if (planningDetail.getReservedQuantity() < quantity) {
            throw new AppException(ErrorCode.ASSEMBLY_INSUFFICIENT_COMPONENTS);
        }
        planningDetail.setReservedQuantity(planningDetail.getReservedQuantity() - quantity);
        planningDetailRepository.save(planningDetail);

        if (reservation.getQuantity() == quantity) {
            reservation.setStatus(ReservationStatus.CONSUMED);
            reservationRepository.save(reservation);
        } else {
            reservation.setQuantity(reservation.getQuantity() - quantity);
            reservationRepository.save(reservation);

            InventoryReservation consumedReservation = new InventoryReservation();
            consumedReservation.setPlanningDetail(reservation.getPlanningDetail());
            consumedReservation.setItem(reservation.getItem());
            consumedReservation.setWarehouse(reservation.getWarehouse());
            consumedReservation.setInventory(inventory);
            consumedReservation.setQuantity(quantity);
            consumedReservation.setStatus(ReservationStatus.CONSUMED);
            consumedReservation.setExpiredTime(reservation.getExpiredTime());
            reservationRepository.save(consumedReservation);
        }

        return inventory;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. READ: Get Reservations by PlanningDetail
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<InventoryReservationResponse> getReservationsByPlanningDetail(Long planningDetailId) {
        return reservationRepository.findByPlanningDetailId(planningDetailId).stream()
                .map(this::mapReservationToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tìm Inventory record phù hợp để reserve.
     * Nếu warehouseId được chỉ định → tìm trong warehouse đó.
     * Nếu không → quét tất cả COMPONENT_WAREHOUSE tìm record có đủ hàng.
     */
    private Map<Long, ComponentRequirement> explodeLeafComponentRequirements(Item setItem, int assemblyQty) {
        Map<Long, ComponentRequirement> requirements = new LinkedHashMap<>();
        Set<Long> path = new HashSet<>();
        path.add(setItem.getId());
        explodeLeafComponentRequirements(setItem, assemblyQty, requirements, path);
        return requirements;
    }

    private void explodeLeafComponentRequirements(Item parentItem,
                                                  int parentQty,
                                                  Map<Long, ComponentRequirement> requirements,
                                                  Set<Long> path) {
        List<Bom> children = bomRepository.findByParentItemId(parentItem.getId());
        if (children.isEmpty()) {
            requirements.merge(parentItem.getId(),
                    new ComponentRequirement(parentItem, parentQty),
                    (existing, next) -> new ComponentRequirement(
                            existing.item(), checkedPlanningAdd(existing.quantity(), next.quantity())));
            return;
        }

        for (Bom bom : children) {
            Item childItem = bom.getChildItem();
            if (path.contains(childItem.getId())) {
                throw new AppException(ErrorCode.BOM_CYCLE_DETECTED);
            }
            int childQty = checkedPlanningMultiply(parentQty, bom.getQuantity());
            path.add(childItem.getId());
            explodeLeafComponentRequirements(childItem, childQty, requirements, path);
            path.remove(childItem.getId());
        }
    }

    private int checkedPlanningMultiply(int left, int right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    private int checkedPlanningAdd(int left, int right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    private record ComponentRequirement(Item item, int quantity) {
    }

    private List<Inventory> findReservationCandidates(Item item, Long warehouseId) {
        WarehouseType expectedWarehouseType = item.getItemType() == ItemType.SET
                ? WarehouseType.SET_WAREHOUSE
                : WarehouseType.COMPONENT_WAREHOUSE;
        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            if (warehouse.getType() != expectedWarehouseType) {
                throw new AppException(ErrorCode.INVALID_WAREHOUSE_TYPE);
            }
            if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
                throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_NOT_CONFIGURED);
            }
            return inventoryRepository.findByItemIdAndWarehouseId(item.getId(), warehouseId);
        }
        return inventoryRepository.findByItemId(item.getId()).stream()
                .filter(inv -> inv.getWarehouse().getType() == expectedWarehouseType
                        && inv.getWarehouse().getStatus() == WarehouseStatus.ACTIVE)
                .collect(Collectors.toList());
    }


    /**
     * Tìm inventory record theo item + warehouse (lấy đầu tiên).
     * Ném INVENTORY_NOT_FOUND_FOR_ITEM nếu không tìm thấy.
     */
    private Inventory findInventoryOrThrow(Long itemId, Long warehouseId) {
        List<Inventory> records = inventoryRepository.findByItemIdAndWarehouseId(itemId, warehouseId);
        if (records.isEmpty()) {
            throw new AppException(ErrorCode.INVENTORY_NOT_FOUND_FOR_ITEM);
        }
        return records.get(0);
    }

    /**
     * Tìm warehouse đầu tiên theo type.
     * Ném WAREHOUSE_NOT_FOUND nếu không có.
     */
    private Warehouse findActiveWarehouseByType(WarehouseType type) {
        List<Warehouse> warehouses = warehouseRepository.findByTypeAndStatus(type, WarehouseStatus.ACTIVE);
        if (warehouses.size() != 1) {
            throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_NOT_CONFIGURED);
        }
        return warehouses.get(0);
    }

    private void reserveProducedInventory(PlanningDetail planningDetail, Inventory inventory, int producedQuantity) {
        if (planningDetail == null || producedQuantity <= 0) {
            return;
        }
        PlanningDetail lockedDetail = planningDetailRepository.findByIdForUpdate(planningDetail.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        if (lockedDetail.getPlanning().getStatus() != PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        int remainingRequired = Math.max(0,
                lockedDetail.getRequiredQuantity() - lockedDetail.getReservedQuantity());
        int reserveQuantity = Math.min(producedQuantity, remainingRequired);
        if (reserveQuantity <= 0) {
            return;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - reserveQuantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + reserveQuantity);
        inventoryRepository.save(inventory);

        lockedDetail.setReservedQuantity(lockedDetail.getReservedQuantity() + reserveQuantity);
        planningDetailRepository.save(lockedDetail);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setPlanningDetail(lockedDetail);
        reservation.setItem(lockedDetail.getItem());
        reservation.setWarehouse(inventory.getWarehouse());
        reservation.setInventory(inventory);
        reservation.setQuantity(reserveQuantity);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiredTime(LocalDateTime.now().plusHours(24));
        reservationRepository.save(reservation);
    }

    /**
     * Tìm hoặc tạo mới Inventory record cho item + warehouse + location.
     * Dùng khi nhận hàng (receiveGoods) hoặc cộng output của Recycle/Assembly.
     */
    private Inventory findOrCreateInventory(Item item, Warehouse warehouse, Location location) {
        if (location != null) {
            return inventoryRepository
                    .findByItemIdAndWarehouseIdAndLocationId(item.getId(), warehouse.getId(), location.getId())
                    .orElseGet(() -> createNewInventory(item, warehouse, location));
        }
        List<Inventory> existing = inventoryRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId());
        return existing.isEmpty() ? createNewInventory(item, warehouse, null) : existing.get(0);
    }

    /** Tạo Inventory record mới với quantity = 0. */
    private Inventory createNewInventory(Item item, Warehouse warehouse, Location location) {
        Inventory inv = new Inventory();
        inv.setItem(item);
        inv.setWarehouse(warehouse);
        inv.setLocation(location);
        inv.setTotalQuantity(0);
        inv.setReservedQuantity(0);
        inv.setAvailableQuantity(0);
        return inventoryRepository.save(inv);
    }

    /** Tạo InventoryTransaction header. */
    private InventoryTransaction buildTransaction(TransactionType type,
                                                   Warehouse from,
                                                   Warehouse to,
                                                   String refType,
                                                   Long refId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String txnNo = type.name().substring(0, 2) + "-" + dateStr + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        InventoryTransaction txn = new InventoryTransaction();
        txn.setTransactionNo(txnNo);
        txn.setTransactionType(type);
        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setFromWarehouse(from);
        txn.setToWarehouse(to);
        txn.setReferenceType(refType);
        txn.setReferenceId(refId);
        return txn;
    }

    /**
     * Tạo InventoryTransactionDetail.
     * locationFrom = kho xuất, locationTo = kho nhập.
     * Dùng null nếu không áp dụng.
     */
    private InventoryTransactionDetail buildTxnDetail(InventoryTransaction txn,
                                                       Item item,
                                                       Location locationFrom,
                                                       Location locationTo,
                                                       int quantity) {
        InventoryTransactionDetail detail = new InventoryTransactionDetail();
        detail.setTransaction(txn);
        detail.setItem(item);
        detail.setLocationFrom(locationFrom);
        detail.setLocationTo(locationTo);
        detail.setQuantity(quantity);
        return detail;
    }

    /** Map InventoryReservation entity → DTO. */
    private InventoryReservationResponse mapReservationToResponse(InventoryReservation reservation) {
        return InventoryReservationResponse.builder()
                .id(reservation.getId())
                .planningDetailId(reservation.getPlanningDetail() != null
                        ? reservation.getPlanningDetail().getId() : null)
                .itemId(reservation.getItem().getId())
                .itemCode(reservation.getItem().getCode())
                .warehouseId(reservation.getWarehouse().getId())
                .warehouseCode(reservation.getWarehouse().getCode())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .expiredTime(reservation.getExpiredTime())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
