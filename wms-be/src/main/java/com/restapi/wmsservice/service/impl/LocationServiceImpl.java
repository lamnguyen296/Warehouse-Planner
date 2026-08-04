package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.LocationRequest;
import com.restapi.wmsservice.dto.response.LocationResponse;
import com.restapi.wmsservice.entity.Location;
import com.restapi.wmsservice.entity.Warehouse;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.LocationMapper;
import com.restapi.wmsservice.repository.LocationRepository;
import com.restapi.wmsservice.repository.WarehouseRepository;
import com.restapi.wmsservice.service.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LocationServiceImpl implements LocationService {

    LocationRepository locationRepository;
    WarehouseRepository warehouseRepository;
    LocationMapper locationMapper;

    @Override
    @Transactional
    public LocationResponse create(LocationRequest request) {
        if (locationRepository.existsByWarehouseIdAndCode(request.getWarehouseId(), request.getCode())) {
            throw new AppException(ErrorCode.LOCATION_CODE_EXISTED);
        }
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Location location = locationMapper.toLocation(request);
        location.setWarehouse(warehouse);
        
        location = locationRepository.save(location);
        return locationMapper.toLocationResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAll() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toLocationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getByWarehouseId(Long warehouseId) {
        return locationRepository.findByWarehouseId(warehouseId).stream()
                .map(locationMapper::toLocationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        return locationMapper.toLocationResponse(location);
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        if (!location.getWarehouse().getId().equals(request.getWarehouseId())) {
            throw new AppException(ErrorCode.MASTER_DATA_IDENTITY_IMMUTABLE);
        }
        if (locationRepository.existsByWarehouseIdAndCodeAndIdNot(
                request.getWarehouseId(), request.getCode(), id)) {
            throw new AppException(ErrorCode.LOCATION_CODE_EXISTED);
        }

        locationMapper.updateLocation(location, request);
        
        location = locationRepository.save(location);
        return locationMapper.toLocationResponse(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }
        locationRepository.deleteById(id);
    }
}
