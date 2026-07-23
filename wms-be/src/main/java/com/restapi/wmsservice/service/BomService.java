package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.BomRequest;
import com.restapi.wmsservice.dto.response.BomResponse;

import java.util.List;

public interface BomService {
    BomResponse create(BomRequest request);
    List<BomResponse> getAll();
    List<BomResponse> getByParentItemId(Long parentItemId);
    BomResponse getById(Long id);
    BomResponse update(Long id, BomRequest request);
    void delete(Long id);
}
