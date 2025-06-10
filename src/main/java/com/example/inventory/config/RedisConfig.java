package com.example.inventory.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.inventory.listener.RedisPubSubListener;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    @Value("${spring.redis.namespace:inventory}")
    private String redisNamespace;

    @Value("${spring.cache.redis.time-to-live.products:3600}")
    private long productsTtl;

    @Value("${spring.cache.redis.time-to-live.categories:7200}")
    private long categoriesTtl;

    @Value("${spring.cache.redis.time-to-live.suppliers:7200}")
    private long suppliersTtl;

    private final ObjectMapper objectMapper;

    public RedisConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use StringRedisSerializer with namespace prefix for keys
        NamespacedStringRedisSerializer keySerializer = new NamespacedStringRedisSerializer(redisNamespace);
        template.setKeySerializer(keySerializer);

        // Use Jackson JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // StringRedisSerializer for hash keys (with namespace)
        template.setHashKeySerializer(keySerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration with TTL and namespace
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .computePrefixWith(cacheName -> redisNamespace + ":" + cacheName + ":")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper)));

        // Create cache configurations with different TTLs for different cache names
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Products cache configuration
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofSeconds(productsTtl)));

        // Categories cache configuration
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofSeconds(categoriesTtl)));

        // Suppliers cache configuration
        cacheConfigurations.put("suppliers", defaultConfig.entryTtl(Duration.ofSeconds(suppliersTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisPubSubListener redisPubSubListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to the cache invalidation channel with namespace
        container.addMessageListener(redisPubSubListener,
                new ChannelTopic(redisNamespace + ":cache:invalidation"));

        return container;
    }

    @Bean
    public ChannelTopic cacheInvalidationTopic() {
        return new ChannelTopic(redisNamespace + ":cache:invalidation");
    }

    /**
     * Custom Redis serializer that adds namespace to keys
     */
    public static class NamespacedStringRedisSerializer extends StringRedisSerializer {
        private final String namespace;

        public NamespacedStringRedisSerializer(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public byte[] serialize(String string) {
            if (string == null) {
                return null;
            }
            return super.serialize(namespace + ":" + string);
        }

        @Override
        public String deserialize(byte[] bytes) {
            String key = super.deserialize(bytes);
            if (key != null && key.startsWith(namespace + ":")) {
                return key.substring((namespace + ":").length());
            }
            return key;
        }
    }
}