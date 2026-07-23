package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.dto.request.InventoryRequest;
import com.restapi.wmsservice.dto.response.InventoryResponse;
import com.restapi.wmsservice.entity.Inventory;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.Location;
import com.restapi.wmsservice.entity.Warehouse;
import com.restapi.wmsservice.entity.InventoryTransaction;
import com.restapi.wmsservice.entity.InventoryTransactionDetail;
import com.restapi.wmsservice.enums.TransactionStatus;
import com.restapi.wmsservice.enums.TransactionType;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.InventoryMapper;
import com.restapi.wmsservice.repository.InventoryRepository;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.LocationRepository;
import com.restapi.wmsservice.repository.WarehouseRepository;
import com.restapi.wmsservice.repository.InventoryTransactionRepository;
import com.restapi.wmsservice.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;
    InventoryMapper inventoryMapper;
    WarehouseRepository warehouseRepository;
    LocationRepository locationRepository;
    ItemRepository itemRepository;
    InventoryTransactionRepository transactionRepository;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        validateInventoryQuantities(request);
        validateLocationWarehouse(location, warehouse);
        if (request.getReservedQuantity() != 0) {
            throw new AppException(ErrorCode.RESERVED_QUANTITY_SYSTEM_MANAGED);
        }

        Inventory inventory = inventoryMapper.toInventory(request);
        inventory.setWarehouse(warehouse);
        inventory.setLocation(location);
        inventory.setItem(item);

        inventory = inventoryRepository.save(inventory);
        recordAdjustment(inventory, request.getTotalQuantity());
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long id) {
        return inventoryRepository.findById(id)
                .map(inventoryMapper::toInventoryResponse)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toInventoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        validateInventoryQuantities(request);
        validateLocationWarehouse(location, warehouse);
        if (!inventory.getWarehouse().getId().equals(warehouse.getId())
                || !inventory.getLocation().getId().equals(location.getId())
                || !inventory.getItem().getId().equals(item.getId())) {
            throw new AppException(ErrorCode.INVENTORY_IDENTITY_IMMUTABLE);
        }
        if (!inventory.getReservedQuantity().equals(request.getReservedQuantity())) {
            throw new AppException(ErrorCode.RESERVED_QUANTITY_SYSTEM_MANAGED);
        }
        int adjustment = request.getTotalQuantity() - inventory.getTotalQuantity();

        inventoryMapper.updateInventory(inventory, request);
        inventory.setWarehouse(warehouse);
        inventory.setLocation(location);
        inventory.setItem(item);

        inventory = inventoryRepository.save(inventory);
        recordAdjustment(inventory, adjustment);
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        if (inventory.getTotalQuantity() != 0 || inventory.getReservedQuantity() != 0) {
            throw new AppException(ErrorCode.INVENTORY_NOT_EMPTY);
        }
        inventoryRepository.deleteById(id);
    }

    private void validateInventoryQuantities(InventoryRequest request) {
        if (request.getReservedQuantity() > request.getTotalQuantity()
                || !request.getAvailableQuantity().equals(request.getTotalQuantity() - request.getReservedQuantity())) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
    }

    private void validateLocationWarehouse(Location location, Warehouse warehouse) {
        if (!location.getWarehouse().getId().equals(warehouse.getId())) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND);
        }
    }

    private void recordAdjustment(Inventory inventory, int adjustment) {
        if (adjustment == 0) {
            return;
        }
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNo("ADJ-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        transaction.setTransactionType(TransactionType.ADJUST);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReferenceType("Inventory");
        transaction.setReferenceId(inventory.getId());
        if (adjustment > 0) {
            transaction.setToWarehouse(inventory.getWarehouse());
        } else {
            transaction.setFromWarehouse(inventory.getWarehouse());
        }

        InventoryTransactionDetail detail = new InventoryTransactionDetail();
        detail.setTransaction(transaction);
        detail.setItem(inventory.getItem());
        detail.setQuantity(Math.abs(adjustment));
        if (adjustment > 0) {
            detail.setLocationTo(inventory.getLocation());
        } else {
            detail.setLocationFrom(inventory.getLocation());
        }
        transaction.getDetails().add(detail);
        transactionRepository.save(transaction);
    }
}
