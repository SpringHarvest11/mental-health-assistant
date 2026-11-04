package com.nie.mentalserver.service.impl;

import com.nie.mentalserver.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String KEY_PREFIX = "logout:jti:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String jti, long remainingMillis) {
        if (jti == null || jti.isBlank() || remainingMillis <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + jti,
                    String.valueOf(System.currentTimeMillis()),
                    Duration.ofMillis(remainingMillis));
        } catch (Exception e) {
            // 黑名单写入失败不能影响退出流程，按 fail-open 处理
            log.warn("写入令牌黑名单失败, jti={}", jti, e);
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            // Redis 不可用时放行，避免鉴权链路整体不可用
            log.warn("查询令牌黑名单失败, jti={}", jti, e);
            return false;
        }
    }
}
