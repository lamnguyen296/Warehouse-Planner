package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.TransferOrderRequest;
import com.restapi.wmsservice.dto.response.TransferOrderResponse;
import com.restapi.wmsservice.enums.TransferStatus;

import java.util.List;

public interface TransferOrderService {
    TransferOrderResponse create(TransferOrderRequest request);
    TransferOrderResponse get(Long id);
    List<TransferOrderResponse> getAll();
    TransferOrderResponse update(Long id, TransferOrderRequest request);
    void delete(Long id);

    // ── Business Flow (Phase 6 – Flow 4) ──────────────────────────────────

    /**
     * Bắt đầu vận chuyển: PENDING → IN_TRANSIT.
     * Tại bước này:
     *  - DEBIT inventory từ fromWarehouse (trừ available)
     *  - Ghi InventoryTransaction (TRANSFER, status=PENDING)
     *  - Chuyển TransferOrder sang IN_TRANSIT
     */
    TransferOrderResponse executeTransfer(Long id);

    /**
     * Hoàn tất vận chuyển: IN_TRANSIT → COMPLETED.
     * Tại bước này:
     *  - CREDIT inventory vào toWarehouse (cộng available)
     *  - Ghi InventoryTransaction (TRANSFER, status=COMPLETED)
     *  - Chuyển TransferOrder sang COMPLETED
     *
     * @param warehouseLocationId Location trong toWarehouse để nhập hàng (optional)
     */
    TransferOrderResponse completeTransfer(Long id, Long warehouseLocationId);

    /**
     * Hủy TransferOrder: PENDING → CANCELLED.
     * Nếu đã IN_TRANSIT → rollback inventory DEBIT về fromWarehouse.
     */
    TransferOrderResponse cancelTransfer(Long id);

    /** Lấy TransferOrders theo status. */
    List<TransferOrderResponse> getByStatus(TransferStatus status);
}
