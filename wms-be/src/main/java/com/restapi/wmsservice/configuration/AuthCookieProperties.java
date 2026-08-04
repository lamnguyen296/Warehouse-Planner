package com.restapi.wmsservice.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth-cookie")
public class AuthCookieProperties {
    private String accessName = "wms_access_token";
    private String refreshName = "wms_refresh_token";
    private String accessPath = "/wms";
    private String refreshPath = "/wms/auth";
    private boolean secure;
    private String sameSite = "Lax";
}
