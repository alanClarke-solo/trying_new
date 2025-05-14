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