package com.restapi.wmsservice.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TokenStorageServiceImplRedisIntegrationTest {

    private static final long TEST_TTL_SECONDS = 30L;

    private final List<String> keysToDelete = new ArrayList<>();
    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        try {
            connectionFactory = new LettuceConnectionFactory("localhost", 6379);
            connectionFactory.afterPropertiesSet();
            connectionFactory.start();
            redisTemplate = new StringRedisTemplate(connectionFactory);
            redisTemplate.afterPropertiesSet();
            assumeTrue("PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping()),
                    "Redis localhost:6379 did not respond to PING");
        } catch (RuntimeException exception) {
            assumeTrue(false, "Redis localhost:6379 is unavailable: " + exception.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (redisTemplate != null && !keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void saveRefreshToken_writesBothKeysWithTtlOnRedis() {
        String familyId = "batch11-family-" + UUID.randomUUID();
        String username = "batch11-user";
        String refreshToken = "batch11-refresh-" + UUID.randomUUID();
        String familyKey = "rt_family:" + familyId;
        String refreshKey = "rt:" + refreshToken;
        keysToDelete.addAll(List.of(familyKey, refreshKey));

        TokenStorageServiceImpl service = serviceWithTtl(TEST_TTL_SECONDS);
        service.saveRefreshToken(familyId, username, refreshToken);

        assertThat(redisTemplate.opsForValue().get(familyKey)).isEqualTo(refreshToken);
        assertThat(redisTemplate.opsForValue().get(refreshKey)).isEqualTo(familyId + ":" + username);
        assertThat(redisTemplate.getExpire(familyKey, TimeUnit.SECONDS)).isBetween(1L, TEST_TTL_SECONDS);
        assertThat(redisTemplate.getExpire(refreshKey, TimeUnit.SECONDS)).isBetween(1L, TEST_TTL_SECONDS);
    }

    @Test
    void saveRefreshToken_invalidTtlLeavesNeitherKeyOnRedis() {
        String familyId = "batch11-family-" + UUID.randomUUID();
        String refreshToken = "batch11-refresh-" + UUID.randomUUID();
        String familyKey = "rt_family:" + familyId;
        String refreshKey = "rt:" + refreshToken;
        keysToDelete.addAll(List.of(familyKey, refreshKey));

        TokenStorageServiceImpl service = serviceWithTtl(0L);

        assertThatThrownBy(() -> service.saveRefreshToken(familyId, "batch11-user", refreshToken))
                .isInstanceOf(DataAccessException.class);
        assertThat(redisTemplate.hasKey(familyKey)).isFalse();
        assertThat(redisTemplate.hasKey(refreshKey)).isFalse();
    }

    private TokenStorageServiceImpl serviceWithTtl(long ttlSeconds) {
        TokenStorageServiceImpl service = new TokenStorageServiceImpl(redisTemplate);
        ReflectionTestUtils.setField(service, "REFRESHABLE_DURATION", ttlSeconds);
        return service;
    }
}
