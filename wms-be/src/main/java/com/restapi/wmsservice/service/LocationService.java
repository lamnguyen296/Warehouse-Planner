package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.LocationRequest;
import com.restapi.wmsservice.dto.response.LocationResponse;

import java.util.List;

public interface LocationService {
    LocationResponse create(LocationRequest request);
    List<LocationResponse> getAll();
    List<LocationResponse> getByWarehouseId(Long warehouseId);
    LocationResponse getById(Long id);
    LocationResponse update(Long id, LocationRequest request);
    void delete(Long id);
}
