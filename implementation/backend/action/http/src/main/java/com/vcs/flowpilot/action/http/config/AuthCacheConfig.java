package com.vcs.flowpilot.action.http.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
class AuthCacheConfig {

//    @Bean
//    Cache<String, AuthConnection> authConnections() {
//        return Caffeine
//                .newBuilder()
//                .expireAfter(new Expiry<String, AuthConnection>() {
//
//                    @Override public long expireAfterCreate(String k, AuthConnection v, long nowNanos) {
//                        return ttlNanos(v);
//                    }
//
//                    @Override public long expireAfterUpdate(String k, AuthConnection v, long nowNanos, long currentDuration) {
//                        return ttlNanos(v);
//                    }
//
//                    @Override public long expireAfterRead(String k, AuthConnection v, long nowNanos, long currentDuration) {
//                        return currentDuration;
//                    }
//
//                    private long ttlNanos(AuthConnection v) {
//                        long nowSec = Instant.now().getEpochSecond();
//                        long ttlSec = Math.max(1, v.expiresAtEpochSec - nowSec);
//                        return TimeUnit.SECONDS.toNanos(ttlSec);
//                    }
//                })
//                .maximumSize(500)
//                .build();
//    }
}