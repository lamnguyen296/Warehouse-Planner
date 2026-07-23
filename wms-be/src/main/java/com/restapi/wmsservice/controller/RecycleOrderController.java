package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.RecycleOrderRequest;
import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.RecycleOrderResponse;
import com.restapi.wmsservice.service.RecycleOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recycle-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecycleOrderController {

    RecycleOrderService recycleOrderService;

    @PostMapping
    public ApiResponse<RecycleOrderResponse> create(@RequestBody @Valid RecycleOrderRequest request) {
        return ApiResponse.<RecycleOrderResponse>builder()
                .result(recycleOrderService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RecycleOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.<RecycleOrderResponse>builder()
                .result(recycleOrderService.get(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RecycleOrderResponse>> getAll() {
        return ApiResponse.<List<RecycleOrderResponse>>builder()
                .result(recycleOrderService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<RecycleOrderResponse> update(@PathVariable Long id, @RequestBody @Valid RecycleOrderRequest request) {
        return ApiResponse.<RecycleOrderResponse>builder()
                .result(recycleOrderService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recycleOrderService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    // ── Business Flow Endpoints ────────────────────────────────────────────

    /** Bắt đầu Recycle: PENDING → IN_PROGRESS */
    @PostMapping("/{id}/start")
    public ApiResponse<RecycleOrderResponse> start(@PathVariable Long id) {
        return ApiResponse.<RecycleOrderResponse>builder()
                .result(recycleOrderService.startRecycle(id))
                .build();
    }

    /** Hủy Recycle: PENDING/IN_PROGRESS → FAILED */
    @PostMapping("/{id}/cancel")
    public ApiResponse<RecycleOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.<RecycleOrderResponse>builder()
                .result(recycleOrderService.cancelRecycle(id))
                .build();
    }
}

