package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.UserRole;
import com.restapi.wmsservice.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
