package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.BatchInfoRequest;
import com.restapi.wmsservice.dto.response.BatchInfoResponse;

import java.util.List;

public interface BatchInfoService {
    BatchInfoResponse createBatchInfo(BatchInfoRequest request);
    BatchInfoResponse getBatchInfo(Long id);
    List<BatchInfoResponse> getAllBatchInfos();
    BatchInfoResponse updateBatchInfo(Long id, BatchInfoRequest request);
    void deleteBatchInfo(Long id);
}
