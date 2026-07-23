package com.restapi.wmsservice.service;

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

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> getAll();
    void delete(Long permission);
}

