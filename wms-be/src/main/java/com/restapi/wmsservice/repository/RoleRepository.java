package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    List<Role> findAll();
}
