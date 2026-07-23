package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.response.InventoryTransactionAttachmentResponse;
import com.restapi.wmsservice.dto.response.StoredMedia;
import com.restapi.wmsservice.entity.InventoryTransaction;
import com.restapi.wmsservice.entity.InventoryTransactionAttachment;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.InventoryTransactionAttachmentRepository;
import com.restapi.wmsservice.repository.InventoryTransactionRepository;
import com.restapi.wmsservice.service.InventoryTransactionAttachmentService;
import com.restapi.wmsservice.service.MediaStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionAttachmentServiceImpl implements InventoryTransactionAttachmentService {

    InventoryTransactionRepository transactionRepository;
    InventoryTransactionAttachmentRepository attachmentRepository;
    MediaStorageService mediaStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionAttachmentResponse> getAll(Long transactionId) {
        ensureTransactionExists(transactionId);
        return attachmentRepository.findAllByTransactionIdOrderByCreatedAtDesc(transactionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryTransactionAttachmentResponse upload(Long transactionId, MultipartFile file) {
        InventoryTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        StoredMedia uploaded = mediaStorageService.uploadImage(file, "movements/" + transactionId);

        InventoryTransactionAttachment attachment = new InventoryTransactionAttachment();
        attachment.setTransaction(transaction);
        attachment.setPublicId(uploaded.publicId());
        attachment.setSecureUrl(uploaded.secureUrl());
        attachment.setOriginalFilename(safeFilename(file.getOriginalFilename()));
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(uploaded.fileSize());
        attachment.setUploadedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        try {
            return toResponse(attachmentRepository.saveAndFlush(attachment));
        } catch (RuntimeException exception) {
            mediaStorageService.deleteNow(uploaded.publicId());
            throw exception;
        }
    }

    @Override
    @Transactional
    public void delete(Long transactionId, Long attachmentId) {
        InventoryTransactionAttachment attachment = attachmentRepository
                .findByIdAndTransactionId(attachmentId, transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTACHMENT_NOT_FOUND));
        attachmentRepository.delete(attachment);
        mediaStorageService.deleteAfterCommit(attachment.getPublicId());
    }

    private void ensureTransactionExists(Long transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image";
        }
        String filename = originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }

    private InventoryTransactionAttachmentResponse toResponse(InventoryTransactionAttachment attachment) {
        return InventoryTransactionAttachmentResponse.builder()
                .id(attachment.getId())
                .transactionId(attachment.getTransaction().getId())
                .secureUrl(attachment.getSecureUrl())
                .originalFilename(attachment.getOriginalFilename())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .uploadedBy(attachment.getUploadedBy())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
