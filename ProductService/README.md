# Product Service

## Purpose

The Product Service is responsible for managing the **product catalog** and **category information** for the Ecommerce platform. It provides public facing APIs for browsing products, searching, and filtering by categories. The service will ensure that product information (prices, stock levels - not yet decided how to sync) are always up-to-date while optimizing category lookups through intelligent caching.

Key responsibilities include:

- **Product Catalog Management**: Store and serve product information including name, description, price, SKU, stock levels, and images
- **Category Management**: Organize products into categories with hierarchical relationships
- **Product Search**: Enable full-text search across product names and descriptions
- **Category Caching**: Optimize category lookups using Redis and extend it in the future to cache the most searched products and its statis info
- **Pagination Support**: Efficiently handle large product catalogs with configurable pagination

## Architecture

### High-Level Flow

```
Client Request
   ↓
Gateway Service (Port 8080)
   ↓
ProductService (Port 8083)
   ↓
┌─────────────────┬──────────────────┐
│                 │                  │
PostgreSQL        Redis              │
(product_db)      (redis-catalog)    │
│                 │                  │
Categories        Category Cache     │
Products          (TTL: 30 days)     │
```

### Data Flow

1. **Product Requests**: Always fetched fresh from PostgreSQL to ensure accurate stock and pricing
2. **Category Requests**: 
   - First request: Fetch from database, cache in Redis, return response
   - Subsequent requests: Return from cache (if available), but product count is always calculated dynamically
3. **Search Requests**: Full-text search across product name and description fields

## Challenges

1. **Real-time Inventory & Pricing**
   - Product prices and stock levels must be accurate at all times
   - Cannot cache product data as it changes frequently (stock updates, price changes)
   - Solution: Products are always fetched fresh from the database (for now)

2. **Category Performance**
   - Category information is relatively static but accessed frequently
   - Need to balance cache freshness with performance
   - Solution: Cache categories in Redis with 30-day TTL, but product counts are calculated dynamically

3. **Search Performance**
   - Full-text search across large product catalogs can be slow
   - Solution: Database indexes on name and description fields, pagination to limit result sets

4. **Scalability**
   - Product catalog can grow to millions of items
   - Solution: Pagination with configurable page sizes, efficient database queries with proper indexing

## Solution for Problems

### 1. Product Data Strategy

**Products are NOT cached** - Always fetched fresh from database:
- Ensures accurate stock levels for inventory management
- Guarantees up-to-date pricing for customers
- Prevents stale data issues that could lead to overselling or incorrect pricing

**Future Consideration**: Static product data (name, description, imageUrl) could be cached separately, but price and stock must always be rechecked from the database.

### 2. Category Caching Strategy

**Categories are cached in Redis** with intelligent fallback:
- **Cache Key Format**: `category:{categoryId}`
- **TTL**: 30 days (configurable via `product.category.ttl-seconds`)
- **Cache Invalidation**: Manual eviction (future: automatic on category update events)
- **Fallback**: If Redis is unavailable, service falls back to database directly
- **Product Count**: Always calculated dynamically (not cached) to ensure accuracy

### 3. Pagination

- **Default Page Size**: 20 items
- **Maximum Page Size**: 100 items
- **Response Format**: Consistent `PageResponse<T>` structure across all endpoints

## API Endpoints

All endpoints are **public** (no authentication required). See [TEST_CURL_COMMANDS.md](./TEST_CURL_COMMANDS.md) for detailed examples.

### Product Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products` | GET | List all products (paginated, default: page=0, size=20) |
| `/products/{id}` | GET | Get product details by UUID |
| `/products/search?q={query}` | GET | Search products by name/description (paginated) |

### Category Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products/categories` | GET | List all categories (paginated, default: page=0, size=20) |
| `/products/categories/{slug}` | GET | Get category by slug with product count |

### Query Parameters

- `page` (integer, default: 0) - Page number (0-indexed)
- `size` (integer, default: 20, max: 100) - Number of items per page
- `q` (string, required for search) - Search query

## Caching Strategy

### Category Caching

- **When**: Categories are cached on first `getCategoryById()` or `getCategoryBySlug()` request
- **Key Format**: `category:{categoryId}`
- **TTL**: 30 days (configurable via `product.category.ttl-seconds`)
- **Cache Content**: Category metadata (id, name, description, slug, imageUrl, createdAt)
- **NOT Cached**: Product count is always calculated dynamically from database
- **Fallback**: If Redis is unavailable, service automatically falls back to database

### Product Caching

- **Products are NOT cached** - Always fetched fresh from database, as stock levels and prices must be accurate and up-to-date
- **Future Consideration**: Static product data (name, description, imageUrl) could be cached separately, but price and stock must always be rechecked

## Monitoring & Observability

- **Product Query Performance**: Response times for product list, search, and detail endpoints
- **Category Cache Hit Rate**: Percentage of category requests served from cache
- **Database Query Performance**: Slow query detection for product searches
- **Redis Connection Health**: Cache availability and performance
- **Pagination Usage**: Average page size and page numbers requested

### Critical Alerts

- **Database Connection Failures**: Cannot serve product data
- **Redis Connection Failures**: Category caching unavailable (service still operational with fallback)
- **High Error Rates**: API endpoint failures
- **Slow Query Performance**: Database query degradation

## Internal Service Authentication

- All requests from Gateway must include `X-Gateway-Request` header with value from `GATEWAY_SERVICE_SECRET` environment variable
- ProductService validates both header value and source IP address
- Supports both local development and Docker environments
- IP whitelist configurable via `GATEWAY_ALLOWED_IPS` environment variable

## Summary

The Product Service provides a **scalable, performant product catalog** for the e-commerce platform:

- **Always Fresh Product Data**: Products are never cached, ensuring accurate inventory and pricing
- **Optimized Category Lookups**: Categories are cached in Redis with 30-day TTL and automatic fallback
- **Efficient Search**: Full-text search with proper database indexing and pagination
- **Public API**: All endpoints are publicly accessible (no authentication required)
- **Resilient Design**: Graceful degradation when Redis is unavailable
- **Scalable Architecture**: Pagination and efficient queries support large product catalogs

The service is designed to handle high traffic volumes while maintaining data accuracy and performance through intelligent caching strategies and database optimization.
