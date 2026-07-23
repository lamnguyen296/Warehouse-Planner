package com.restapi.wmsservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String NOTIFICATION_DESTINATION = "/user/queue/notifications";

    private final CustomJwtDecoder customJwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            throw new AccessDeniedException("Client messages are not supported");
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing bearer token");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        Jwt jwt = customJwtDecoder.decode(token);
        List<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                jwt.getSubject(), token, authorities));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        if (!NOTIFICATION_DESTINATION.equals(accessor.getDestination())) {
            throw new AccessDeniedException("Subscription destination is not allowed");
        }
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.trim().split("\\s+"))
                .filter(authority -> !authority.isBlank())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
