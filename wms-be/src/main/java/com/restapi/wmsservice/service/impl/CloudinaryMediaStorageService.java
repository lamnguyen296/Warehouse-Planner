package com.restapi.wmsservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.restapi.wmsservice.configuration.CloudinaryProperties;
import com.restapi.wmsservice.dto.response.StoredMedia;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.service.MediaStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryMediaStorageService implements MediaStorageService {

    static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    Cloudinary cloudinary;
    CloudinaryProperties properties;

    @Override
    public StoredMedia uploadImage(MultipartFile file, String subfolder) {
        validate(file);
        ensureConfigured();

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", properties.getFolder() + "/" + subfolder,
                    "resource_type", "image",
                    "unique_filename", true,
                    "overwrite", false));

            Object uploadedBytes = result.get("bytes");
            long fileSize = uploadedBytes instanceof Number number ? number.longValue() : file.getSize();
            return new StoredMedia(
                    String.valueOf(result.get("public_id")),
                    String.valueOf(result.get("secure_url")),
                    fileSize);
        } catch (IOException | RuntimeException exception) {
            log.warn("Cloud media upload failed for subfolder={}", subfolder, exception);
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteNow(String publicId) {
        if (!properties.isConfigured() || !StringUtils.hasText(publicId)) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException | RuntimeException exception) {
            log.warn("Cloud media cleanup failed for publicId={}", publicId, exception);
        }
    }

    @Override
    public void deleteAfterCommit(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteNow(publicId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteNow(publicId);
            }
        });
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_MEDIA_FILE);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new AppException(ErrorCode.MEDIA_FILE_TOO_LARGE);
        }
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new AppException(ErrorCode.MEDIA_STORAGE_NOT_CONFIGURED);
        }
    }
}
