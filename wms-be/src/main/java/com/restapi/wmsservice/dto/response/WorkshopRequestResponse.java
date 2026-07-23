package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.RequestPriority;
import com.restapi.wmsservice.enums.RequestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkshopRequestResponse {
    Long id;
    String requestNo;
    RequestPriority priority;
    RequestStatus status;
    LocalDateTime requestedDate;
    LocalDateTime expectedDate;
    String createdBy;
    List<WorkshopRequestDetailResponse> details;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
