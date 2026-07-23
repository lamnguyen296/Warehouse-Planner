package com.restapi.wmsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restapi.wmsservice.dto.request.RoleRequest;
import com.restapi.wmsservice.dto.response.RoleResponse;
import com.restapi.wmsservice.entity.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    Role toRole(RoleRequest request);

    @Mapping(target = "permissions", source = "rolePermissions")
    RoleResponse toRoleResponse(Role role);

    default Set<com.restapi.wmsservice.dto.response.PermissionResponse> mapRolePermissions(
            List<com.restapi.wmsservice.entity.RolePermission> rolePermissions) {
        if (rolePermissions == null) {
            return Set.of();
        }
        return rolePermissions.stream()
                .map(rolePermission -> rolePermission.getPermission())
                .map(permission -> com.restapi.wmsservice.dto.response.PermissionResponse.builder()
                        .id(permission.getId())
                        .code(permission.getCode())
                        .name(permission.getName())
                        .module(permission.getModule())
                        .description(permission.getDescription())
                        .build())
                .collect(Collectors.toSet());
    }
}
