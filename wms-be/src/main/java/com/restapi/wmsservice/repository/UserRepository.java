package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

   boolean existsByUsername(String username);

   Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    List<User> findAll();

    @Query("""
            select distinct u.username
            from User u
            join u.userRoles ur
            join ur.role r
            join r.rolePermissions rp
            join rp.permission p
            where p.code = :permissionCode
              and u.status = com.restapi.wmsservice.enums.UserStatus.ACTIVE
            """)
    List<String> findActiveUsernamesByPermission(@Param("permissionCode") String permissionCode);
}

