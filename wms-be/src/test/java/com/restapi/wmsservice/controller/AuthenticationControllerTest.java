package com.restapi.wmsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restapi.wmsservice.configuration.CustomJwtDecoder;
import com.restapi.wmsservice.configuration.AuthCookieProperties;
import com.restapi.wmsservice.configuration.SecurityConfig;
import com.restapi.wmsservice.dto.request.AuthenticationRequest;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.exception.GlobalExceptionHandler;
import com.restapi.wmsservice.security.AuthCookieService;
import com.restapi.wmsservice.security.CookieBearerTokenResolver;
import com.restapi.wmsservice.security.IssuedTokens;
import com.restapi.wmsservice.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @MockitoBean
    CookieBearerTokenResolver cookieBearerTokenResolver;

    @MockitoBean
    AuthCookieProperties authCookieProperties;

    @MockitoBean
    AuthCookieService authCookieService;

    @BeforeEach
    void setUpCookies() {
        when(authCookieProperties.getSameSite()).thenReturn("Lax");
        when(authCookieService.getRefreshToken(any())).thenReturn(Optional.of(REFRESH_TOKEN));
    }

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
        when(authenticationService.refreshToken(REFRESH_TOKEN))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_INACTIVE));

        assertAuthenticationError("/auth/refresh", null, ErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    void refresh_locked_returnsHttp401AndNoTokens() throws Exception {
        when(authenticationService.refreshToken(REFRESH_TOKEN))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_LOCKED));

        assertAuthenticationError("/auth/refresh", null, ErrorCode.ACCOUNT_LOCKED);
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

        mockMvc.perform(post("/auth/token").with(csrf())
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
        verify(authCookieService).addAuthenticationCookies(any(), eq(successfulResponse()));
    }

    @Test
    void login_withoutCsrfToken_isRejected() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(loginRequest())))
                .andExpect(status().isForbidden());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void refresh_active_preservesSuccessfulResponse() throws Exception {
        when(authenticationService.refreshToken(REFRESH_TOKEN))
                .thenReturn(successfulResponse());

        assertAuthenticationSuccess("/auth/refresh", null);
        verify(authCookieService).addAuthenticationCookies(any(), eq(successfulResponse()));
    }

    private void assertAuthenticationError(String endpoint, Object request, ErrorCode errorCode) throws Exception {
        var requestBuilder = post(endpoint).with(csrf()).contentType(MediaType.APPLICATION_JSON);
        if (request != null) requestBuilder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$..accessToken").doesNotExist())
                .andExpect(jsonPath("$..refreshToken").doesNotExist());
    }

    private void assertAuthenticationSuccess(String endpoint, Object request) throws Exception {
        var requestBuilder = post(endpoint).with(csrf()).contentType(MediaType.APPLICATION_JSON);
        if (request != null) requestBuilder.content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..accessToken").doesNotExist())
                .andExpect(jsonPath("$..refreshToken").doesNotExist())
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    private AuthenticationRequest loginRequest() {
        return AuthenticationRequest.builder()
                .username("test-user")
                .password("password123")
                .build();
    }

    private IssuedTokens successfulResponse() {
        return new IssuedTokens(ACCESS_TOKEN, REFRESH_TOKEN);
    }
}
