package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.service.*;

import java.util.concurrent.TimeUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenStorageServiceImpl implements TokenStorageService {

    private static final DefaultRedisScript<Long> SAVE_REFRESH_TOKEN_SCRIPT = new DefaultRedisScript<>("""
            local ttl = tonumber(ARGV[3])
            if not ttl or ttl <= 0 then
                return redis.error_reply('Invalid refresh token TTL')
            end
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ttl)
            redis.call('SET', KEYS[2], ARGV[2], 'EX', ttl)
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> ROTATE_REFRESH_TOKEN_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current or current ~= ARGV[1] then
                return 0
            end
            local ttl = tonumber(ARGV[4])
            if not ttl or ttl <= 0 then
                return redis.error_reply('Invalid refresh token TTL')
            end
            redis.call('SET', KEYS[1], ARGV[2], 'EX', ttl)
            redis.call('SET', KEYS[2], ARGV[3], 'EX', ttl)
            return 1
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public void saveRefreshToken(String familyId, String username, String refreshToken) {
        Long result = stringRedisTemplate.execute(
                SAVE_REFRESH_TOKEN_SCRIPT,
                List.of("rt_family:" + familyId, "rt:" + refreshToken),
                refreshToken,
                familyId + ":" + username,
                String.valueOf(REFRESHABLE_DURATION));

        if (!Long.valueOf(1L).equals(result)) {
            throw new IllegalStateException("Redis did not persist the refresh token family");
        }
    }

    @Override
    public boolean rotateRefreshToken(String familyId, String username,
                                      String currentRefreshToken, String newRefreshToken) {
        Long result = stringRedisTemplate.execute(
                ROTATE_REFRESH_TOKEN_SCRIPT,
                List.of("rt_family:" + familyId, "rt:" + newRefreshToken),
                currentRefreshToken,
                newRefreshToken,
                familyId + ":" + username,
                String.valueOf(REFRESHABLE_DURATION));
        return Long.valueOf(1L).equals(result);
    }

    public void blacklistAccessToken(String jit, long remainingTimeMs) {
        if (remainingTimeMs > 0) {
            stringRedisTemplate.opsForValue().set("bl_at:" + jit, "invalid", remainingTimeMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String jit) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey("bl_at:" + jit));
    }

    public void invalidateRefreshTokenFamily(String familyId) {
        if (familyId != null && !familyId.isEmpty()) {
            stringRedisTemplate.delete("rt_family:" + familyId);
        }
    }

    public String getRefreshTokenData(String refreshToken) {
        return stringRedisTemplate.opsForValue().get("rt:" + refreshToken);
    }

    public String getCurrentFamilyRefreshToken(String familyId) {
        return stringRedisTemplate.opsForValue().get("rt_family:" + familyId);
    }
}

