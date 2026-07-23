package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.PurchaseRequestRequest;
import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.PurchaseRequestResponse;
import com.restapi.wmsservice.enums.PurchaseStatus;
import com.restapi.wmsservice.service.PurchaseRequestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseRequestController {

    PurchaseRequestService purchaseRequestService;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @PostMapping
    public ApiResponse<PurchaseRequestResponse> create(@RequestBody @Valid PurchaseRequestRequest request) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseRequestResponse> get(@PathVariable Long id) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.get(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PurchaseRequestResponse>> getAll() {
        return ApiResponse.<List<PurchaseRequestResponse>>builder()
                .result(purchaseRequestService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseRequestResponse> update(@PathVariable Long id, @RequestBody @Valid PurchaseRequestRequest request) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseRequestService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    // ── Business Flow ─────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseRequestResponse> approve(@PathVariable Long id) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.approvePurchase(id))
                .build();
    }

    @PostMapping("/{id}/order")
    public ApiResponse<PurchaseRequestResponse> order(@PathVariable Long id) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.orderPurchase(id))
                .build();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseRequestResponse> cancel(@PathVariable Long id) {
        return ApiResponse.<PurchaseRequestResponse>builder()
                .result(purchaseRequestService.cancelPurchase(id))
                .build();
    }

    @GetMapping("/by-status")
    public ApiResponse<List<PurchaseRequestResponse>> getByStatus(@RequestParam PurchaseStatus status) {
        return ApiResponse.<List<PurchaseRequestResponse>>builder()
                .result(purchaseRequestService.getByStatus(status))
                .build();
    }
}
