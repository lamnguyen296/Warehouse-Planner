package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseRequestRequest {

    Long planningDetailId;

    @Valid
    @NotEmpty(message = "Purchase request must contain at least one detail")
    List<PurchaseRequestDetailRequest> details;
}
