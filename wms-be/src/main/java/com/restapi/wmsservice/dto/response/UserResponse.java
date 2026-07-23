package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String fullname;
    UserStatus status;
    Set<RoleResponse> roles;
}
