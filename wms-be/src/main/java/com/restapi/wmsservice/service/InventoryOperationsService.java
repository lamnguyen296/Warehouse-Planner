package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.response.InventoryReservationResponse;

import java.util.List;

/**
 * Phase 6 – Flow 3: Inventory Operations Service.
 *
 * Tập trung toàn bộ business logic liên quan đến:
 *  - Reserve / Release Inventory (với Optimistic Locking)
 *  - Complete Recycle   (RAW → FINISHED_COMPONENT)
 *  - Receive Goods      (Purchase → COMPONENT_WAREHOUSE)
 *  - Complete Assembly  (FINISHED_COMPONENT → SET)
 *
 * Mỗi operation đều:
 *  - Dùng @Transactional để đảm bảo atomicity
 *  - Dùng @Version (Optimistic Locking) để chống Lost Update
 *  - Ghi InventoryTransaction + InventoryTransactionDetail để audit trail
 */
public interface InventoryOperationsService {

    /**
     * Reserve tồn kho cho một PlanningDetail.
     *
     * Logic:
     *  1. Tìm Inventory records của item trên COMPONENT_WAREHOUSE
     *  2. Kiểm tra availableQuantity >= qty
     *  3. Trừ availableQuantity, cộng reservedQuantity (optimistic lock)
     *  4. Tạo InventoryReservation record
     *
     * @param planningDetailId ID của PlanningDetail cần reserve
     * @param warehouseId      Warehouse cụ thể để reserve (null = tự chọn warehouse có hàng)
     * @param quantity         Số lượng cần reserve
     * @return InventoryReservationResponse
     */
    List<InventoryReservationResponse> reserveInventory(Long planningDetailId, Long warehouseId, int quantity);

    void reserveRecycleInput(Long recycleOrderId);

    void releaseRecycleInput(Long recycleOrderId);

    /**
     * Release reservation – hoàn trả lại availableQuantity.
     *
     * @param reservationId ID của InventoryReservation cần release
     */
    void releaseReservation(Long reservationId);

    int releaseExpiredReservations();

    /**
     * Complete Recycle: xử lý RecycleOrder PENDING → COMPLETED.
     *
     * Logic:
     *  1. Validate RecycleOrder status = PENDING hoặc IN_PROGRESS
     *  2. Trừ fromItem (RAW_COMPONENT) từ COMPONENT_WAREHOUSE
     *  3. Cộng toItem (FINISHED_COMPONENT) vào COMPONENT_WAREHOUSE (actualYield)
     *  4. Cập nhật RecycleOrder.status = COMPLETED, finishTime = now
     *  5. Ghi InventoryTransaction type=RECYCLING
     *
     * @param recycleOrderId ID của RecycleOrder
     * @param actualYield    Số lượng FINISHED_COMPONENT thực tế tạo ra
     */
    void completeRecycle(Long recycleOrderId, int actualYield);

    /**
     * Receive Goods: nhận hàng cho PurchaseRequest (một phần hoặc toàn bộ).
     *
     * Logic:
     *  1. Validate PurchaseRequest status = ORDERED hoặc PARTIAL_RECEIVED
     *  2. Cộng receivedQty vào inventory COMPONENT_WAREHOUSE (tìm/tạo inventory record)
     *  3. Cập nhật PurchaseRequestDetail.receivedQuantity
     *  4. Cập nhật PurchaseRequest.status (PARTIAL_RECEIVED hoặc RECEIVED)
     *  5. Ghi InventoryTransaction type=PURCHASE
     *
     * @param purchaseRequestId  ID của PurchaseRequest
     * @param purchaseDetailId   ID của PurchaseRequestDetail cụ thể
     * @param receivedQty        Số lượng thực nhận
     * @param warehouseId        Warehouse nhập hàng
     * @param locationId         Location nhập hàng
     */
    void receiveGoods(Long purchaseRequestId, Long purchaseDetailId, int receivedQty,
                      Long warehouseId, Long locationId);

    /**
     * Complete Assembly: xử lý AssemblyOrder PENDING → COMPLETED.
     *
     * Logic:
     *  1. Validate AssemblyOrder status = PENDING hoặc IN_PROGRESS
     *  2. BOM explosion: lấy danh sách components cần cho SET
     *  3. Validate tất cả components đủ tồn kho trên COMPONENT_WAREHOUSE
     *  4. Trừ từng component khỏi COMPONENT_WAREHOUSE (qty × assemblyQty)
     *  5. Cộng SET item vào SET_WAREHOUSE
     *  6. Cập nhật AssemblyOrder.status = COMPLETED
     *  7. Ghi InventoryTransaction type=ASSEMBLY
     *
     * @param assemblyOrderId ID của AssemblyOrder
     */
    void completeAssembly(Long assemblyOrderId, Long warehouseId, Long locationId);

    /**
     * Lấy tất cả reservations của một PlanningDetail.
     */
    List<InventoryReservationResponse> getReservationsByPlanningDetail(Long planningDetailId);
}
