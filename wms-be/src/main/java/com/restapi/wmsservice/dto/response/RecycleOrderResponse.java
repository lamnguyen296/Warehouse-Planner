package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.RecycleStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecycleOrderResponse {

    Long id;

    String orderNo;

    Long planningDetailId;

    ItemResponse fromItem;

    ItemResponse toItem;

    Integer quantity;

    Integer expectedYield;

    Integer conversionRatio;

    Integer actualYield;

    RecycleStatus status;

    LocalDateTime startTime;

    LocalDateTime finishTime;
}
