package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.InventoryTransactionDetailRequest;
import com.restapi.wmsservice.dto.response.InventoryTransactionDetailResponse;
import com.restapi.wmsservice.entity.InventoryTransactionDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryTransactionDetailMapper {

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "batchInfo", ignore = true)
    @Mapping(target = "locationFrom", ignore = true)
    @Mapping(target = "locationTo", ignore = true)
    InventoryTransactionDetail toDetail(InventoryTransactionDetailRequest request);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "batchInfo.id", target = "batchId")
    @Mapping(source = "batchInfo.batchNo", target = "batchNo")
    @Mapping(source = "locationFrom.id", target = "locationFrom")
    @Mapping(source = "locationFrom.code", target = "locationFromCode")
    @Mapping(source = "locationTo.id", target = "locationTo")
    @Mapping(source = "locationTo.code", target = "locationToCode")
    InventoryTransactionDetailResponse toResponse(InventoryTransactionDetail detail);
}
