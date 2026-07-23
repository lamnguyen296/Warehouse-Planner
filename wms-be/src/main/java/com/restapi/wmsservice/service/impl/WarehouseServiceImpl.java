package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.WarehouseRequest;
import com.restapi.wmsservice.dto.response.WarehouseResponse;
import com.restapi.wmsservice.entity.Warehouse;
import com.restapi.wmsservice.enums.WarehouseStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.WarehouseMapper;
import com.restapi.wmsservice.repository.WarehouseRepository;
import com.restapi.wmsservice.service.WarehouseService;
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
public class WarehouseServiceImpl implements WarehouseService {

    WarehouseRepository warehouseRepository;
    WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.WAREHOUSE_CODE_EXISTED);
        }
        validateSingleActiveWarehouse(request.getType(), request.getStatus(), null);
        Warehouse warehouse = warehouseMapper.toWarehouse(request);
        warehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll().stream()
                .map(warehouseMapper::toWarehouseResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return warehouseMapper.toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        
        if (!warehouse.getCode().equals(request.getCode()) && warehouseRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.WAREHOUSE_CODE_EXISTED);
        }
        validateSingleActiveWarehouse(request.getType(), request.getStatus(), id);

        warehouseMapper.updateWarehouse(warehouse, request);
        warehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new AppException(ErrorCode.WAREHOUSE_NOT_FOUND);
        }
        warehouseRepository.deleteById(id);
    }

    private void validateSingleActiveWarehouse(com.restapi.wmsservice.enums.WarehouseType type,
                                               WarehouseStatus status,
                                               Long excludedId) {
        if (status != WarehouseStatus.ACTIVE) {
            return;
        }
        boolean activeExists = excludedId == null
                ? warehouseRepository.existsByTypeAndStatus(type, WarehouseStatus.ACTIVE)
                : warehouseRepository.existsByTypeAndStatusAndIdNot(type, WarehouseStatus.ACTIVE, excludedId);
        if (activeExists) {
            throw new AppException(ErrorCode.ACTIVE_WAREHOUSE_TYPE_EXISTED);
        }
    }
}
