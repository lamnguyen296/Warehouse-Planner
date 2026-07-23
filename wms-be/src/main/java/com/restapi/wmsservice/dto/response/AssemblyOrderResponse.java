package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.AssemblyStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyOrderResponse {

    Long id;

    String assemblyNo;

    Long planningDetailId;

    ItemResponse setItem;

    Integer quantity;

    AssemblyStatus status;

    java.time.LocalDateTime createdAt;
}
