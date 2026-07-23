package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.ItemRequest;
import com.restapi.wmsservice.dto.response.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    ItemResponse create(ItemRequest request);
    List<ItemResponse> getAll();
    ItemResponse getById(Long id);
    ItemResponse update(Long id, ItemRequest request);
    ItemResponse updateImage(Long id, MultipartFile file);
    ItemResponse deleteImage(Long id);
    void delete(Long id);
}
