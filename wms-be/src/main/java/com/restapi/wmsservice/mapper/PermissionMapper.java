package com.restapi.wmsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restapi.wmsservice.dto.request.PermissionRequest;
import com.restapi.wmsservice.dto.response.PermissionResponse;
import com.restapi.wmsservice.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @Mapping(target = "rolePermissions", ignore = true)
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
