package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "FULLNAME_REQUIRED")
    String fullname;

    UserStatus status;

    @NotEmpty(message = "ROLES_REQUIRED")
    Set<Long> roles;
}
