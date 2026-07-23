package com.restapi.wmsservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanningRequest {

    @NotNull(message = "REQUEST_ID_REQUIRED")
    Long requestId;

    // status không được set bởi client – do service quản lý
    // details là optional: runPlanningEngine() tự tạo, createPlanning() mới cần
    @Valid
    List<PlanningDetailRequest> details;
}

