package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.WarehouseRequest;
import com.restapi.wmsservice.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse create(WarehouseRequest request);
    List<WarehouseResponse> getAll();
    WarehouseResponse getById(Long id);
    WarehouseResponse update(Long id, WarehouseRequest request);
    void delete(Long id);
}
