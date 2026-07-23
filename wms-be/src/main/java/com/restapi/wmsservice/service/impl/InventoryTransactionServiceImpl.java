package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.dto.request.InventoryTransactionDetailRequest;
import com.restapi.wmsservice.dto.request.InventoryTransactionRequest;
import com.restapi.wmsservice.dto.response.InventoryTransactionResponse;
import com.restapi.wmsservice.entity.*;
import com.restapi.wmsservice.enums.TransactionStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.InventoryTransactionMapper;
import com.restapi.wmsservice.repository.*;
import com.restapi.wmsservice.service.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    InventoryTransactionRepository transactionRepository;
    InventoryTransactionMapper transactionMapper;
    WarehouseRepository warehouseRepository;
    ItemRepository itemRepository;
    BatchInfoRepository batchInfoRepository;
    LocationRepository locationRepository;

    @Override
    @Transactional
    public InventoryTransactionResponse createTransaction(InventoryTransactionRequest request) {
        InventoryTransaction transaction = transactionMapper.toTransaction(request);
        transaction.setTransactionNo("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setStatus(TransactionStatus.COMPLETED); // default status for now

        if (request.getFromWarehouseId() != null) {
            Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setFromWarehouse(fromWarehouse);
        }

        if (request.getToWarehouseId() != null) {
            Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            transaction.setToWarehouse(toWarehouse);
        }

        for (InventoryTransactionDetailRequest detailReq : request.getDetails()) {
            InventoryTransactionDetail detail = new InventoryTransactionDetail();
            Item item = itemRepository.findById(detailReq.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
            detail.setItem(item);

            if (detailReq.getBatchId() != null) {
                BatchInfo batchInfo = batchInfoRepository.findById(detailReq.getBatchId())
                        .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));
                detail.setBatchInfo(batchInfo);
            }

            if (detailReq.getLocationFrom() != null) {
                Location locFrom = locationRepository.findById(detailReq.getLocationFrom())
                        .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
                detail.setLocationFrom(locFrom);
            }

            if (detailReq.getLocationTo() != null) {
                Location locTo = locationRepository.findById(detailReq.getLocationTo())
                        .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
                detail.setLocationTo(locTo);
            }

            detail.setQuantity(detailReq.getQuantity());
            detail.setTransaction(transaction);
            transaction.getDetails().add(detail);
        }

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public InventoryTransactionResponse getTransaction(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    @Override
    public List<InventoryTransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        transactionRepository.deleteById(id);
    }
}
