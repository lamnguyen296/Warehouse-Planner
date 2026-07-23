package com.restapi.wmsservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomResponse {
    Long id;
    Long parentItemId;
    String parentItemCode;
    Long childItemId;
    String childItemCode;
    Integer quantity;
    Integer priority;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
