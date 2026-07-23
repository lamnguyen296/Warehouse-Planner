package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.UserCreationRequest;
import com.restapi.wmsservice.dto.request.UserUpdateRequest;
import com.restapi.wmsservice.dto.response.UserResponse;
import com.restapi.wmsservice.entity.Role;
import com.restapi.wmsservice.entity.User;
import com.restapi.wmsservice.entity.UserRole;
import com.restapi.wmsservice.entity.UserRoleId;
import com.restapi.wmsservice.enums.UserStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.UserMapper;
import com.restapi.wmsservice.repository.RoleRepository;
import com.restapi.wmsservice.repository.UserRepository;
import com.restapi.wmsservice.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        List<Role> roles = loadRoles(request.getRoles());
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setUserRoles(toUserRoles(user, roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Role> roles = loadRoles(request.getRoles());
        validateSelfSecurityChange(user, request, roles);
        user.setFullname(request.getFullname());
        user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        synchronizeRoles(user, roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (isCurrentUser(user)) {
            throw new AppException(ErrorCode.SELF_DELETE_NOT_ALLOWED);
        }
        userRepository.delete(user);
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGE')")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(){
        log.info("In method get Users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name or hasAuthority('SECURITY_MANAGE')")
    @Transactional(readOnly = true)
    public UserResponse getUser(String id){
        log.info("In method get user by Id");
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    private List<Role> loadRoles(Set<Long> roleIds) {
        Set<Long> distinctRoleIds = new HashSet<>(roleIds);
        List<Role> roles = roleRepository.findAllById(distinctRoleIds);
        if (roles.size() != distinctRoleIds.size()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        return roles;
    }

    private List<UserRole> toUserRoles(User user, List<Role> roles) {
        return roles.stream()
                .map(role -> newUserRole(user, role))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void synchronizeRoles(User user, List<Role> roles) {
        if (user.getUserRoles() == null) {
            user.setUserRoles(new ArrayList<>());
        }

        Set<Long> selectedRoleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
        user.getUserRoles().removeIf(userRole -> !selectedRoleIds.contains(userRole.getRole().getId()));

        Set<Long> existingRoleIds = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getId())
                .collect(Collectors.toSet());
        roles.stream()
                .filter(role -> !existingRoleIds.contains(role.getId()))
                .map(role -> newUserRole(user, role))
                .forEach(user.getUserRoles()::add);
    }

    private void validateSelfSecurityChange(User user, UserUpdateRequest request, List<Role> roles) {
        if (!isCurrentUser(user)) {
            return;
        }

        Set<Long> currentRoleIds = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getId())
                .collect(Collectors.toSet());
        Set<Long> requestedRoleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
        if (user.getStatus() != request.getStatus() || !currentRoleIds.equals(requestedRoleIds)) {
            throw new AppException(ErrorCode.SELF_SECURITY_CHANGE_NOT_ALLOWED);
        }
    }

    private boolean isCurrentUser(User user) {
        return user.getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private UserRole newUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId());
        userRole.setUser(user);
        userRole.setRole(role);
        return userRole;
    }
}
