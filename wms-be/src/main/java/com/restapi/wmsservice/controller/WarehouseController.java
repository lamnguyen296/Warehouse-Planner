package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.WarehouseRequest;
import com.restapi.wmsservice.dto.response.WarehouseResponse;
import com.restapi.wmsservice.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseController {

    WarehouseService warehouseService;

    @PostMapping
    public ApiResponse<WarehouseResponse> create(@RequestBody @Valid WarehouseRequest request) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> getAll() {
        return ApiResponse.<List<WarehouseResponse>>builder()
                .result(warehouseService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> getById(@PathVariable Long id) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @RequestBody @Valid WarehouseRequest request) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(warehouseService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
