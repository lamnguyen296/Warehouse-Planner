package com.restapi.wmsservice.security;

import com.restapi.wmsservice.configuration.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {
    private AuthCookieService authCookieService;

    @BeforeEach
    void setUp() {
        AuthCookieProperties properties = new AuthCookieProperties();
        properties.setSecure(true);
        properties.setSameSite("Lax");
        authCookieService = new AuthCookieService(properties, 900, 259200);
    }

    @Test
    void addAuthenticationCookies_setsSecureHttpOnlyCookiesWithScopedPaths() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authCookieService.addAuthenticationCookies(
                response, new IssuedTokens("access-token", "refresh-token"));

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("wms_access_token=access-token", "Path=/wms", "Max-Age=900",
                                "Secure", "HttpOnly", "SameSite=Lax"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("wms_refresh_token=refresh-token", "Path=/wms/auth", "Max-Age=259200",
                                "Secure", "HttpOnly", "SameSite=Lax"));
    }

    @Test
    void clearAuthenticationCookies_expiresBothCookiesUsingTheirOriginalPaths() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authCookieService.clearAuthenticationCookies(response);

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("wms_access_token=", "Path=/wms", "Max-Age=0"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("wms_refresh_token=", "Path=/wms/auth", "Max-Age=0"));
    }

    @Test
    void readsTokensFromRequestCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("wms_access_token", "access-token"),
                new Cookie("wms_refresh_token", "refresh-token"));

        assertThat(authCookieService.getAccessToken(request)).contains("access-token");
        assertThat(authCookieService.getRefreshToken(request)).contains("refresh-token");
    }
}
