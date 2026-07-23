package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.InventoryRequest;
import com.restapi.wmsservice.dto.response.InventoryResponse;
import com.restapi.wmsservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryService inventoryService;

    @PostMapping
    public ApiResponse<InventoryResponse> createInventory(@RequestBody @Valid InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.createInventory(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable Long id) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.getInventory(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<InventoryResponse>> getAllInventories() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .result(inventoryService.getAllInventories())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryResponse> updateInventory(@PathVariable Long id, @RequestBody @Valid InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.updateInventory(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ApiResponse.<String>builder()
                .result("Inventory deleted successfully")
                .build();
    }
}
