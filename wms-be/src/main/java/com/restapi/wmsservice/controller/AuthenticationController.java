package com.restapi.wmsservice.controller;



import com.restapi.wmsservice.dto.request.*;
import com.restapi.wmsservice.dto.response.AuthenticationResponse;
import com.restapi.wmsservice.dto.response.CsrfResponse;
import com.restapi.wmsservice.dto.response.IntrospectResponse;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.security.AuthCookieService;
import com.restapi.wmsservice.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    AuthCookieService authCookieService;

    @GetMapping("/csrf")
    ApiResponse<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ApiResponse.<CsrfResponse>builder()
                .result(new CsrfResponse(
                        csrfToken.getHeaderName(),
                        csrfToken.getParameterName(),
                        csrfToken.getToken()))
                .build();
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
                                                       HttpServletResponse response) {
        var tokens = authenticationService.authenticate(request);
        authCookieService.addAuthenticationCookies(response, tokens);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return ApiResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder().authenticated(true).build())
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(HttpServletRequest request,
                                                HttpServletResponse response) {
        try {
            var tokens = authenticationService.refreshToken(
                    authCookieService.getRefreshToken(request).orElse(null));
            authCookieService.addAuthenticationCookies(response, tokens);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(AuthenticationResponse.builder().authenticated(true).build())
                    .build();
        } catch (AppException exception) {
            authCookieService.clearAuthenticationCookies(response);
            throw exception;
        }
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            authenticationService.logout(
                    authCookieService.getAccessToken(request).orElse(null),
                    authCookieService.getRefreshToken(request).orElse(null));
            return ApiResponse.<Void>builder().build();
        } finally {
            authCookieService.clearAuthenticationCookies(response);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        }
    }
}
