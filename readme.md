Basic directory structure for a Spring Boot project with the appropriate packages:

```
C:\projects\ai\cache\
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── inventory/
│   │   │               ├── InventoryApplication.java
│   │   │               ├── config/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               ├── service/
│   │   │               ├── controller/
│   │   │               ├── listener/
│   │   │               └── event/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── inventory/
└── pom.xml
```


```java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectCreator {
    public static void main(String[] args) {
        String baseDir = "C:\\projects\\ai\\cache";
        
        // Create base directory if it doesn't exist
        Path basePath = Paths.get(baseDir);
        if (!Files.exists(basePath)) {
            try {
                Files.createDirectories(basePath);
                System.out.println("Created base directory: " + baseDir);
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + e.getMessage());
                return;
            }
        }
        
        // Create project structure
        createProjectStructure(baseDir);
    }
    
    private static void createProjectStructure(String baseDir) {
        // Create directories
        String[] directories = {
            "src/main/java/com/example/inventory",
            "src/main/java/com/example/inventory/config",
            "src/main/java/com/example/inventory/controller",
            "src/main/java/com/example/inventory/event",
            "src/main/java/com/example/inventory/listener",
            "src/main/java/com/example/inventory/model",
            "src/main/java/com/example/inventory/repository",
            "src/main/java/com/example/inventory/service",
            "src/main/resources"
        };
        
        for (String dir : directories) {
            Path path = Paths.get(baseDir, dir);
            try {
                Files.createDirectories(path);
                System.out.println("Created directory: " + path);
            } catch (IOException e) {
                System.err.println("Failed to create directory " + path + ": " + e.getMessage());
            }
        }
        
        // Create files with content
        createProjectFiles(baseDir);
    }
    
    private static void createProjectFiles(String baseDir) {
        // Implementation to create the actual files with content
        // ...
    }
}
```

Create the main project directory:
```
C:\projects\ai\cache
```


## 2. Create the Project Directory Structure

Create the following directories:

```
C:\projects\ai\cache\src\main\java\com\example\inventory
C:\projects\ai\cache\src\main\java\com\example\inventory\config
C:\projects\ai\cache\src\main\java\com\example\inventory\controller
C:\projects\ai\cache\src\main\java\com\example\inventory\event
C:\projects\ai\cache\src\main\java\com\example\inventory\listener
C:\projects\ai\cache\src\main\java\com\example\inventory\model
C:\projects\ai\cache\src\main\java\com\example\inventory\repository
C:\projects\ai\cache\src\main\java\com\example\inventory\service
C:\projects\ai\cache\src\main\resources
```


## 3. Create the Project Files

Now, create the following files with their contents:

### 3.1. Main Application Class

**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\InventoryApplication.java`

```java
package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
```


### 3.2. Configuration Files

**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\config\RedisConfig.java`

```java
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
    public class NamespacedStringRedisSerializer extends StringRedisSerializer {
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
```


**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\config\LettuceConfig.java`

```java
package com.example.inventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import java.time.Duration;

@Configuration
public class LettuceConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        // Redis server configuration
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            serverConfig.setPassword(redisPassword);
        }

        // Lettuce client options
        ClientOptions clientOptions = ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();

        // Lettuce pooling configuration
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofMillis(redisTimeout))
                .readFrom(ReadFrom.REPLICA_PREFERRED) // Adjust if using Redis Sentinel or Cluster
                .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
```


**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\config\JacksonConfig.java`

```java
package com.example.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
```


### 3.3. Event System

**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\event\CacheInvalidationEvent.java`

```java
package com.example.inventory.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheInvalidationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cacheName;
    private String key;
}
```


### 3.4. Listeners

**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\listener\RedisPubSubListener.java`

```java
package com.example.inventory.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.inventory.event.CacheInvalidationEvent;
import com.example.inventory.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisPubSubListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    private final String redisNamespace;

    @Autowired
    public RedisPubSubListener(
            ObjectMapper objectMapper, 
            CacheService cacheService,
            @Value("${spring.redis.namespace:inventory}") String redisNamespace) {
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
        this.redisNamespace = redisNamespace;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            log.debug("Received Redis message via Lettuce on namespace {}: {}", 
                    redisNamespace, messageBody);
            
            CacheInvalidationEvent event = objectMapper.readValue(messageBody, CacheInvalidationEvent.class);
            
            // Invalidate the cache entry based on the received event
            log.debug("Invalidating cache: {}:{}", event.getCacheName(), event.getKey());
            cacheService.invalidateCache(event.getCacheName(), event.getKey());
            
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}
```


**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\listener\CacheUpdateListener.java`

```java
package com.example.inventory.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.inventory.event.CacheInvalidationEvent;
import com.example.inventory.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheUpdateListener {

    private final CacheService cacheService;

    /**
     * Handle cache invalidation events after a transaction is completed.
     * This ensures that the cache is only invalidated if the database transaction succeeds.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        log.debug("Handling cache invalidation event after transaction: {}", event);
        cacheService.invalidateCache(event.getCacheName(), event.getKey());
    }
}
```


### 3.5. Services

**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\service\CacheService.java`

```java
package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.inventory.event.CacheInvalidationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic cacheInvalidationTopic;
    private final ObjectMapper objectMapper;
    private final String redisNamespace;

    @Autowired
    public CacheService(
            CacheManager cacheManager, 
            RedisTemplate<String, Object> redisTemplate,
            ChannelTopic cacheInvalidationTopic,
            ObjectMapper objectMapper,
            @Value("${spring.redis.namespace:inventory}") String redisNamespace) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.cacheInvalidationTopic = cacheInvalidationTopic;
        this.objectMapper = objectMapper;
        this.redisNamespace = redisNamespace;
    }

    /**
     * Invalidates a specific cache entry
     * 
     * @param cacheName the name of the cache
     * @param key the key to invalidate
     */
    public void invalidateCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.debug("Invalidating cache entry: {}:{}", cacheName, key);
            cache.evict(key);
        }
    }

    /**
     * Invalidates an entire cache
     * 
     * @param cacheName the name of the cache to invalidate
     */
    public void invalidateEntireCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.debug("Invalidating entire cache: {}", cacheName);
            cache.clear();
        }
    }

    /**
     * Publishes a cache invalidation event to Redis pub/sub
     * 
     * @param cacheName the name of the cache
     * @param key the key to invalidate
     */
    public void publishCacheInvalidation(String cacheName, Object key) {
        CacheInvalidationEvent event = new CacheInvalidationEvent(cacheName, String.valueOf(key));
        try {
            String message = objectMapper.writeValueAsString(event);
            log.debug("Publishing cache invalidation event: {}", message);
            redisTemplate.convertAndSend(cacheInvalidationTopic.getTopic(), message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache invalidation event", e);
        }
    }
    
    /**
     * Store a value directly in Redis with namespace and TTL
     */
    public void storeValue(String key, Object value, long ttlSeconds) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * Retrieve a value directly from Redis with namespace
     */
    public Object getValue(String key) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }
    
    /**
     * Clear all keys in the current namespace
     */
    public void clearNamespace() {
        Set<String> keys = redisTemplate.keys(redisNamespace + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared all keys in namespace: {}", redisNamespace);
        }
    }
}
```


**File:** `C:\projects\ai\cache\src\main\java\com\example\inventory\service\NamespaceService.java`

```java
package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
public class NamespaceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String baseNamespace;
    private final ThreadLocal<String> tenantNamespace = new ThreadLocal<>();

    @Autowired
    public NamespaceService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${spring.redis.namespace:inventory}") String baseNamespace) {
        this.redisTemplate = redisTemplate;
        this.baseNamespace = baseNamespace;
    }

    /**
     * Set the current tenant namespace for the thread
     */
    public void setTenantNamespace(String tenantId) {
        tenantNamespace.set(tenantId);
        log.debug("Set tenant namespace to: {}", tenantId);
    }

    /**
     * Clear the tenant namespace from the thread
     */
    public void clearTenantNamespace() {
        tenantNamespace.remove();
    }

    /**
     * Get the current namespace (base + tenant if available)
     */
    public String getCurrentNamespace() {
        String tenant = tenantNamespace.get();
        if (tenant != null && !tenant.isEmpty()) {
            return baseNamespace + ":" + tenant;
        }
        return baseNamespace;
    }

    /**
     * Execute a Redis operation within a specific tenant namespace
     */
    public <T> T executeWithTenant(String tenantId, Function<String, T> operation) {
        String previousTenant = tenantNamespace.get();
        try {
            setTenantNamespace(tenantId);
            return operation.apply(getCurrentNamespace());
        } finally {
            if (previousTenant != null) {
                tenantNamespace.set(previousTenant);
            } else {
                tenantNamespace.remove();
            }
        }
    }

    /**
     * Clear all keys in a specific tenant namespace
     */
    public void clearTenantNamespace(String tenantId) {
        String namespace = baseNamespace + ":" + tenantId + ":*";
        Set<String> keys = redisTemplate.keys(namespace);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared all keys in tenant namespace: {}", namespace);
        }
    }
}
```


### 3.6. Configuration File

**File:** `C:\projects\ai\cache\src\main\resources\application.yml`

```yaml
spring:
  application:
    name: inventory-management-system
  
  # Database Configuration
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
    username: inventory_user
    password: secure_password
    driver-class-name: oracle.jdbc.OracleDriver
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
  
  # Redis Configuration
  redis:
    host: localhost
    port: 6379
    password: # Leave empty if no password
    timeout: 2000 # Connection timeout in milliseconds
    namespace: inventory-${spring.profiles.active:dev}  # Environment-specific namespace
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
  
  # Cache Configuration
  cache:
    type: redis
    redis:
      time-to-live:
        products: 3600    # 1 hour in seconds
        categories: 7200  # 2 hours in seconds
        suppliers: 7200   # 2 hours in seconds
      cache-null-values: false
      use-key-prefix: true

---
# Development profile
spring:
  config:
    activate:
      on-profile: dev
  redis:
    namespace: inventory-dev

---
# Testing profile
spring:
  config:
    activate:
      on-profile: test
  redis:
    namespace: inventory-test

---
# Staging profile
spring:
  config:
    activate:
      on-profile: staging
  redis:
    namespace: inventory-staging

---
# Production profile
spring:
  config:
    activate:
      on-profile: prod
  redis:
    namespace: inventory-prod
```


### 3.7. Maven Configuration

**File:** `C:\projects\ai\cache\pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>inventory</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>inventory</name>
    <description>Inventory Management System with Redis Cache</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JDBC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>
        
        <!-- Spring Redis with Lettuce -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <!-- Apache Commons Pool for Lettuce connection pooling -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        
        <!-- Spring Cache -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        
        <!-- Oracle Database -->
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc11</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```


### 3.8. README File

**File:** `C:\projects\ai\cache\README.md`

```markdown
# Redis Caching with Namespaces - Spring Boot Example

This project demonstrates a Spring Boot application with Redis caching using the Lettuce client and implementing namespace separation for better organization and multi-tenant support.

## Features

- Redis caching with Lettuce client
- Namespace-based key organization
- Environment-specific cache configuration
- Multi-tenant support
- Cache invalidation via Redis Pub/Sub
- Write-through caching strategy

## Architecture

The application implements a write-through caching strategy:
- Read operations use Redis cache to improve performance
- Write operations update both the cache and the database
- Cache invalidation is handled via Redis Pub/Sub to keep all application instances in sync
- Namespaces provide logical separation of keys for different environments or tenants

## Key Components

### 1. Redis Configuration

The application uses Lettuce as the Redis client with the following features:
- Connection pooling
- Namespace-based key prefixing
- Environment-specific cache configurations
- Custom serializers that handle namespacing automatically

### 2. Caching Strategy

- **Time-To-Live (TTL)**: Different cache regions have configurable TTLs
- **Namespacing**: All keys are prefixed with the appropriate namespace
- **Cache Invalidation**: Redis Pub/Sub is used for cross-instance invalidation
- **Write-Through**: Database is always the source of truth

### 3. Multi-Tenant Support

The application supports multi-tenancy through:
- Tenant-specific namespaces
- Thread-local tenant context management
- Namespace service for tenant operations

## Getting Started

### Prerequisites

- JDK 17 or higher
- Redis server
- Oracle Database

### Configuration

The application is configured through the `application.yml` file. Key configurations include:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    namespace: inventory-${spring.profiles.active:dev}
  
  cache:
    redis:
      time-to-live:
        products: 3600    # 1 hour in seconds
        categories: 7200  # 2 hours in seconds
        suppliers: 7200   # 2 hours in seconds
```


### Environment-Specific Configurations

The application includes profile-specific configurations for:
- Development
- Testing
- Staging
- Production

Each environment has its own namespace to prevent key collisions.

## Key Classes

### Redis Configuration

- `RedisConfig`: Configures Redis connection, caching, and namespacing
- `LettuceConfig`: Configures Lettuce-specific features like connection pooling
- `NamespacedStringRedisSerializer`: Custom serializer that adds namespace prefixes

### Cache Services

- `CacheService`: Core service that handles cache operations and invalidation
- `NamespaceService`: Manages tenant-specific namespaces and context

### Listeners

- `RedisPubSubListener`: Listens for cache invalidation events across instances
- `CacheUpdateListener`: Processes cache events after database transactions

## Using the Cache

### Basic Caching

```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    // Method will only be executed if the key isn't in the cache
    return productRepository.findById(id);
}
```


### Cache Invalidation

```java
@CacheEvict(value = "products", key = "#id")
public void updateProduct(Long id, Product product) {
    productRepository.save(product);
    // Also publish invalidation to other instances
    cacheService.publishCacheInvalidation("products", id);
}
```


### Multi-Tenant Usage

```java
public Product getProductForTenant(String tenantId, Long productId) {
    return namespaceService.executeWithTenant(tenantId, namespace -> {
        // This operation will use the tenant-specific namespace
        return productService.getProduct(productId);
    });
}
```


## Monitoring and Management

### Redis Commands for Inspecting Caches

Monitor cache keys:
```
KEYS inventory-dev:*
```


Check cache contents:
```
GET inventory-dev:products:1
```


Clear all keys in a namespace:
```
EVAL "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" 0 "inventory-dev:*"
```


## Best Practices

1. **Use Appropriate TTLs**: Set TTLs based on data volatility
2. **Always Publish Invalidations**: Make sure all write operations publish invalidation events
3. **Monitor Cache Hit Ratios**: Adjust TTLs based on actual cache performance
4. **Use Clear Namespace Conventions**: Keep namespaces consistent and documented
5. **Consider Redis Memory**: Monitor Redis memory usage and adjust eviction policies

## Advanced Features

### 1. Namespace Isolation

The application supports complete namespace isolation for different environments or tenants:

```java
// Clear all keys in a specific namespace
cacheService.clearNamespace();

// Clear all keys for a specific tenant
namespaceService.clearTenantNamespace("tenant1");
```


### 2. Dynamic TTL Management

TTLs are configurable per cache region and can be adjusted at runtime:

```yaml
spring:
  cache:
    redis:
      time-to-live:
        products: 3600    # 1 hour
        categories: 7200  # 2 hours
        suppliers: 7200   # 2 hours
```


## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Me
recreate **File:** `C:\projects\ai\cache\README.md`


## AI Assistant
Let me recreate the README.md file with a more comprehensive structure and content:

**File:** `C:\projects\ai\cache\README.md`

```markdown
# Redis Caching with Lettuce Client and Namespace Support

This project demonstrates a robust caching implementation using Redis with the Lettuce client in a Spring Boot application. The implementation features namespace support for cache separation, making it ideal for multi-tenant applications and multi-environment deployments.

## Overview

This caching solution provides:

- **Advanced Redis Caching**: Using Lettuce client for high performance
- **Namespace-based Organization**: Logical separation of Redis keys
- **Multi-environment Support**: Different cache namespaces per environment
- **Multi-tenant Capabilities**: Tenant-specific cache isolation
- **Distributed Cache Invalidation**: Using Redis Pub/Sub for cross-instance synchronization

## Architecture

### Caching Pattern

The system implements a write-through caching strategy:
1. Read operations check the cache first, falling back to the database if needed
2. Write operations update both the database and the cache
3. Cache invalidation events are published via Redis Pub/Sub to all application instances

### Namespace Structure

Keys in Redis follow this pattern:
```

{namespace}:{cache-name}:{entity-id}
```
Examples:
- `inventory-dev:products:1001` - Product #1001 in dev environment
- `inventory-prod:categories:5` - Category #5 in production environment
- `inventory-client1:suppliers:42` - Supplier #42 for client1 tenant

## Key Components

### Configuration

- **RedisConfig**: Sets up Redis connection, templates, and cache managers with namespace support
- **LettuceConfig**: Configures Lettuce-specific features (connection pooling, client resources)
- **NamespacedStringRedisSerializer**: Custom serializer that automatically adds namespace prefixes

### Core Services

- **CacheService**: Provides methods for cache management, invalidation, and direct Redis operations
- **NamespaceService**: Handles namespace context for multi-tenant support

### Event System

- **CacheInvalidationEvent**: Event object for cache invalidation messages
- **RedisPubSubListener**: Listens for cache invalidation events from other instances
- **CacheUpdateListener**: Processes cache events after successful transactions

## Configuration Options

The cache behavior can be customized via application.yml settings:
```
yaml
spring:
redis:
host: localhost
port: 6379
namespace: inventory-${spring.profiles.active:dev}
timeout: 2000

cache:
type: redis
redis:
time-to-live:
products: 3600    # 1 hour in seconds
categories: 7200  # 2 hours in seconds
suppliers: 7200   # 2 hours in seconds
```
## Environment-specific Configuration

The application automatically uses different namespaces based on the active Spring profile:
```
yaml
---
# Development profile
spring:
config:
activate:
on-profile: dev
redis:
namespace: inventory-dev

---
# Testing profile
spring:
config:
activate:
on-profile: test
redis:
namespace: inventory-test

---
# Production profile
spring:
config:
activate:
on-profile: prod
redis:
namespace: inventory-prod
```
## Usage Examples

### Basic Caching
```
java
// Automatic caching with Spring's @Cacheable
@Service
public class ProductService {
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
return productRepository.findById(id)
.orElseThrow(() -> new NotFoundException("Product not found"));
}

    @CachePut(value = "products", key = "#product.id")
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        // Also notify other instances
        cacheService.publishCacheInvalidation("products", id);
    }
}
```
### Working with Multi-tenant Caches
```
java
@Service
public class TenantAwareProductService {
private final ProductService productService;
private final NamespaceService namespaceService;

    public Product getProductForTenant(String tenantId, Long productId) {
        return namespaceService.executeWithTenant(tenantId, namespace -> {
            return productService.getProduct(productId);
        });
    }
}
```
### Direct Cache Operations
```
java
// Manual cache operations when needed
@Service
public class CustomCacheService {
private final CacheService cacheService;

    public void storeComplexObject(String key, ComplexObject obj) {
        cacheService.storeValue(key, obj, 3600); // 1 hour TTL
    }
    
    public ComplexObject getComplexObject(String key) {
        return (ComplexObject) cacheService.getValue(key);
    }
    
    public void clearAllCachesForCurrentNamespace() {
        cacheService.clearNamespace();
    }
}
```
## Advanced Features

### 1. Cache Region-specific TTLs

Different types of data can have different expiration times:
```
java
// Products cache - 1 hour
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) { ... }

// Categories cache - 2 hours
@Cacheable(value = "categories", key = "#id")
public Category getCategory(Long id) { ... }
```
### 2. Programmatic Namespace Management
```
java
// Clear all keys in the current namespace
cacheService.clearNamespace();

// Work with a specific tenant's cache
namespaceService.executeWithTenant("client123", ns -> {
// Operations in this block use the client123 namespace
return someService.getData();
});

// Clear a specific tenant's cache
namespaceService.clearTenantNamespace("client123");
```
### 3. Redis Pub/Sub for Cache Synchronization

When a cache entry is updated or invalidated, the system publishes events to all instances:
```
java
// Publish cache invalidation event
cacheService.publishCacheInvalidation("products", productId);
```
Other instances receive the event via the `RedisPubSubListener` and update their local caches accordingly.

## Performance Considerations

- **TTL Settings**: Adjust TTLs based on data volatility and access patterns
- **Namespace Design**: Keep namespace hierarchies simple to avoid performance issues
- **Serialization**: The system uses JSON serialization by default (can be customized)
- **Key Expiration**: Redis handles key expiration automatically based on TTL
- **Memory Management**: Monitor Redis memory usage to prevent out-of-memory situations

## Redis Monitoring

Common Redis commands for monitoring the cache:
```

# List all keys in the dev namespace
KEYS inventory-dev:*

# Get details about a specific cache entry
GET inventory-dev:products:1001

# Monitor cache operations in real-time
MONITOR

# Get Redis memory usage statistics
INFO memory

# Delete all keys in a specific namespace
EVAL "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" 0 "inventory-dev:*"
```
## Dependencies

- Spring Boot 3.2.3+
- Spring Data Redis
- Lettuce Redis Client
- Apache Commons Pool (for connection pooling)
- Oracle Database (for persistent storage)
- Project Lombok (for reduced boilerplate)

## Getting Started

1. Ensure Redis is running and accessible
2. Configure database and Redis connection details in application.yml
3. Set the appropriate Spring profile for your environment
4. Start the application and monitoring Redis activity

## License

This project is licensed under the MIT License.
```

