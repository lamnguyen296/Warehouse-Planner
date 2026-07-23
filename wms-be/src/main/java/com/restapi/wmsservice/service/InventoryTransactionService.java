package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.InventoryTransactionRequest;
import com.restapi.wmsservice.dto.response.InventoryTransactionResponse;

import java.util.List;

public interface InventoryTransactionService {
    InventoryTransactionResponse createTransaction(InventoryTransactionRequest request);
    InventoryTransactionResponse getTransaction(Long id);
    List<InventoryTransactionResponse> getAllTransactions();
    void deleteTransaction(Long id);
}
