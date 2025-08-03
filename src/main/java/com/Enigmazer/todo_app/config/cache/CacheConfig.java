package com.Enigmazer.todo_app.config.cache;

import com.Enigmazer.todo_app.constants.CacheNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.List;

/**
 * CacheConfig class manages cache managers
 * and how they store the cache
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * For in memory per-instance cache
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        log.debug("initializing caffeine cache manager");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(CacheNameConstants.getCaffeineCacheNamesSet());

        log.info("caffeine cache manager is successfully initialized");
        return cacheManager;
    }

    /**
     * For centralized, distributed, shared caching (for multi-instance)
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.debug("initializing redis cache manager");
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15));

        log.info("redis cache manager is successfully initialized");
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .initialCacheNames(CacheNameConstants.getRedisCacheNamesSet())
                .build();
    }

    /**
     * Initializes a CompositeCacheManager that combines multiple cache managers.
     * This allows Spring to delegate caching operations to the first cache manager
     * that supports a given cache name (e.g., Caffeine, Redis).
     *
     * Throw an error if a requested cache is not found in any configured manager,
     * due to fallbackToNoOpCache being set to false.
     */

    @Primary
    @Bean
    public CacheManager cacheManager(CaffeineCacheManager caffeineCacheManager,
                                     RedisCacheManager redisCacheManager){
        log.debug("initializing CompositeCacheManager manager");
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        compositeCacheManager.setCacheManagers(List.of(caffeineCacheManager, redisCacheManager));
        compositeCacheManager.setFallbackToNoOpCache(false); // throw error if no matching cache found
        log.info("cache manager is successfully initialized");
        return compositeCacheManager;
    }
}
