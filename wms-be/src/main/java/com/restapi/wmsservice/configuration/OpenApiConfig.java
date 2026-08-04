package com.restapi.wmsservice.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "WMS Service API",
                version = "1.0",
                description = "API Documentation for WMS (Warehouse Management System)"
        ),
        security = {
                @SecurityRequirement(name = "cookieAuth")
        }
)
@SecurityScheme(
        name = "cookieAuth",
        description = "JWT access token stored in an HttpOnly cookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "wms_access_token"
)
public class OpenApiConfig {
}
