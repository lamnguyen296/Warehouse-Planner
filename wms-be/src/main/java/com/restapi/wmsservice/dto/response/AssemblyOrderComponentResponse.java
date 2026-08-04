package com.restapi.wmsservice.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyOrderComponentResponse {
    Long itemId;
    String itemCode;
    String itemName;
    Integer requiredQuantity;
}
