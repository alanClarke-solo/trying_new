
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