package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.ItemRequest;
import com.restapi.wmsservice.dto.response.ItemResponse;
import com.restapi.wmsservice.dto.response.StoredMedia;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.ItemMapper;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.service.ItemService;
import com.restapi.wmsservice.service.MediaStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemServiceImpl implements ItemService {

    ItemRepository itemRepository;
    ItemMapper itemMapper;
    MediaStorageService mediaStorageService;

    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {
        if (itemRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.ITEM_CODE_EXISTED);
        }
        Item item = itemMapper.toItem(request);
        item = itemRepository.save(item);
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getItemType() != request.getItemType()) {
            throw new AppException(ErrorCode.MASTER_DATA_IDENTITY_IMMUTABLE);
        }

        if (!item.getCode().equals(request.getCode()) && itemRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.ITEM_CODE_EXISTED);
        }

        itemMapper.updateItem(item, request);
        item = itemRepository.save(item);
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse updateImage(Long id, MultipartFile file) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        StoredMedia uploaded = mediaStorageService.uploadImage(file, "items");
        String previousPublicId = item.getImagePublicId();

        try {
            item.setImagePublicId(uploaded.publicId());
            item.setImageUrl(uploaded.secureUrl());
            item = itemRepository.saveAndFlush(item);
        } catch (RuntimeException exception) {
            mediaStorageService.deleteNow(uploaded.publicId());
            throw exception;
        }

        mediaStorageService.deleteAfterCommit(previousPublicId);
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse deleteImage(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        String publicId = item.getImagePublicId();
        item.setImagePublicId(null);
        item.setImageUrl(null);
        item = itemRepository.save(item);
        mediaStorageService.deleteAfterCommit(publicId);
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        itemRepository.delete(item);
        mediaStorageService.deleteAfterCommit(item.getImagePublicId());
    }
}
