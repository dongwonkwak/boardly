package com.boardly.shared.application.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 캐시 설정
 * 
 * <p>
 * 현재 사용 중인 캐시:
 * - users: 전체 User 객체 캐싱 (userId 기반)
 * - userNames: 사용자 이름 정보만 캐싱 (userId 기반)
 * - boardNames: 보드 이름 정보 캐싱 (boardId 기반)
 * </p>
 * 
 * <p>
 * 향후 개선 방향:
 * - Redis나 Caffeine 같은 고급 캐시 구현체 도입
 * - TTL 설정으로 캐시 만료 시간 관리
 * - 캐시 크기 제한 설정
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("userNames", "boardNames", "users", "userExists");
    }
}