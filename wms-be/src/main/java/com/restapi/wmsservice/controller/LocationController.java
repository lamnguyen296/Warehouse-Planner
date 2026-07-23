package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.LocationRequest;
import com.restapi.wmsservice.dto.response.LocationResponse;
import com.restapi.wmsservice.service.LocationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {

    LocationService locationService;

    @PostMapping
    public ApiResponse<LocationResponse> create(@RequestBody @Valid LocationRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .result(locationService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<LocationResponse>> getAll(@RequestParam(required = false) Long warehouseId) {
        List<LocationResponse> result = (warehouseId != null) 
            ? locationService.getByWarehouseId(warehouseId) 
            : locationService.getAll();
            
        return ApiResponse.<List<LocationResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<LocationResponse> getById(@PathVariable Long id) {
        return ApiResponse.<LocationResponse>builder()
                .result(locationService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<LocationResponse> update(@PathVariable Long id, @RequestBody @Valid LocationRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .result(locationService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
