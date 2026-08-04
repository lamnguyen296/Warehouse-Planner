package com.restapi.wmsservice.security;

import com.restapi.wmsservice.configuration.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthCookieService {
    private final AuthCookieProperties properties;
    private final Duration accessTokenMaxAge;
    private final Duration refreshTokenMaxAge;

    public AuthCookieService(
            AuthCookieProperties properties,
            @Value("${jwt.valid-duration}") long accessTokenDurationSeconds,
            @Value("${jwt.refreshable-duration}") long refreshTokenDurationSeconds) {
        this.properties = properties;
        this.accessTokenMaxAge = Duration.ofSeconds(accessTokenDurationSeconds);
        this.refreshTokenMaxAge = Duration.ofSeconds(refreshTokenDurationSeconds);
    }

    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, properties.getAccessName());
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, properties.getRefreshName());
    }

    public void addAuthenticationCookies(HttpServletResponse response, IssuedTokens tokens) {
        addCookie(response, properties.getAccessName(), tokens.accessToken(),
                properties.getAccessPath(), accessTokenMaxAge);
        addCookie(response, properties.getRefreshName(), tokens.refreshToken(),
                properties.getRefreshPath(), refreshTokenMaxAge);
    }

    public void clearAuthenticationCookies(HttpServletResponse response) {
        addCookie(response, properties.getAccessName(), "", properties.getAccessPath(), Duration.ZERO);
        addCookie(response, properties.getRefreshName(), "", properties.getRefreshPath(), Duration.ZERO);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String name, String value,
                           String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
