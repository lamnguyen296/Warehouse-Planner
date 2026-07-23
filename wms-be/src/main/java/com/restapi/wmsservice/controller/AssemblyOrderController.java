package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.AssemblyOrderRequest;
import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.AssemblyOrderResponse;
import com.restapi.wmsservice.service.AssemblyOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assembly-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssemblyOrderController {

    AssemblyOrderService assemblyOrderService;

    @PostMapping
    public ApiResponse<AssemblyOrderResponse> create(@RequestBody @Valid AssemblyOrderRequest request) {
        return ApiResponse.<AssemblyOrderResponse>builder()
                .result(assemblyOrderService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AssemblyOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.<AssemblyOrderResponse>builder()
                .result(assemblyOrderService.get(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<AssemblyOrderResponse>> getAll() {
        return ApiResponse.<List<AssemblyOrderResponse>>builder()
                .result(assemblyOrderService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AssemblyOrderResponse> update(@PathVariable Long id, @RequestBody @Valid AssemblyOrderRequest request) {
        return ApiResponse.<AssemblyOrderResponse>builder()
                .result(assemblyOrderService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assemblyOrderService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    // ── Business Flow Endpoints ────────────────────────────────────────────

    /** Bắt đầu Assembly: PENDING → IN_PROGRESS */
    @PostMapping("/{id}/start")
    public ApiResponse<AssemblyOrderResponse> start(@PathVariable Long id) {
        return ApiResponse.<AssemblyOrderResponse>builder()
                .result(assemblyOrderService.startAssembly(id))
                .build();
    }

    /** Hủy Assembly: PENDING/IN_PROGRESS → FAILED */
    @PostMapping("/{id}/cancel")
    public ApiResponse<AssemblyOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.<AssemblyOrderResponse>builder()
                .result(assemblyOrderService.cancelAssembly(id))
                .build();
    }
}

