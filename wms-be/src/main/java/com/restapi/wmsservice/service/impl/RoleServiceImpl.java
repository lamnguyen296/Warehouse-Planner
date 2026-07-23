package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.service.*;


import com.restapi.wmsservice.dto.request.RoleRequest;
import com.restapi.wmsservice.dto.response.RoleResponse;
import com.restapi.wmsservice.mapper.RoleMapper;
import com.restapi.wmsservice.repository.PermissionRepository;
import com.restapi.wmsservice.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @Transactional
    public RoleResponse create(RoleRequest request){
        final var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        var rolePermissions = permissions.stream().map(permission -> {
            com.restapi.wmsservice.entity.RolePermission rp = new com.restapi.wmsservice.entity.RolePermission();
            rp.setRole(role);
            rp.setPermission(permission);
            return rp;
        }).toList();
        
        role.setRolePermissions(rolePermissions);

        var savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAll(){
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Transactional
    public void delete(Long role){
        roleRepository.deleteById(role);
    }
}

