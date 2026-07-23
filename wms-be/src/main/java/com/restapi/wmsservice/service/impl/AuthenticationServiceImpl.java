package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.service.*;

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
import com.restapi.wmsservice.entity.User;
import com.restapi.wmsservice.enums.UserStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    JwtService jwtService;
    TokenStorageService tokenStorageService;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getAccessToken();
        boolean isValid = true;

        try {
            verifyTokenFully(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        validateAccountStatus(user);

        String familyId = UUID.randomUUID().toString();
        var token = jwtService.generateToken(user, familyId);
        
        String refreshToken = UUID.randomUUID().toString();
        tokenStorageService.saveRefreshToken(familyId, user.getUsername(), refreshToken);

        return AuthenticationResponse.builder().accessToken(token).refreshToken(refreshToken).authenticated(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyTokenFully(request.getAccessToken());

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            long remainingTime = expiryTime.getTime() - new Date().getTime();
            tokenStorageService.blacklistAccessToken(jit, remainingTime);

            String familyId = signToken.getJWTClaimsSet().getStringClaim("fid");
            tokenStorageService.invalidateRefreshTokenFamily(familyId);

        } catch (AppException exception){
            log.info("Access Token already expired or invalid");
        }
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        String reqRefreshToken = request.getRefreshToken();
        
        if (reqRefreshToken == null || reqRefreshToken.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        String rtVal = tokenStorageService.getRefreshTokenData(reqRefreshToken);
        if (rtVal == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        String[] parts = rtVal.split(":");
        if (parts.length != 2) {
             throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String familyId = parts[0];
        String username = parts[1];
        
        String currentFamilyRt = tokenStorageService.getCurrentFamilyRefreshToken(familyId);
        
        if (!reqRefreshToken.equals(currentFamilyRt)) {
            // Reuse detected!
            tokenStorageService.invalidateRefreshTokenFamily(familyId);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        validateAccountStatus(user);
        
        var token = jwtService.generateToken(user, familyId);
        String newRefreshToken = UUID.randomUUID().toString();
        
        tokenStorageService.saveRefreshToken(familyId, username, newRefreshToken);

        return AuthenticationResponse.builder().accessToken(token).refreshToken(newRefreshToken).authenticated(true).build();
    }

    private void validateAccountStatus(User user) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private SignedJWT verifyTokenFully(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = jwtService.verifyTokenSignatureAndExpiry(token);

        if (tokenStorageService.isAccessTokenBlacklisted(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}
