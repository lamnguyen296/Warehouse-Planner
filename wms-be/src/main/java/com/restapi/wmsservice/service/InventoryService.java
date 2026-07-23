package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.InventoryRequest;
import com.restapi.wmsservice.dto.response.InventoryResponse;

import java.util.List;

public interface InventoryService {
    InventoryResponse createInventory(InventoryRequest request);
    InventoryResponse getInventory(Long id);
    List<InventoryResponse> getAllInventories();
    InventoryResponse updateInventory(Long id, InventoryRequest request);
    void deleteInventory(Long id);
}
