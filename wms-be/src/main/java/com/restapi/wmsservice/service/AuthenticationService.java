package com.restapi.wmsservice.service;

import java.text.ParseException;
import com.restapi.wmsservice.dto.request.AuthenticationRequest;
import com.restapi.wmsservice.dto.request.IntrospectRequest;
import com.restapi.wmsservice.dto.response.IntrospectResponse;
import com.restapi.wmsservice.security.IssuedTokens;
import com.nimbusds.jose.JOSEException;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    IssuedTokens authenticate(AuthenticationRequest request);
    void logout(String accessToken, String refreshToken);
    IssuedTokens refreshToken(String refreshToken);
}

