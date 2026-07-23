package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.BatchInfoRequest;
import com.restapi.wmsservice.dto.response.BatchInfoResponse;
import com.restapi.wmsservice.service.BatchInfoService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/batch-info")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BatchInfoController {

    BatchInfoService batchInfoService;

    @PostMapping
    public ApiResponse<BatchInfoResponse> createBatchInfo(@RequestBody @Valid BatchInfoRequest request) {
        return ApiResponse.<BatchInfoResponse>builder()
                .result(batchInfoService.createBatchInfo(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BatchInfoResponse> getBatchInfo(@PathVariable Long id) {
        return ApiResponse.<BatchInfoResponse>builder()
                .result(batchInfoService.getBatchInfo(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BatchInfoResponse>> getAllBatchInfos() {
        return ApiResponse.<List<BatchInfoResponse>>builder()
                .result(batchInfoService.getAllBatchInfos())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BatchInfoResponse> updateBatchInfo(@PathVariable Long id, @RequestBody @Valid BatchInfoRequest request) {
        return ApiResponse.<BatchInfoResponse>builder()
                .result(batchInfoService.updateBatchInfo(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBatchInfo(@PathVariable Long id) {
        batchInfoService.deleteBatchInfo(id);
        return ApiResponse.<String>builder()
                .result("BatchInfo deleted successfully")
                .build();
    }
}
