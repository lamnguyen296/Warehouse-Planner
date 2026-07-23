package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.dto.request.BatchInfoRequest;
import com.restapi.wmsservice.dto.response.BatchInfoResponse;
import com.restapi.wmsservice.entity.BatchInfo;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.BatchInfoMapper;
import com.restapi.wmsservice.repository.BatchInfoRepository;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.service.BatchInfoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BatchInfoServiceImpl implements BatchInfoService {

    BatchInfoRepository batchInfoRepository;
    BatchInfoMapper batchInfoMapper;
    ItemRepository itemRepository;

    @Override
    @Transactional
    public BatchInfoResponse createBatchInfo(BatchInfoRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        BatchInfo batchInfo = batchInfoMapper.toBatchInfo(request);
        batchInfo.setItem(item);

        return batchInfoMapper.toBatchInfoResponse(batchInfoRepository.save(batchInfo));
    }

    @Override
    public BatchInfoResponse getBatchInfo(Long id) {
        return batchInfoRepository.findById(id)
                .map(batchInfoMapper::toBatchInfoResponse)
                .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));
    }

    @Override
    public List<BatchInfoResponse> getAllBatchInfos() {
        return batchInfoRepository.findAll().stream()
                .map(batchInfoMapper::toBatchInfoResponse)
                .toList();
    }

    @Override
    @Transactional
    public BatchInfoResponse updateBatchInfo(Long id, BatchInfoRequest request) {
        BatchInfo batchInfo = batchInfoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        batchInfoMapper.updateBatchInfo(batchInfo, request);
        batchInfo.setItem(item);

        return batchInfoMapper.toBatchInfoResponse(batchInfoRepository.save(batchInfo));
    }

    @Override
    @Transactional
    public void deleteBatchInfo(Long id) {
        if (!batchInfoRepository.existsById(id)) {
            throw new AppException(ErrorCode.BATCH_NOT_FOUND);
        }
        batchInfoRepository.deleteById(id);
    }
}
