package com.restapi.wmsservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class TokenStorageServiceImplTest {

    private static final long REFRESHABLE_DURATION = 259200L;

    @Mock
    StringRedisTemplate stringRedisTemplate;

    TokenStorageServiceImpl tokenStorageService;

    @BeforeEach
    void setUp() {
        tokenStorageService = new TokenStorageServiceImpl(stringRedisTemplate);
        ReflectionTestUtils.setField(tokenStorageService, "REFRESHABLE_DURATION", REFRESHABLE_DURATION);
    }

    @Test
    void saveRefreshToken_executesOneAtomicScriptWithBothKeysAndTtl() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);

        tokenStorageService.saveRefreshToken("family-id", "test-user", "refresh-token");

        ArgumentCaptor<RedisScript> scriptCaptor = ArgumentCaptor.forClass(RedisScript.class);
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(scriptCaptor.capture(), keysCaptor.capture(), argsCaptor.capture());

        assertThat(keysCaptor.getValue())
                .containsExactly("rt_family:family-id", "rt:refresh-token");
        assertThat(argsCaptor.getValue())
                .containsExactly("refresh-token", "family-id:test-user", String.valueOf(REFRESHABLE_DURATION));
        assertThat(scriptCaptor.getValue().getScriptAsString())
                .contains("redis.call('SET', KEYS[1]", "redis.call('SET', KEYS[2]", "'EX', ttl");
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void saveRefreshToken_propagatesAtomicOperationFailure() {
        var redisFailure = new DataAccessResourceFailureException("Redis unavailable");
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenThrow(redisFailure);

        assertThatThrownBy(() -> tokenStorageService.saveRefreshToken("family-id", "test-user", "refresh-token"))
                .isSameAs(redisFailure);
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void saveRefreshToken_rejectsMissingAtomicOperationResult() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(null);

        assertThatThrownBy(() -> tokenStorageService.saveRefreshToken("family-id", "test-user", "refresh-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redis did not persist the refresh token family");
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void rotateRefreshToken_comparesAndReplacesFamilyTokenAtomically() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);

        boolean rotated = tokenStorageService.rotateRefreshToken(
                "family-id", "test-user", "old-refresh-token", "new-refresh-token");

        assertThat(rotated).isTrue();
        ArgumentCaptor<RedisScript> scriptCaptor = ArgumentCaptor.forClass(RedisScript.class);
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(scriptCaptor.capture(), keysCaptor.capture(), argsCaptor.capture());
        assertThat(keysCaptor.getValue())
                .containsExactly("rt_family:family-id", "rt:new-refresh-token");
        assertThat(argsCaptor.getValue()).containsExactly(
                "old-refresh-token", "new-refresh-token", "family-id:test-user",
                String.valueOf(REFRESHABLE_DURATION));
        assertThat(scriptCaptor.getValue().getScriptAsString())
                .contains("current ~= ARGV[1]", "redis.call('SET', KEYS[1]", "redis.call('SET', KEYS[2]");
    }
}
