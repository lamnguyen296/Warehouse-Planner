package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.ItemRequest;
import com.restapi.wmsservice.dto.response.ItemResponse;
import com.restapi.wmsservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {

    ItemService itemService;

    @PostMapping
    public ApiResponse<ItemResponse> create(@RequestBody @Valid ItemRequest request) {
        return ApiResponse.<ItemResponse>builder()
                .result(itemService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ItemResponse>> getAll() {
        return ApiResponse.<List<ItemResponse>>builder()
                .result(itemService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ItemResponse>builder()
                .result(itemService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemResponse> update(@PathVariable Long id, @RequestBody @Valid ItemRequest request) {
        return ApiResponse.<ItemResponse>builder()
                .result(itemService.update(id, request))
                .build();
    }

    @PutMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ApiResponse<ItemResponse> updateImage(@PathVariable Long id,
                                                  @RequestPart("file") MultipartFile file) {
        return ApiResponse.<ItemResponse>builder()
                .result(itemService.updateImage(id, file))
                .build();
    }

    @DeleteMapping("/{id}/image")
    public ApiResponse<ItemResponse> deleteImage(@PathVariable Long id) {
        return ApiResponse.<ItemResponse>builder()
                .result(itemService.deleteImage(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
