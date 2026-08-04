package com.restapi.wmsservice.security;

public record IssuedTokens(String accessToken, String refreshToken) {
}
