# CartService

## Purpose

The **CartService** provides fast, scalable cart management supporting both anonymous (guest) and authenticated users. It uses Redis for high-performance cart operations with atomic Lua scripts, optimistic locking, and sliding TTL for anonymous carts.

## Architecture

### Redis Keyspace Structure

#### Authenticated User Carts
- **`user_carts:{userId}`** (HASH) - Cart metadata
  - Fields: `currency`, `created_at`, `updated_at`, `status`, `version`, `region`
  - TTL: None or 90 days (configurable)
  
- **`user_cart_items:{userId}`** (HASH) - Cart items
  - Fields: `sku:{skuId}` -> `quantity` (integer)
  - TTL: Same as cart metadata

#### Anonymous User Carts
- **`anon_carts:{cartId}`** (HASH) - Cart metadata
  - Fields: `currency`, `created_at`, `updated_at`, `version`, `region`
  - TTL: 24-72 hours (sliding, refreshed on writes only)
  
- **`anon_cart_items:{cartId}`** (HASH) - Cart items
  - Fields: `sku:{skuId}` -> `quantity` (integer)
  - TTL: Same as cart metadata (sliding)

### Key Features

1. **Atomic Operations**: Lua scripts ensure atomic add/update operations
2. **Optimistic Locking**: Version field prevents concurrent modification conflicts
3. **Sliding TTL**: Anonymous cart TTL refreshed on writes, not reads
4. **Cookie-Based Identification**: Anonymous carts identified via `anon_cart_id` cookie
5. **Constraints**: Max 100 items per cart, max 999 quantity per SKU

## Cookie Expiry Refresh

When CartService performs write operations (POST, PUT, DELETE items) for anonymous carts:
1. Redis TTL is refreshed (sliding TTL)
2. Response includes header: `X-Cart-Cookie-Refresh: true`
3. Gateway intercepts this header and refreshes `anon_cart_id` cookie expiry
4. Cookie expiry stays synchronized with Redis TTL

## Future: Cart Merge Strategy

When a user logs in, their anonymous cart should be merged into their user cart:

1. Gateway calls `/cart/merge` endpoint after successful login
2. Lua script atomically merges `anon_cart_items:{cartId}` into `user_cart_items:{userId}`
3. Merge rules:
   - Sum quantities for same SKU
   - User cart currency wins on mismatch
   - Delete anonymous cart after successful merge
4. Implementation deferred to future task

