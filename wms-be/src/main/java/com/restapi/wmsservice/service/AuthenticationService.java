package com.restapi.wmsservice.service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.restapi.wmsservice.dto.request.AuthenticationRequest;
import com.restapi.wmsservice.dto.request.IntrospectRequest;
import com.restapi.wmsservice.dto.request.LogoutRequest;
import com.restapi.wmsservice.dto.request.RefreshRequest;
import com.restapi.wmsservice.dto.response.AuthenticationResponse;
import com.restapi.wmsservice.dto.response.IntrospectResponse;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}

