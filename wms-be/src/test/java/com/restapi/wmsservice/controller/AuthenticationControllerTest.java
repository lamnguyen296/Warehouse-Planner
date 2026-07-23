package com.restapi.wmsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restapi.wmsservice.configuration.CustomJwtDecoder;
import com.restapi.wmsservice.configuration.SecurityConfig;
import com.restapi.wmsservice.dto.request.AuthenticationRequest;
import com.restapi.wmsservice.dto.request.RefreshRequest;
import com.restapi.wmsservice.dto.response.AuthenticationResponse;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.exception.GlobalExceptionHandler;
import com.restapi.wmsservice.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthenticationControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthenticationService authenticationService;

    @MockitoBean
    CustomJwtDecoder customJwtDecoder;

    @Test
    void login_inactive_returnsHttp401AndNoTokens() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_INACTIVE));

        assertAuthenticationError("/auth/token", loginRequest(), ErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    void login_locked_returnsHttp401AndNoTokens() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_LOCKED));

        assertAuthenticationError("/auth/token", loginRequest(), ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    void refresh_inactive_returnsHttp401AndNoTokens() throws Exception {
        when(authenticationService.refreshToken(any(RefreshRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_INACTIVE));

        assertAuthenticationError("/auth/refresh", refreshRequest(), ErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    void refresh_locked_returnsHttp401AndNoTokens() throws Exception {
        when(authenticationService.refreshToken(any(RefreshRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_LOCKED));

        assertAuthenticationError("/auth/refresh", refreshRequest(), ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    void login_wrongPassword_preservesUnauthenticatedContract() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        assertAuthenticationError("/auth/token", loginRequest(), ErrorCode.UNAUTHENTICATED);
    }

    @Test
    void login_unknownUser_preservesNotFoundContract() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(loginRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_EXISTED.getCode()))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    void login_active_preservesSuccessfulResponse() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(successfulResponse());

        assertAuthenticationSuccess("/auth/token", loginRequest());
    }

    @Test
    void refresh_active_preservesSuccessfulResponse() throws Exception {
        when(authenticationService.refreshToken(any(RefreshRequest.class)))
                .thenReturn(successfulResponse());

        assertAuthenticationSuccess("/auth/refresh", refreshRequest());
    }

    private void assertAuthenticationError(String endpoint, Object request, ErrorCode errorCode) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$..accessToken").doesNotExist())
                .andExpect(jsonPath("$..refreshToken").doesNotExist());
    }

    private void assertAuthenticationSuccess(String endpoint, Object request) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.result.refreshToken").value(REFRESH_TOKEN))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    private AuthenticationRequest loginRequest() {
        return AuthenticationRequest.builder()
                .username("test-user")
                .password("password123")
                .build();
    }

    private RefreshRequest refreshRequest() {
        return RefreshRequest.builder().refreshToken(REFRESH_TOKEN).build();
    }

    private AuthenticationResponse successfulResponse() {
        return AuthenticationResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .authenticated(true)
                .build();
    }
}
