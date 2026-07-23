package com.restapi.wmsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restapi.wmsservice.dto.request.UserCreationRequest;
import com.restapi.wmsservice.dto.response.UserResponse;
import com.restapi.wmsservice.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userRoles", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "roles", source = "userRoles")
    UserResponse toUserResponse(User user);

    default java.util.Set<com.restapi.wmsservice.dto.response.RoleResponse> mapUserRoles(java.util.List<com.restapi.wmsservice.entity.UserRole> userRoles) {
        if (userRoles == null) {
            return null;
        }
        return userRoles.stream()
                .map(ur -> {
                    com.restapi.wmsservice.entity.Role r = ur.getRole();
                    if (r == null) return null;
                    return com.restapi.wmsservice.dto.response.RoleResponse.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .description(r.getDescription())
                            .permissions(r.getRolePermissions() == null ? java.util.Set.of() : r.getRolePermissions().stream()
                                    .map(rolePermission -> rolePermission.getPermission())
                                    .map(permission -> com.restapi.wmsservice.dto.response.PermissionResponse.builder()
                                            .id(permission.getId())
                                            .code(permission.getCode())
                                            .name(permission.getName())
                                            .module(permission.getModule())
                                            .description(permission.getDescription())
                                            .build())
                                    .collect(java.util.stream.Collectors.toSet()))
                            .build();
                })
                .collect(java.util.stream.Collectors.toSet());
    }
}
