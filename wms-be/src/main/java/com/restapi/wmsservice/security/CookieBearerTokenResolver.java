package com.restapi.wmsservice.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CookieBearerTokenResolver implements BearerTokenResolver {
    private static final Set<String> COOKIE_AUTH_IGNORED_PATHS = Set.of(
            "/auth/token", "/auth/refresh", "/auth/logout", "/auth/introspect", "/auth/csrf");

    private final AuthCookieService authCookieService;

    @Override
    public String resolve(HttpServletRequest request) {
        if (COOKIE_AUTH_IGNORED_PATHS.contains(request.getServletPath())) {
            return null;
        }
        return authCookieService.getAccessToken(request).orElse(null);
    }
}
