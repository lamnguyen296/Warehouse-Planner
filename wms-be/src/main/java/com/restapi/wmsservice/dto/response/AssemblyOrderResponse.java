package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.AssemblyStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyOrderResponse {

    Long id;

    String assemblyNo;

    Long planningDetailId;

    ItemResponse setItem;

    Integer quantity;

    List<AssemblyOrderComponentResponse> components;

    AssemblyStatus status;

    java.time.LocalDateTime createdAt;
}
