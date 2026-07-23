package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.PurchaseStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseRequestResponse {

    Long id;

    String requestNo;

    Long planningDetailId;

    PurchaseStatus status;

    List<PurchaseRequestDetailResponse> details;
}
