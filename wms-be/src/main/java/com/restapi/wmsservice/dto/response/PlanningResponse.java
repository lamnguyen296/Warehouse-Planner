package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.PlanningStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanningResponse {
    Long id;
    String planningNo;
    Long workshopRequestId;
    String workshopRequestNo;
    PlanningStatus status;
    List<PlanningDetailResponse> details;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
