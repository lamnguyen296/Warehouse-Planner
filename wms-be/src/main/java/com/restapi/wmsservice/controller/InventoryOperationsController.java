package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.CompleteRecycleRequest;
import com.restapi.wmsservice.dto.request.ReceiveGoodsRequest;
import com.restapi.wmsservice.dto.request.CompleteAssemblyRequest;
import com.restapi.wmsservice.dto.response.InventoryReservationResponse;
import com.restapi.wmsservice.service.InventoryOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 6 – Flow 3: Inventory Operations Controller.
 *
 * Endpoints:
 *  POST /inventory-ops/reserve                             – Reserve tồn kho
 *  DELETE /inventory-ops/reservations/{id}                 – Release reservation
 *  POST /inventory-ops/recycle-orders/{id}/complete        – Hoàn thành Recycle
 *  POST /inventory-ops/purchase-requests/{id}/receive      – Nhận hàng mua
 *  POST /inventory-ops/assembly-orders/{id}/complete       – Hoàn thành Assembly
 *  GET /inventory-ops/reservations/by-planning-detail/{id} – Lấy reservations
 */
@RestController
@RequestMapping("/inventory-ops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryOperationsController {

    InventoryOperationsService inventoryOperationsService;

    // ── Reserve / Release ────────────────────────────────────────────────

    /**
     * Reserve tồn kho cho một PlanningDetail.
     * POST /inventory-ops/reserve
     * Body: { planningDetailId, warehouseId (optional), quantity }
     */
    @PostMapping("/reserve")
    public ApiResponse<List<InventoryReservationResponse>> reserveInventory(
            @RequestParam Long planningDetailId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam @Min(1) int quantity) {
        return ApiResponse.<List<InventoryReservationResponse>>builder()
                .result(inventoryOperationsService.reserveInventory(planningDetailId, warehouseId, quantity))
                .build();
    }

    /**
     * Release (hủy) một reservation.
     * DELETE /inventory-ops/reservations/{id}
     */
    @DeleteMapping("/reservations/{id}")
    public ApiResponse<String> releaseReservation(@PathVariable Long id) {
        inventoryOperationsService.releaseReservation(id);
        return ApiResponse.<String>builder()
                .result("Reservation released successfully")
                .build();
    }

    @PostMapping("/reservations/expire")
    public ApiResponse<Integer> releaseExpiredReservations() {
        return ApiResponse.<Integer>builder()
                .result(inventoryOperationsService.releaseExpiredReservations())
                .build();
    }

    /**
     * Lấy tất cả reservations của một PlanningDetail.
     * GET /inventory-ops/reservations/by-planning-detail/{planningDetailId}
     */
    @GetMapping("/reservations/by-planning-detail/{planningDetailId}")
    public ApiResponse<List<InventoryReservationResponse>> getReservationsByPlanningDetail(
            @PathVariable Long planningDetailId) {
        return ApiResponse.<List<InventoryReservationResponse>>builder()
                .result(inventoryOperationsService.getReservationsByPlanningDetail(planningDetailId))
                .build();
    }

    // ── Recycle ───────────────────────────────────────────────────────────

    /**
     * Hoàn thành quá trình Recycle: RAW → FINISHED_COMPONENT.
     * POST /inventory-ops/recycle-orders/{id}/complete
     * Body: { actualYield }
     */
    @PostMapping("/recycle-orders/{id}/complete")
    public ApiResponse<String> completeRecycle(
            @PathVariable Long id,
            @RequestBody @Valid CompleteRecycleRequest request) {
        inventoryOperationsService.completeRecycle(id, request.getActualYield());
        return ApiResponse.<String>builder()
                .result("Recycle order completed successfully")
                .build();
    }

    // ── Purchase / Receive Goods ──────────────────────────────────────────

    /**
     * Nhận hàng cho một PurchaseRequest (hỗ trợ partial receipt).
     * POST /inventory-ops/purchase-requests/{id}/receive
     * Body: { purchaseDetailId, receivedQty, warehouseId, locationId }
     */
    @PostMapping("/purchase-requests/{id}/receive")
    public ApiResponse<String> receiveGoods(
            @PathVariable Long id,
            @RequestBody @Valid ReceiveGoodsRequest request) {
        inventoryOperationsService.receiveGoods(
                id,
                request.getPurchaseDetailId(),
                request.getReceivedQty(),
                request.getWarehouseId(),
                request.getLocationId());
        return ApiResponse.<String>builder()
                .result("Goods received successfully")
                .build();
    }

    // ── Assembly ──────────────────────────────────────────────────────────

    /**
     * Hoàn thành Assembly: FINISHED_COMPONENT list → SET.
     * POST /inventory-ops/assembly-orders/{id}/complete
     */
    @PostMapping("/assembly-orders/{id}/complete")
    public ApiResponse<String> completeAssembly(
            @PathVariable Long id,
            @RequestBody @Valid CompleteAssemblyRequest request) {
        inventoryOperationsService.completeAssembly(id, request.getWarehouseId(), request.getLocationId());
        return ApiResponse.<String>builder()
                .result("Assembly order completed successfully")
                .build();
    }
}
