package com.sonkim.bookmarking.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        // 캐시 기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(30));

        // 캐시 종류 별 유효시간 설정
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("ogData", defaultConfig.entryTtl(Duration.ofHours(12)));        // OG 데이터는 12시간

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();

    }
}
