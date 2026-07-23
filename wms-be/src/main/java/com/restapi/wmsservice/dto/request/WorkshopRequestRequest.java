package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.RequestPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkshopRequestRequest {

    @NotNull(message = "PRIORITY_REQUIRED")
    RequestPriority priority;

    // requestedDate: null → defaults to now in service layer
    LocalDateTime requestedDate;

    // expectedDate: must be in the future if provided
    @Future(message = "INVALID_EXPECTED_DATE")
    LocalDateTime expectedDate;

    @NotEmpty(message = "DETAILS_REQUIRED")
    @Valid
    List<WorkshopRequestDetailRequest> details;
}
