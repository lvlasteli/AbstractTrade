-- Atomic add item to cart
-- KEYS[1] = cart metadata key (e.g., "anon_carts:{cartId}" or "user_carts:{userId}")
-- KEYS[2] = cart items key (e.g., "anon_cart_items:{cartId}" or "user_cart_items:{userId}")
-- ARGV[1] = sku
-- ARGV[2] = quantity to add
-- ARGV[3] = is_anonymous (1 for anonymous, 0 for user)
-- ARGV[4] = ttl_seconds (for anonymous carts)
-- ARGV[5] = currency (default "USD")
-- ARGV[6] = region (default "us-east-1")
-- Returns: new version number or error message

local cart_key = KEYS[1]
local items_key = KEYS[2]
local sku = ARGV[1]
local quantity = tonumber(ARGV[2])
local is_anonymous = tonumber(ARGV[3])
local ttl_seconds = tonumber(ARGV[4])
local currency = ARGV[5] or "USD"
local region = ARGV[6] or "us-east-1"
local item_key = "sku:" .. sku

if not quantity or quantity <= 0 then
    return {"err", "INVALID_QUANTITY"}
end

if quantity > 999 then
    return {"err", "MAX_QUANTITY_EXCEEDED"}
end

-- Check if cart exists
local cart_exists = redis.call("EXISTS", cart_key)
local version = 1

if cart_exists == 0 then
    -- Create cart metadata
    local now = redis.call("TIME")
    local timestamp = now[1] .. "." .. string.format("%06d", now[2])
    
    redis.call("HSET", cart_key, "currency", currency)
    redis.call("HSET", cart_key, "created_at", timestamp)
    redis.call("HSET", cart_key, "updated_at", timestamp)
    redis.call("HSET", cart_key, "status", "ACTIVE")
    redis.call("HSET", cart_key, "version", "1")
    redis.call("HSET", cart_key, "region", region)
    
    if is_anonymous == 1 and ttl_seconds > 0 then
        redis.call("EXPIRE", cart_key, ttl_seconds)
        redis.call("EXPIRE", items_key, ttl_seconds)
    end
else
    local version_str = redis.call("HGET", cart_key, "version")
    if version_str then
        version = tonumber(version_str)
    end
    
    local now = redis.call("TIME")
    local timestamp = now[1] .. "." .. string.format("%06d", now[2])
    redis.call("HSET", cart_key, "updated_at", timestamp)
    
    -- Refresh TTL for anonymous carts
    if is_anonymous == 1 and ttl_seconds > 0 then
        redis.call("EXPIRE", cart_key, ttl_seconds)
        redis.call("EXPIRE", items_key, ttl_seconds)
    end
end

local current_quantity = redis.call("HGET", items_key, item_key)
local new_quantity = quantity

if current_quantity then
    local current_qty = tonumber(current_quantity)
    if not current_qty then
        return {"err", "INVALID_CURRENT_QUANTITY"}
    end
    new_quantity = current_qty + quantity
end

if new_quantity > 999 then
    return {"err", "MAX_QUANTITY_EXCEEDED"}
end

redis.call("HSET", items_key, item_key, tostring(new_quantity))

version = version + 1
redis.call("HSET", cart_key, "version", tostring(version))

return {"ok", version}
