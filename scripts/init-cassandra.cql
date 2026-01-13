-- Cassandra Initialization Script
-- This script creates the cart_keyspace and all basic tables for simple cart managment
-- Replication factor of 3 ensures all nodes have all data

CREATE KEYSPACE IF NOT EXISTS cart_keyspace
WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'DC1': 3
} AND durable_writes = true;

USE cart_keyspace;

-- ==================== Authenticated User Carts ====================
-- Main cart table for logged-in users
-- Partition by user_id for efficient user-based queries
CREATE TABLE IF NOT EXISTS user_carts (
    user_id UUID,
    cart_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status TEXT,  -- active, checked_out, abandoned
    PRIMARY KEY (user_id, cart_id)
) WITH CLUSTERING ORDER BY (cart_id DESC)
  AND default_time_to_live = 7776000  -- 90 days in seconds
  AND comment = 'Main cart records for authenticated users';


CREATE TABLE IF NOT EXISTS user_cart_items (
    cart_id UUID,
    product_id UUID,
    quantity INT,
    price DECIMAL,
    added_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (cart_id, product_id)
) WITH CLUSTERING ORDER BY (product_id ASC)
  AND comment = 'Individual items in authenticated user carts';


-- ==================== Anonymous User Carts ====================

CREATE TABLE IF NOT EXISTS anon_carts (
    session_id TEXT,
    cart_id UUID,
    fingerprint TEXT,  -- Device fingerprint for validation
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    ip_address TEXT,
    PRIMARY KEY (session_id, cart_id)
) WITH CLUSTERING ORDER BY (cart_id DESC)
  AND default_time_to_live = 2592000  -- 30 days in seconds
  AND comment = 'Cart records for anon/guest users';


CREATE TABLE IF NOT EXISTS anon_cart_items (
    cart_id UUID,
    product_id UUID,
    quantity INT,
    price DECIMAL,
    added_at TIMESTAMP,
    PRIMARY KEY (cart_id, product_id)
) WITH CLUSTERING ORDER BY (product_id ASC)
  AND default_time_to_live = 2592000  -- 30 days in seconds
  AND comment = 'Individual items in anon/guest carts';

