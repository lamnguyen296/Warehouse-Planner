package com.restapi.wmsservice.service;

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

public interface RoleService {
    RoleResponse create(RoleRequest request);
    List<RoleResponse> getAll();
    void delete(Long role);
}

