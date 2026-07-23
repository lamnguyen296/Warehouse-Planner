package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.TransferOrderRequest;
import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.TransferOrderResponse;
import com.restapi.wmsservice.enums.TransferStatus;
import com.restapi.wmsservice.service.TransferOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 5 CRUD + Phase 6 Flow 4 Business Endpoints.
 *
 * Business endpoints:
 *  POST /transfer-orders/{id}/execute     – Execute: PENDING → IN_TRANSIT (DEBIT fromWarehouse)
 *  POST /transfer-orders/{id}/complete    – Complete: IN_TRANSIT → COMPLETED (CREDIT toWarehouse)
 *  POST /transfer-orders/{id}/cancel      – Cancel: PENDING/IN_TRANSIT → CANCELLED (rollback if needed)
 *  GET  /transfer-orders/by-status?status – Lấy orders theo status
 */
@RestController
@RequestMapping("/transfer-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferOrderController {

    TransferOrderService transferOrderService;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @PostMapping
    public ApiResponse<TransferOrderResponse> create(@RequestBody @Valid TransferOrderRequest request) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.get(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TransferOrderResponse>> getAll() {
        return ApiResponse.<List<TransferOrderResponse>>builder()
                .result(transferOrderService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TransferOrderResponse> update(@PathVariable Long id,
                                                      @RequestBody @Valid TransferOrderRequest request) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        transferOrderService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    // ── Business Flow Endpoints ────────────────────────────────────────────

    /**
     * Bắt đầu vận chuyển: PENDING → IN_TRANSIT.
     * Trừ inventory tại fromWarehouse.
     * POST /transfer-orders/{id}/execute
     */
    @PostMapping("/{id}/execute")
    public ApiResponse<TransferOrderResponse> execute(@PathVariable Long id) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.executeTransfer(id))
                .build();
    }

    /**
     * Hoàn tất vận chuyển: IN_TRANSIT → COMPLETED.
     * Cộng inventory tại toWarehouse.
     * POST /transfer-orders/{id}/complete?locationId={optional}
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<TransferOrderResponse> complete(
            @PathVariable Long id,
            @RequestParam(required = false) Long locationId) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.completeTransfer(id, locationId))
                .build();
    }

    /**
     * Hủy TransferOrder.
     * Nếu IN_TRANSIT → rollback inventory DEBIT về fromWarehouse.
     * POST /transfer-orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<TransferOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.<TransferOrderResponse>builder()
                .result(transferOrderService.cancelTransfer(id))
                .build();
    }

    /**
     * Lấy TransferOrders theo status.
     * GET /transfer-orders/by-status?status=PENDING
     */
    @GetMapping("/by-status")
    public ApiResponse<List<TransferOrderResponse>> getByStatus(@RequestParam TransferStatus status) {
        return ApiResponse.<List<TransferOrderResponse>>builder()
                .result(transferOrderService.getByStatus(status))
                .build();
    }
}
