package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.ItemRequest;
import com.restapi.wmsservice.dto.response.ItemResponse;
import com.restapi.wmsservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toItem(ItemRequest request);

    ItemResponse toItemResponse(Item item);

    void updateItem(@MappingTarget Item item, ItemRequest request);
}
