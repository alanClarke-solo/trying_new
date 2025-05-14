e# Redis Caching with Namespaces - Spring Boot Example

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
-