package com.restapi.wmsservice.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieBearerTokenResolverTest {
    @Mock
    AuthCookieService authCookieService;

    @InjectMocks
    CookieBearerTokenResolver resolver;

    @Test
    void resolvesAccessTokenForProtectedApi() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/users/myInfo");
        when(authCookieService.getAccessToken(request)).thenReturn(Optional.of("access-token"));

        assertThat(resolver.resolve(request)).isEqualTo("access-token");
    }

    @Test
    void ignoresExpiredAccessCookieOnRefreshEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/auth/refresh");

        assertThat(resolver.resolve(request)).isNull();
        verifyNoInteractions(authCookieService);
    }
}
