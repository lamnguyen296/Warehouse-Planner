package com.restapi.wmsservice.configuration;

import com.restapi.wmsservice.entity.User;
import com.restapi.wmsservice.entity.Role;
import com.restapi.wmsservice.entity.UserRole;
import com.restapi.wmsservice.entity.UserRoleId;
import com.restapi.wmsservice.repository.UserRepository;
import com.restapi.wmsservice.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig implements ApplicationRunner {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String roleName : List.of("ADMIN", "OPERATOR", "WORKSHOP", "MANAGER")) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName + " role");
                return roleRepository.save(role);
            });
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            String adminPassword = environment.getProperty("app.bootstrap-admin.password", "");
            if (adminPassword.isBlank()) {
                log.warn("Bootstrap admin was not created because ADMIN_PASSWORD is not configured");
                return;
            }
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setStatus(com.restapi.wmsservice.enums.UserStatus.ACTIVE);
            user.setFullname("System Admin");

            UserRole userRole = new UserRole();
            userRole.setId(new UserRoleId());
            userRole.setUser(user);
            userRole.setRole(adminRole);

            user.setUserRoles(List.of(userRole));

            userRepository.save(user);
            log.info("Bootstrap admin user has been created");
        }
    }
}
