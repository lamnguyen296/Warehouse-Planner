package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.InventoryTransactionResponse;
import com.restapi.wmsservice.dto.response.InventoryTransactionAttachmentResponse;
import com.restapi.wmsservice.service.InventoryTransactionAttachmentService;
import com.restapi.wmsservice.service.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inventory-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionController {

    InventoryTransactionService transactionService;
    InventoryTransactionAttachmentService attachmentService;

    @GetMapping("/{id}")
    public ApiResponse<InventoryTransactionResponse> getTransaction(@PathVariable Long id) {
        return ApiResponse.<InventoryTransactionResponse>builder()
                .result(transactionService.getTransaction(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<InventoryTransactionResponse>> getAllTransactions() {
        return ApiResponse.<List<InventoryTransactionResponse>>builder()
                .result(transactionService.getAllTransactions())
                .build();
    }

    @GetMapping("/{id}/attachments")
    public ApiResponse<List<InventoryTransactionAttachmentResponse>> getAttachments(@PathVariable Long id) {
        return ApiResponse.<List<InventoryTransactionAttachmentResponse>>builder()
                .result(attachmentService.getAll(id))
                .build();
    }

    @PostMapping(value = "/{id}/attachments", consumes = "multipart/form-data")
    public ApiResponse<InventoryTransactionAttachmentResponse> uploadAttachment(
            @PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.<InventoryTransactionAttachmentResponse>builder()
                .result(attachmentService.upload(id, file))
                .build();
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        attachmentService.delete(id, attachmentId);
        return ApiResponse.<Void>builder().build();
    }

}
