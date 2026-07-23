package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.BatchInfoRequest;
import com.restapi.wmsservice.dto.response.BatchInfoResponse;
import com.restapi.wmsservice.entity.BatchInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BatchInfoMapper {

    @Mapping(target = "item", ignore = true)
    BatchInfo toBatchInfo(BatchInfoRequest request);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    BatchInfoResponse toBatchInfoResponse(BatchInfo batchInfo);

    @Mapping(target = "item", ignore = true)
    void updateBatchInfo(@MappingTarget BatchInfo batchInfo, BatchInfoRequest request);
}
