package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "FULLNAME_REQUIRED")
    String fullname;

    @NotNull(message = "USER_STATUS_REQUIRED")
    UserStatus status;

    @NotEmpty(message = "ROLES_REQUIRED")
    Set<Long> roles;
}
