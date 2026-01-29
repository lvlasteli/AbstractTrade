-- Atomic update/remove item from cart
-- KEYS[1] = cart metadata key
-- KEYS[2] = cart items key
-- ARGV[1] = sku
-- ARGV[2] = new quantity (0 to remove)
-- ARGV[3] = is_anonymous (1 for anonymous, 0 for user)
-- ARGV[4] = ttl_seconds (for anonymous carts)
-- ARGV[5] = expected_version (for optimistic locking)
-- Returns: new version number or error message

local cart_key = KEYS[1]
local items_key = KEYS[2]
local sku = ARGV[1]
local new_quantity = tonumber(ARGV[2])
local is_anonymous = tonumber(ARGV[3])
local ttl_seconds = tonumber(ARGV[4])
local expected_version = tonumber(ARGV[5])
local item_key = "sku:" .. sku

if not new_quantity then
    return {"err", "INVALID_QUANTITY"}
end

if new_quantity > 999 then
    return {"err", "MAX_QUANTITY_EXCEEDED"}
end

local cart_exists = redis.call("EXISTS", cart_key)
if cart_exists == 0 then
    return {"err", "CART_NOT_FOUND"}
end

if expected_version then
    local current_version_str = redis.call("HGET", cart_key, "version")
    local current_version = tonumber(current_version_str)
    if current_version ~= expected_version then
        return {"err", "VERSION_MISMATCH", "current", current_version}
    end
end

local version_str = redis.call("HGET", cart_key, "version")
local version = tonumber(version_str) or 1

local now = redis.call("TIME")
local timestamp = now[1] .. "." .. string.format("%06d", now[2])
redis.call("HSET", cart_key, "updated_at", timestamp)

if is_anonymous == 1 and ttl_seconds > 0 then
    redis.call("EXPIRE", cart_key, ttl_seconds)
    redis.call("EXPIRE", items_key, ttl_seconds)
end

if new_quantity <= 0 then
    redis.call("HDEL", items_key, item_key)
else
    redis.call("HSET", items_key, item_key, tostring(new_quantity))
end

version = version + 1
redis.call("HSET", cart_key, "version", tostring(version))

return {"ok", version}
