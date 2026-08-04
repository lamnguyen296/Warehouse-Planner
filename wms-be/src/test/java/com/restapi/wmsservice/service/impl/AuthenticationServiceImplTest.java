package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.AuthenticationRequest;
import com.restapi.wmsservice.entity.User;
import com.restapi.wmsservice.enums.UserStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.UserRepository;
import com.restapi.wmsservice.service.JwtService;
import com.restapi.wmsservice.service.TokenStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String USERNAME = "test-user";
    private static final String RAW_PASSWORD = "password123";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String FAMILY_ID = "family-id";

    @Mock
    UserRepository userRepository;

    @Mock
    JwtService jwtService;

    @Mock
    TokenStorageService tokenStorageService;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Test
    void authenticate_activeUser_returnsAccessAndRefreshTokens() {
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(eq(user), anyString())).thenReturn(ACCESS_TOKEN);

        var response = authenticationService.authenticate(authenticationRequest(RAW_PASSWORD));

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotBlank();

        ArgumentCaptor<String> familyIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtService).generateToken(eq(user), familyIdCaptor.capture());
        verify(tokenStorageService).saveRefreshToken(
                eq(familyIdCaptor.getValue()), eq(USERNAME), eq(response.refreshToken()));
    }

    @Test
    void authenticate_inactiveUser_returnsAccountInactiveWithoutCreatingTokens() {
        User user = user(UserStatus.INACTIVE);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertAuthenticationFailure(
                () -> authenticationService.authenticate(authenticationRequest(RAW_PASSWORD)),
                ErrorCode.ACCOUNT_INACTIVE,
                HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(jwtService, tokenStorageService);
    }

    @Test
    void authenticate_lockedUser_returnsAccountLockedWithoutCreatingTokens() {
        User user = user(UserStatus.LOCKED);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertAuthenticationFailure(
                () -> authenticationService.authenticate(authenticationRequest(RAW_PASSWORD)),
                ErrorCode.ACCOUNT_LOCKED,
                HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(jwtService, tokenStorageService);
    }

    @Test
    void authenticate_wrongPassword_preservesUnauthenticatedErrorWithoutCreatingTokens() {
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertAuthenticationFailure(
                () -> authenticationService.authenticate(authenticationRequest("wrong-password")),
                ErrorCode.UNAUTHENTICATED,
                HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(jwtService, tokenStorageService);
    }

    @Test
    void authenticate_unknownUser_preservesUserNotExistedErrorWithoutCreatingTokens() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertAuthenticationFailure(
                () -> authenticationService.authenticate(authenticationRequest(RAW_PASSWORD)),
                ErrorCode.USER_NOT_EXISTED,
                HttpStatus.NOT_FOUND);
        verifyNoInteractions(jwtService, tokenStorageService);
    }

    @Test
    void refreshToken_activeUser_returnsNewAccessAndRefreshTokens() throws Exception {
        User user = user(UserStatus.ACTIVE);
        prepareRefresh(user);
        when(jwtService.generateToken(user, FAMILY_ID)).thenReturn(ACCESS_TOKEN);
        when(tokenStorageService.rotateRefreshToken(
                eq(FAMILY_ID), eq(USERNAME), eq(REFRESH_TOKEN), anyString())).thenReturn(true);

        var response = authenticationService.refreshToken(REFRESH_TOKEN);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotBlank().isNotEqualTo(REFRESH_TOKEN);
        verify(tokenStorageService).rotateRefreshToken(
                FAMILY_ID, USERNAME, REFRESH_TOKEN, response.refreshToken());
    }

    @Test
    void refreshToken_inactiveUser_returnsAccountInactiveWithoutCreatingTokens() {
        User user = user(UserStatus.INACTIVE);
        prepareRefresh(user);

        assertAuthenticationFailure(
                () -> authenticationService.refreshToken(REFRESH_TOKEN),
                ErrorCode.ACCOUNT_INACTIVE,
                HttpStatus.UNAUTHORIZED);
        verifyNoTokenWrites(user);
    }

    @Test
    void refreshToken_lockedUser_returnsAccountLockedWithoutCreatingTokens() {
        User user = user(UserStatus.LOCKED);
        prepareRefresh(user);

        assertAuthenticationFailure(
                () -> authenticationService.refreshToken(REFRESH_TOKEN),
                ErrorCode.ACCOUNT_LOCKED,
                HttpStatus.UNAUTHORIZED);
        verifyNoTokenWrites(user);
    }

    @Test
    void refreshToken_nullStatus_failsClosedWithoutCreatingTokens() {
        User user = user(null);
        prepareRefresh(user);

        assertAuthenticationFailure(
                () -> authenticationService.refreshToken(REFRESH_TOKEN),
                ErrorCode.UNAUTHENTICATED,
                HttpStatus.UNAUTHORIZED);
        verifyNoTokenWrites(user);
    }

    @Test
    void logout_withoutUsableAccessToken_stillInvalidatesRefreshFamily() {
        when(tokenStorageService.getRefreshTokenData(REFRESH_TOKEN))
                .thenReturn(FAMILY_ID + ":" + USERNAME);

        authenticationService.logout(null, REFRESH_TOKEN);

        verify(tokenStorageService).invalidateRefreshTokenFamily(FAMILY_ID);
        verify(tokenStorageService, never()).blacklistAccessToken(anyString(), anyLong());
    }

    private void prepareRefresh(User user) {
        when(tokenStorageService.getRefreshTokenData(REFRESH_TOKEN))
                .thenReturn(FAMILY_ID + ":" + USERNAME);
        when(tokenStorageService.getCurrentFamilyRefreshToken(FAMILY_ID)).thenReturn(REFRESH_TOKEN);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
    }

    private User user(UserStatus status) {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(new BCryptPasswordEncoder(10).encode(RAW_PASSWORD));
        user.setStatus(status);
        return user;
    }

    private AuthenticationRequest authenticationRequest(String password) {
        return AuthenticationRequest.builder()
                .username(USERNAME)
                .password(password)
                .build();
    }

    private void verifyNoTokenWrites(User user) {
        verify(jwtService, never()).generateToken(eq(user), anyString());
        verify(tokenStorageService, never()).saveRefreshToken(anyString(), anyString(), anyString());
        verify(tokenStorageService, never()).rotateRefreshToken(
                anyString(), anyString(), anyString(), anyString());
        verify(tokenStorageService, never()).invalidateRefreshTokenFamily(anyString());
        verify(tokenStorageService, never()).blacklistAccessToken(anyString(), anyLong());
    }

    private void assertAuthenticationFailure(ThrowingOperation operation,
                                             ErrorCode expectedError,
                                             HttpStatus expectedStatus) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(AppException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(expectedError);
                    assertThat(exception.getErrorCode().getStatusCode()).isEqualTo(expectedStatus);
                });
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }
}
