package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.response.StoredMedia;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    StoredMedia uploadImage(MultipartFile file, String subfolder);

    void deleteNow(String publicId);

    void deleteAfterCommit(String publicId);
}
