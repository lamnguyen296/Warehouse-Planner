package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.service.*;

import com.restapi.wmsservice.dto.request.PermissionRequest;
import com.restapi.wmsservice.dto.response.PermissionResponse;
import com.restapi.wmsservice.entity.Permission;
import com.restapi.wmsservice.mapper.PermissionMapper;
import com.restapi.wmsservice.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Transactional
    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Transactional
    public void delete(Long permission){
        permissionRepository.deleteById(permission);
    }
}

