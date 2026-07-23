package com.restapi.wmsservice.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

public interface TokenStorageService {
    void saveRefreshToken(String familyId, String username, String refreshToken);
    void blacklistAccessToken(String jit, long remainingTimeMs);
    boolean isAccessTokenBlacklisted(String jit);
    void invalidateRefreshTokenFamily(String familyId);
    String getRefreshTokenData(String refreshToken);
    String getCurrentFamilyRefreshToken(String familyId);
}

