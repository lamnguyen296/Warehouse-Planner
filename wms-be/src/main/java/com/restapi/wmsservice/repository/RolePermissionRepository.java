package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.RolePermission;
import com.restapi.wmsservice.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}
