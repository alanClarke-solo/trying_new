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
## AI Assistant
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
The application is configured through the file. Key configurations include: `application.yml`
``` yaml
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
- : Configures Redis connection, caching, and namespacing `RedisConfig`
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
``` java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    // Method will only be executed if the key isn't in the cache
    return productRepository.findById(id);
}
```
### Cache Invalidation
``` java
@CacheEvict(value = "products", key = "#id")
public void updateProduct(Long id, Product product) {
    productRepository.save(product);
    // Also publish invalidation to other instances
    cacheService.publishCacheInvalidation("products", id);
}
```
### Multi-Tenant Usage
``` java
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
``` java
// Clear all keys in a specific namespace
cacheService.clearNamespace();

// Clear all keys for a specific tenant
namespaceService.clearTenantNamespace("tenant1");
```
### 2. Dynamic TTL Management
TTLs are configurable per cache region and can be adjusted at runtime:
``` yaml
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

