package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.ItemStatus;
import com.restapi.wmsservice.enums.ItemType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemResponse {
    Long id;
    String code;
    String name;
    ItemType itemType;
    String unit;
    String description;
    String imageUrl;
    ItemStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
