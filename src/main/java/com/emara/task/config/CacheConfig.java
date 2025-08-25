package com.emara.task.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
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
@EnableCaching
public class CacheConfig {

    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // Register JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Create custom serializer with proper Java 8 time support
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        // Custom cache configurations for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - longer TTL since user data changes rarely
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Employee/Manager cache - moderate TTL since role changes are rare
        cacheConfigurations.put("employees", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("managers", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Department cache - long TTL since departments rarely change
        cacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Task statistics cache - shorter TTL since statistics change frequently
        cacheConfigurations.put("task-stats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Task cache - moderate TTL for task lists
        cacheConfigurations.put("tasks", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
