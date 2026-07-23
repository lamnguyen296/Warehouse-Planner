package com.restapi.wmsservice.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static com.restapi.wmsservice.security.PermissionCode.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"
    };

    private final String[] SWAGGER_ENDPOINTS = {
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).denyAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        .requestMatchers("/notifications", "/notifications/**").authenticated()
                        .requestMatchers("/users/myInfo").authenticated()
                        .requestMatchers("/users", "/users/**", "/roles", "/roles/**",
                                "/permissions", "/permissions/**")
                        .hasAuthority(SECURITY_MANAGE)

                        .requestMatchers(HttpMethod.POST, "/workshop-requests/*/approve")
                        .hasAuthority(WORKSHOP_REQUEST_APPROVE)
                        .requestMatchers(HttpMethod.POST, "/workshop-requests/*/submit")
                        .hasAuthority(WORKSHOP_REQUEST_SUBMIT_OWN)
                        .requestMatchers(HttpMethod.POST, "/workshop-requests/*/cancel")
                        .hasAuthority(WORKSHOP_REQUEST_CANCEL_OWN)
                        .requestMatchers(HttpMethod.POST, "/workshop-requests")
                        .hasAuthority(WORKSHOP_REQUEST_CREATE)
                        .requestMatchers(HttpMethod.PUT, "/workshop-requests/**")
                        .hasAuthority(WORKSHOP_REQUEST_UPDATE_OWN)
                        .requestMatchers(HttpMethod.DELETE, "/workshop-requests/**")
                        .hasAuthority(WORKSHOP_REQUEST_UPDATE_OWN)
                        .requestMatchers(HttpMethod.GET, "/workshop-requests/**")
                        .hasAnyAuthority(WORKSHOP_REQUEST_READ_OWN, WORKSHOP_REQUEST_READ_ALL)

                        .requestMatchers(HttpMethod.POST, "/plannings/run-engine/*")
                        .hasAuthority(PLANNING_RUN)
                        .requestMatchers(HttpMethod.POST, "/plannings/*/approve")
                        .hasAuthority(PLANNING_APPROVE)
                        .requestMatchers(HttpMethod.POST, "/plannings/*/fail")
                        .hasAuthority(PLANNING_FAIL)
                        .requestMatchers(HttpMethod.POST, "/plannings/*/start-execution")
                        .hasAuthority(PLANNING_EXECUTE)
                        .requestMatchers(HttpMethod.GET, "/plannings/**")
                        .hasAuthority(PLANNING_READ)
                        .requestMatchers("/plannings/**")
                        .hasAuthority(PLANNING_EXECUTE)

                        .requestMatchers(HttpMethod.GET, "/inventory/**")
                        .hasAuthority(INVENTORY_READ)
                        .requestMatchers("/inventory/**")
                        .hasAuthority(INVENTORY_ADJUST)
                        .requestMatchers(HttpMethod.GET, "/inventory-reservations/**")
                        .hasAuthority(INVENTORY_RESERVATION_READ)
                        .requestMatchers(HttpMethod.GET, "/inventory-transactions/**")
                        .hasAuthority(INVENTORY_MOVEMENT_READ)
                        .requestMatchers(HttpMethod.POST, "/inventory-transactions/*/attachments")
                        .hasAuthority(INVENTORY_ADJUST)
                        .requestMatchers(HttpMethod.DELETE, "/inventory-transactions/*/attachments/*")
                        .hasAuthority(INVENTORY_ADJUST)
                        .requestMatchers(HttpMethod.GET, "/inventory-ops/reservations/**")
                        .hasAuthority(INVENTORY_RESERVATION_READ)
                        .requestMatchers("/inventory-ops/recycle-orders/**")
                        .hasAuthority(RECYCLE_EXECUTE)
                        .requestMatchers("/inventory-ops/purchase-requests/**")
                        .hasAuthority(PURCHASE_EXECUTE)
                        .requestMatchers("/inventory-ops/assembly-orders/**")
                        .hasAuthority(ASSEMBLY_EXECUTE)
                        .requestMatchers("/inventory-ops/**")
                        .hasAuthority(INVENTORY_ADJUST)

                        .requestMatchers(HttpMethod.GET, "/purchase-requests/**").hasAuthority(PURCHASE_READ)
                        .requestMatchers("/purchase-requests/**").hasAuthority(PURCHASE_EXECUTE)
                        .requestMatchers(HttpMethod.GET, "/recycle-orders/**").hasAuthority(RECYCLE_READ)
                        .requestMatchers("/recycle-orders/**").hasAuthority(RECYCLE_EXECUTE)
                        .requestMatchers(HttpMethod.GET, "/assembly-orders/**").hasAuthority(ASSEMBLY_READ)
                        .requestMatchers("/assembly-orders/**").hasAuthority(ASSEMBLY_EXECUTE)
                        .requestMatchers(HttpMethod.GET, "/transfer-orders/**").hasAuthority(TRANSFER_READ)
                        .requestMatchers("/transfer-orders/**").hasAuthority(TRANSFER_EXECUTE)

                        .requestMatchers(HttpMethod.GET, "/warehouses/**", "/locations/**",
                                "/items/**", "/boms/**", "/batch-info/**")
                        .hasAuthority(MASTER_DATA_READ)
                        .requestMatchers("/warehouses/**", "/locations/**", "/items/**", "/boms/**", "/batch-info/**")
                        .hasAuthority(MASTER_DATA_MANAGE)
                        .anyRequest().denyAll());

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(org.springframework.security.config.Customizer.withDefaults());

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("http://localhost:5173"); // Example specific origin
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("DELETE");
        corsConfiguration.addAllowedMethod("OPTIONS");
        corsConfiguration.addAllowedHeader("Authorization");
        corsConfiguration.addAllowedHeader("Content-Type");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
