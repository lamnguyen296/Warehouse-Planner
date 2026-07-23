package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.TransferOrderRequest;
import com.restapi.wmsservice.dto.response.TransferOrderResponse;
import com.restapi.wmsservice.entity.*;
import com.restapi.wmsservice.enums.*;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.TransferOrderMapper;
import com.restapi.wmsservice.repository.*;
import com.restapi.wmsservice.service.TransferOrderService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Phase 6 – Flow 4: Transfer Flow Implementation.
 *
 * Business Flow:
 *  CREATE  → TransferOrder (PENDING)
 *  EXECUTE → DEBIT fromWarehouse inventory → status = IN_TRANSIT
 *              Ghi InventoryTransaction (TRANSFER, PENDING)
 *  COMPLETE → CREDIT toWarehouse inventory → status = COMPLETED
 *              Update InventoryTransaction → COMPLETED
 *  CANCEL  → Nếu PENDING: chỉ cancel. Nếu IN_TRANSIT: rollback DEBIT → CANCELLED
 *
 * Transaction Boundary:
 *  - Mỗi operation trong 1 @Transactional riêng.
 *  - executeTransfer và completeTransfer đều atomic:
 *    nếu inventory update fail → rollback toàn bộ trạng thái.
 *
 * Optimistic Locking:
 *  - Inventory entity có @Version → JPA tự handle concurrent update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferOrderServiceImpl implements TransferOrderService {

    TransferOrderRepository transferOrderRepository;
    WarehouseRepository warehouseRepository;
    ItemRepository itemRepository;
    InventoryRepository inventoryRepository;
    InventoryTransactionRepository transactionRepository;
    LocationRepository locationRepository;
    TransferOrderMapper transferOrderMapper;
    PlanningRepository planningRepository;
    PlanningDetailRepository planningDetailRepository;
    InventoryReservationRepository reservationRepository;
    NotificationEventPublisher notificationEventPublisher;

    

    // ═══════════════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TransferOrderResponse create(TransferOrderRequest request) {
        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Guard: không chuyển cùng warehouse
        if (fromWarehouse.getId().equals(toWarehouse.getId())) {
            throw new AppException(ErrorCode.TRANSFER_SAME_WAREHOUSE);
        }
        validateActiveWarehouses(fromWarehouse, toWarehouse);

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Planning planning = resolveAndValidatePlanning(request.getPlanningId(), fromWarehouse,
                toWarehouse, item, request.getQuantity(), null);

        TransferOrder order = transferOrderMapper.toEntity(request);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        order.setTransferNo("TO-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        order.setFromWarehouse(fromWarehouse);
        order.setToWarehouse(toWarehouse);
        order.setItem(item);
        order.setPlanning(planning);
        order.setStatus(TransferStatus.PENDING);

        order = transferOrderRepository.save(order);
        log.info("Created TransferOrder [no={}, item={}, qty={}, from={}, to={}]",
                order.getTransferNo(), item.getCode(), order.getQuantity(),
                fromWarehouse.getCode(), toWarehouse.getCode());

        return transferOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferOrderResponse get(Long id) {
        return transferOrderRepository.findById(id)
                .map(transferOrderMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderResponse> getAll() {
        return transferOrderRepository.findAll().stream()
                .map(transferOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TransferOrderResponse update(Long id, TransferOrderRequest request) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));

        if (order.getStatus() != TransferStatus.PENDING) {
            throw new AppException(ErrorCode.TRANSFER_ORDER_INVALID_STATUS);
        }
        if (order.getPlanning() != null && !order.getPlanning().getId().equals(request.getPlanningId())) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        if (fromWarehouse.getId().equals(toWarehouse.getId())) {
            throw new AppException(ErrorCode.TRANSFER_SAME_WAREHOUSE);
        }
        validateActiveWarehouses(fromWarehouse, toWarehouse);

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Planning planning = resolveAndValidatePlanning(request.getPlanningId(), fromWarehouse,
                toWarehouse, item, request.getQuantity(), order.getId());

        transferOrderMapper.updateEntity(order, request);
        order.setFromWarehouse(fromWarehouse);
        order.setToWarehouse(toWarehouse);
        order.setItem(item);
        order.setPlanning(planning);

        return transferOrderMapper.toResponse(transferOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));
        if (order.getStatus() != TransferStatus.PENDING) {
            throw new AppException(ErrorCode.TRANSFER_ORDER_INVALID_STATUS);
        }
        transferOrderRepository.deleteById(id);
        log.info("Deleted TransferOrder [id={}]", id);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUSINESS FLOW
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Execute Transfer: PENDING → IN_TRANSIT.
     *
     * Inventory movement:
     *   DEBIT: item trên fromWarehouse.availableQuantity -= qty
     *   (hàng đang trên đường vận chuyển, chưa tới toWarehouse)
     *
     * Transaction:
     *   Tạo InventoryTransaction (type=TRANSFER, status=PENDING) để track hành trình.
     */
    @Override
    @Transactional
    public TransferOrderResponse executeTransfer(Long id) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));

        if (order.getStatus() != TransferStatus.PENDING) {
            throw new AppException(ErrorCode.TRANSFER_ORDER_INVALID_STATUS);
        }

        Item item           = order.getItem();
        Warehouse fromWh    = order.getFromWarehouse();
        int transferQty     = order.getQuantity();

        // Ghi InventoryTransaction (TRANSFER đang di chuyển)
        InventoryTransaction txn = buildTransferTransaction(
                TransactionStatus.PENDING,
                fromWh,
                order.getToWarehouse(),
                order.getId(),
                transactionType(order));

        // DEBIT: trừ available inventory tại fromWarehouse và lấy danh sách detail chính xác theo từng location
        List<InventoryTransactionDetail> debitDetails = order.getPlanning() == null
                ? deductInventory(item, fromWh, transferQty, txn)
                : deductPlannedInventory(order, txn);
        txn.getDetails().addAll(debitDetails);
        transactionRepository.save(txn);

        // Cập nhật TransferOrder status
        order.setStatus(TransferStatus.IN_TRANSIT);
        order = transferOrderRepository.save(order);

        log.info("Transfer executed [no={}, item={}, qty={}, from={}] → IN_TRANSIT",
                order.getTransferNo(), item.getCode(), transferQty, fromWh.getCode());
        publishTransferEvent(order, NotificationType.TRANSFER_IN_TRANSIT,
                "Transfer in transit", " is moving to " + order.getToWarehouse().getCode() + ".");

        return transferOrderMapper.toResponse(order);
    }

    /**
     * Complete Transfer: IN_TRANSIT → COMPLETED.
     *
     * Inventory movement:
     *   CREDIT: item trên toWarehouse.availableQuantity += qty
     *           toWarehouse.totalQuantity += qty
     *
     * Transaction: Update InventoryTransaction → COMPLETED, thêm credit detail.
     *
     * @param id                 ID của TransferOrder
     * @param warehouseLocationId Location cụ thể trong toWarehouse (null = auto-chọn first)
     */
    @Override
    @Transactional
    public TransferOrderResponse completeTransfer(Long id, Long warehouseLocationId) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));

        if (order.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new AppException(ErrorCode.TRANSFER_ORDER_INVALID_STATUS);
        }

        Item item        = order.getItem();
        Warehouse toWh   = order.getToWarehouse();
        int transferQty  = order.getQuantity();

        // Xác định location đích
        Location toLocation = null;
        if (warehouseLocationId != null) {
            toLocation = locationRepository.findById(warehouseLocationId)
                    .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
            if (!toLocation.getWarehouse().getId().equals(toWh.getId())) {
                throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
            }
        } else {
            toLocation = getFirstLocation(toWh.getId());
        }
        if (toLocation == null) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }

        // CREDIT: cộng inventory tại toWarehouse (upsert)
        creditInventory(item, toWh, toLocation, transferQty);

        // Tìm InventoryTransaction PENDING cho TransferOrder này và update → COMPLETED
        // (InventoryTransaction được tạo ở executeTransfer)
        TransactionType transactionType = transactionType(order);
        InventoryTransaction txn = transactionRepository
                .findFirstByTransactionTypeAndStatusAndReferenceTypeAndReferenceId(
                        transactionType, TransactionStatus.PENDING, "TransferOrder", order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        InventoryTransactionDetail creditDetail = buildTxnDetail(txn, item, null, toLocation, transferQty);
        txn.getDetails().add(creditDetail);
        txn.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(txn);

        // Cập nhật TransferOrder status
        order.setStatus(TransferStatus.COMPLETED);
        order = transferOrderRepository.save(order);
        boolean planningCompleted = completePlanningIfDelivered(order.getPlanning());

        log.info("Transfer completed [no={}, item={}, qty={}, to={}]",
                order.getTransferNo(), item.getCode(), transferQty, toWh.getCode());
        publishTransferEvent(order, NotificationType.TRANSFER_COMPLETED,
                "Transfer completed", " arrived at " + toWh.getCode() + ".");
        if (planningCompleted) {
            Planning planning = order.getPlanning();
            notificationEventPublisher.publish(NotificationType.PLANNING_COMPLETED,
                    "Planning completed",
                    planning.getPlanningNo() + " and its workshop request are complete.",
                    "Planning", planning.getId(), planningRequester(planning),
                    Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_READ));
        }

        return transferOrderMapper.toResponse(order);
    }

    /**
     * Cancel Transfer.
     *
     * Nếu PENDING: chỉ đổi status → CANCELLED (chưa có inventory movement).
     * Nếu IN_TRANSIT: rollback DEBIT (hoàn trả lại available cho fromWarehouse) → CANCELLED.
     */
    @Override
    @Transactional
    public TransferOrderResponse cancelTransfer(Long id) {
        TransferOrder order = transferOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSFER_ORDER_NOT_FOUND));

        if (order.getStatus() == TransferStatus.COMPLETED
                || order.getStatus() == TransferStatus.CANCELLED) {
            throw new AppException(ErrorCode.TRANSFER_ORDER_INVALID_STATUS);
        }

        Item item        = order.getItem();
        Warehouse fromWh = order.getFromWarehouse();
        int transferQty  = order.getQuantity();

        if (order.getStatus() == TransferStatus.IN_TRANSIT) {
            // Rollback DEBIT: hoàn trả available về fromWarehouse
            final Long orderId = order.getId();
            TransactionType transactionType = transactionType(order);
            InventoryTransaction txn = transactionRepository
                    .findFirstByTransactionTypeAndStatusAndReferenceTypeAndReferenceId(
                            transactionType, TransactionStatus.PENDING, "TransferOrder", orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

            for (InventoryTransactionDetail detail : txn.getDetails()) {
                if (detail.getLocationFrom() != null) {
                    Inventory restored = creditInventory(
                            detail.getItem(), fromWh, detail.getLocationFrom(), detail.getQuantity());
                    restorePlanningReservation(order.getPlanning(), restored, detail.getQuantity());
                }
            }
            txn.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(txn);

            log.info("Transfer cancelled (IN_TRANSIT rollback) [no={}, item={}, qty={}, fromWh restored]",
                    order.getTransferNo(), item.getCode(), transferQty);
        }

        order.setStatus(TransferStatus.CANCELLED);
        order = transferOrderRepository.save(order);

        log.info("TransferOrder cancelled [no={}]", order.getTransferNo());
        publishTransferEvent(order, NotificationType.TRANSFER_CANCELLED,
                "Transfer cancelled", " was cancelled.");
        return transferOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderResponse> getByStatus(TransferStatus status) {
        return transferOrderRepository.findByStatus(status).stream()
                .map(transferOrderMapper::toResponse)
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Trừ available inventory tại warehouse cho item.
     * Deduct FIFO (first available record first).
     * Trả về danh sách InventoryTransactionDetail tương ứng với các kho/location đã trừ.
     */
    private List<InventoryTransactionDetail> deductInventory(Item item, Warehouse warehouse, int quantity, InventoryTransaction txn) {
        List<Inventory> records = inventoryRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId());

        int totalAvailable = records.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        if (totalAvailable < quantity) {
            throw new AppException(ErrorCode.TRANSFER_INSUFFICIENT_STOCK);
        }

        List<InventoryTransactionDetail> details = new java.util.ArrayList<>();
        int remaining = quantity;
        for (Inventory inv : records) {
            if (remaining <= 0) break;
            int deduct = Math.min(remaining, inv.getAvailableQuantity());
            if (deduct <= 0) continue;
            inv.setAvailableQuantity(inv.getAvailableQuantity() - deduct);
            inv.setTotalQuantity(inv.getTotalQuantity() - deduct);
            inventoryRepository.save(inv);
            
            details.add(buildTxnDetail(txn, item, inv.getLocation(), null, deduct));
            remaining -= deduct;
        }
        return details;
    }

    private List<InventoryTransactionDetail> deductPlannedInventory(TransferOrder order,
                                                                     InventoryTransaction transaction) {
        Planning planning = order.getPlanning();
        Item item = order.getItem();
        Warehouse warehouse = order.getFromWarehouse();
        inventoryRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId());
        List<InventoryReservation> reservations = reservationRepository.findForConsumption(
                planning.getId(), item.getId(), warehouse.getId(), ReservationStatus.RESERVED);
        int reservedQuantity = reservations.stream().mapToInt(InventoryReservation::getQuantity).sum();
        if (reservedQuantity < order.getQuantity()) {
            throw new AppException(ErrorCode.TRANSFER_INSUFFICIENT_STOCK);
        }

        List<InventoryTransactionDetail> details = new java.util.ArrayList<>();
        int remaining = order.getQuantity();
        for (InventoryReservation reservation : reservations) {
            if (remaining <= 0) break;
            int quantity = Math.min(remaining, reservation.getQuantity());
            Inventory inventory = consumeReservation(reservation, quantity);
            details.add(buildTxnDetail(transaction, item, inventory.getLocation(), null, quantity));
            remaining -= quantity;
        }
        return details;
    }

    private Inventory consumeReservation(InventoryReservation reservation, int quantity) {
        Inventory inventory = reservation.getInventory();
        if (inventory == null || inventory.getReservedQuantity() < quantity
                || inventory.getTotalQuantity() < quantity) {
            throw new AppException(ErrorCode.TRANSFER_INSUFFICIENT_STOCK);
        }
        inventory.setTotalQuantity(inventory.getTotalQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getTotalQuantity() - inventory.getReservedQuantity());
        inventoryRepository.save(inventory);

        PlanningDetail detail = planningDetailRepository.findByIdForUpdate(
                        reservation.getPlanningDetail().getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        detail.setReservedQuantity(detail.getReservedQuantity() - quantity);
        planningDetailRepository.save(detail);

        if (reservation.getQuantity() == quantity) {
            reservation.setStatus(ReservationStatus.CONSUMED);
            reservationRepository.save(reservation);
        } else {
            reservation.setQuantity(reservation.getQuantity() - quantity);
            reservationRepository.save(reservation);

            InventoryReservation consumed = new InventoryReservation();
            consumed.setPlanningDetail(detail);
            consumed.setItem(reservation.getItem());
            consumed.setWarehouse(reservation.getWarehouse());
            consumed.setInventory(inventory);
            consumed.setQuantity(quantity);
            consumed.setStatus(ReservationStatus.CONSUMED);
            consumed.setExpiredTime(reservation.getExpiredTime());
            reservationRepository.save(consumed);
        }
        return inventory;
    }

    private Planning resolveAndValidatePlanning(Long planningId,
                                                Warehouse fromWarehouse,
                                                Warehouse toWarehouse,
                                                Item item,
                                                int quantity,
                                                Long excludedTransferId) {
        if (planningId == null) {
            return null;
        }
        Planning planning = planningRepository.findByIdForUpdate(planningId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
        if (planning.getStatus() != PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        if (fromWarehouse.getType() != WarehouseType.SET_WAREHOUSE
                || toWarehouse.getType() != WarehouseType.WORKSHOP
                || fromWarehouse.getStatus() != WarehouseStatus.ACTIVE
                || toWarehouse.getStatus() != WarehouseStatus.ACTIVE
                || item.getItemType() != ItemType.SET) {
            throw new AppException(ErrorCode.INVALID_WAREHOUSE_TYPE);
        }

        List<PlanningDetail> matchingDetails = planningDetailRepository
                .findByPlanningIdAndItemId(planningId, item.getId());
        if (matchingDetails.size() != 1) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }
        int committedQuantity = transferOrderRepository.findByPlanningId(planningId).stream()
                .filter(existing -> !existing.getId().equals(excludedTransferId))
                .filter(existing -> existing.getItem().getId().equals(item.getId()))
                .filter(existing -> existing.getStatus() != TransferStatus.CANCELLED)
                .mapToInt(TransferOrder::getQuantity)
                .sum();
        if ((long) committedQuantity + quantity > matchingDetails.get(0).getRequiredQuantity()) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }
        return planning;
    }

    private void validateActiveWarehouses(Warehouse fromWarehouse, Warehouse toWarehouse) {
        if (fromWarehouse.getStatus() != WarehouseStatus.ACTIVE
                || toWarehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_NOT_CONFIGURED);
        }
    }

    private TransactionType transactionType(TransferOrder order) {
        return order.getToWarehouse().getType() == WarehouseType.WORKSHOP
                ? TransactionType.ISSUE
                : TransactionType.TRANSFER;
    }

    private void restorePlanningReservation(Planning planning, Inventory inventory, int quantity) {
        if (planning == null) {
            return;
        }
        List<PlanningDetail> matchingDetails = planningDetailRepository
                .findByPlanningIdAndItemId(planning.getId(), inventory.getItem().getId());
        if (matchingDetails.size() != 1) {
            throw new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND);
        }
        PlanningDetail detail = planningDetailRepository.findByIdForUpdate(matchingDetails.get(0).getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
        detail.setReservedQuantity(detail.getReservedQuantity() + quantity);
        planningDetailRepository.save(detail);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setPlanningDetail(detail);
        reservation.setItem(inventory.getItem());
        reservation.setWarehouse(inventory.getWarehouse());
        reservation.setInventory(inventory);
        reservation.setQuantity(quantity);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
    }

    private boolean completePlanningIfDelivered(Planning planning) {
        if (planning == null) {
            return false;
        }
        Planning lockedPlanning = planningRepository.findByIdForUpdate(planning.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
        if (lockedPlanning.getStatus() != PlanningStatus.EXECUTING) {
            return false;
        }
        Map<Long, Integer> deliveredByItem = transferOrderRepository.findByPlanningId(planning.getId()).stream()
                .filter(transfer -> transfer.getStatus() == TransferStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        transfer -> transfer.getItem().getId(),
                        Collectors.summingInt(TransferOrder::getQuantity)));
        boolean delivered = lockedPlanning.getDetails().stream()
                .filter(detail -> detail.getItem().getItemType() == ItemType.SET)
                .allMatch(detail -> deliveredByItem.getOrDefault(detail.getItem().getId(), 0)
                        >= detail.getRequiredQuantity());
        if (delivered) {
            lockedPlanning.setStatus(PlanningStatus.COMPLETED);
            lockedPlanning.getWorkshopRequest().setStatus(RequestStatus.COMPLETED);
            planningRepository.save(lockedPlanning);
            return true;
        }
        return false;
    }

    private void publishTransferEvent(TransferOrder order, NotificationType type,
                                      String title, String messageSuffix) {
        notificationEventPublisher.publish(type, title, order.getTransferNo() + messageSuffix,
                "TransferOrder", order.getId(), planningRequester(order.getPlanning()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.TRANSFER_READ));
    }

    private Set<String> planningRequester(Planning planning) {
        if (planning == null || planning.getWorkshopRequest() == null) {
            return Set.of();
        }
        String username = planning.getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    /**
     * Cộng available + total inventory tại warehouse cho item (upsert).
     * Retry cho Optimistic Lock.
     */
    private Inventory creditInventory(Item item, Warehouse warehouse, Location location, int quantity) {
        if (location == null || !location.getWarehouse().getId().equals(warehouse.getId())) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }
        Inventory inventory = inventoryRepository
                .findByItemIdAndWarehouseIdAndLocationId(item.getId(), warehouse.getId(), location.getId())
                .orElseGet(() -> createInventory(item, warehouse, location));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryRepository.save(inventory);
        return inventory;
    }

    private Inventory createInventory(Item item, Warehouse warehouse, Location location) {
        Inventory inv = new Inventory();
        inv.setItem(item);
        inv.setWarehouse(warehouse);
        inv.setLocation(location);
        inv.setTotalQuantity(0);
        inv.setReservedQuantity(0);
        inv.setAvailableQuantity(0);
        return inventoryRepository.save(inv);
    }

    /**
     * Lấy location đầu tiên của warehouse (dùng khi không chỉ định location cụ thể).
     * Trả null nếu warehouse không có location nào.
     */
    private Location getFirstLocation(Long warehouseId) {
        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);
        return locations.isEmpty() ? null : locations.get(0);
    }

    /** Tạo InventoryTransaction header cho TRANSFER. */
    private InventoryTransaction buildTransferTransaction(TransactionStatus status,
                                                           Warehouse fromWh,
                                                           Warehouse toWh,
                                                           Long transferOrderId,
                                                           TransactionType transactionType) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String txnNo = "TR-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        InventoryTransaction txn = new InventoryTransaction();
        txn.setTransactionNo(txnNo);
        txn.setTransactionType(transactionType);
        txn.setStatus(status);
        txn.setFromWarehouse(fromWh);
        txn.setToWarehouse(toWh);
        txn.setReferenceType("TransferOrder");
        txn.setReferenceId(transferOrderId);
        return txn;
    }

    /** Tạo InventoryTransactionDetail. */
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
}
