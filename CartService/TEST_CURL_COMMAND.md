# CartService - cURL Test Commands

## Access Methods

The CartService can be accessed in two ways:

1. **Through Gateway Service (Recommended)** - Port `8080`
   - Gateway automatically adds required headers
   - Production-ready approach

2. **Direct to CartService (Testing Only)** - Port `8084`
   - Requires `X-Gateway-Request` header with secret value or set `auth.security.gateway.enabled=false`
   - Default secret: `gateway-secret-change-me`

---

## Option 1: Through Gateway Service (Recommended)

**Base URL:** `http://localhost:8080`

The Gateway automatically handles authentication headers and forwards requests to CartService.

### 1. View Cart

#### Get current cart (anonymous or authenticated)
```bash
curl -X GET "http://localhost:8080/cart" \
  -H "Content-Type: application/json"
```

#### Get cart with existing session
```bash
curl -X GET "http://localhost:8080/cart" \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

### 2. Add Items to Cart

#### Add item to cart (anonymous)
```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-001",
    "quantity": 2
  }' \
  -c cart_cookies.txt \
  -v
```

**Note:** The `-c cart_cookies.txt` flag saves the cart cookie. Use `-b cart_cookies.txt` in subsequent requests.

#### Add item to cart (authenticated user)
```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b session_cookies.txt \
  -d '{
    "sku": "ELEC-001",
    "quantity": 1
  }' \
  -v
```

#### Add multiple items
```bash
# Add first item
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": 2}' \
  -c cart_cookies.txt

# Add second item
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "BOOK-001", "quantity": 1}'

# Add third item
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "TOY-001", "quantity": 3}'
```

### 3. Update Item Quantity

#### Increase item quantity
```bash
curl -X PUT "http://localhost:8080/cart/items/ELEC-001" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{
    "quantity": 5
  }'
```

#### Decrease item quantity
```bash
curl -X PUT "http://localhost:8080/cart/items/ELEC-001" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{
    "quantity": 1
  }'
```

### 4. Remove Items from Cart

#### Remove specific item
```bash
curl -X DELETE "http://localhost:8080/cart/items/ELEC-001" \
  -b cart_cookies.txt
```

#### Remove multiple items
```bash
curl -X DELETE "http://localhost:8080/cart/items/ELEC-001" \
  -b cart_cookies.txt

curl -X DELETE "http://localhost:8080/cart/items/BOOK-001" \
  -b cart_cookies.txt
```

### 5. Clear Cart

#### Clear entire cart
```bash
curl -X DELETE "http://localhost:8080/cart" \
  -b cart_cookies.txt
```

### 6. Health Check

#### Check service health
```bash
curl -X GET "http://localhost:8080/actuator/health"
```

---

## Option 2: Direct to CartService (Testing Only)

**Base URL:** `http://localhost:8084`

**Required Header:** `X-Gateway-Request: gateway-secret-change-me`

### 1. View Cart

```bash
curl -X GET "http://localhost:8084/cart" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b cart_cookies.txt
```

### 2. Add Items to Cart

```bash
curl -X POST "http://localhost:8084/cart/items" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "sku": "ELEC-001",
    "quantity": 1
  }' \
  -c direct_cart.txt \
  -v
```

### 3. Update Item Quantity

```bash
curl -X PUT "http://localhost:8084/cart/items/ELEC-001" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b direct_cart.txt \
  -d '{
    "quantity": 3
  }'
```

### 4. Remove Item

```bash
curl -X DELETE "http://localhost:8084/cart/items/ELEC-001" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b direct_cart.txt
```

### 5. Clear Cart

```bash
curl -X DELETE "http://localhost:8084/cart" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b direct_cart.txt
```

### 6. Health Check

```bash
curl -X GET "http://localhost:8084/actuator/health"
```

---

## Complete Test Scenarios

### Scenario 1: Anonymous Shopping Cart Flow

```bash
# 1. Add first item to cart
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": 2}' \
  -c cart_cookies.txt \
  -v

# 2. View cart
curl -X GET "http://localhost:8080/cart" \
  -b cart_cookies.txt

# 3. Add second item
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "BOOK-001", "quantity": 1}'

# 4. Update first item quantity
curl -X PUT "http://localhost:8080/cart/items/ELEC-001" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"quantity": 5}'

# 5. View updated cart
curl -X GET "http://localhost:8080/cart" \
  -b cart_cookies.txt

# 6. Remove one item
curl -X DELETE "http://localhost:8080/cart/items/BOOK-001" \
  -b cart_cookies.txt

# 7. View cart after removal
curl -X GET "http://localhost:8080/cart" \
  -b cart_cookies.txt

# 8. Clear entire cart
curl -X DELETE "http://localhost:8080/cart" \
  -b cart_cookies.txt
```

### Scenario 2: Authenticated Shopping Cart Flow

```bash
# 1. Login first
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "SecurePass123!"
  }' \
  -c session_cookies.txt \
  -v

# 2. Add items to authenticated cart
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b session_cookies.txt \
  -d '{"sku": "ELEC-001", "quantity": 2}'

# 3. View cart
curl -X GET "http://localhost:8080/cart" \
  -b session_cookies.txt

# 4. Add more items
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b session_cookies.txt \
  -d '{"sku": "BOOK-001", "quantity": 3}'

# 5. View updated cart
curl -X GET "http://localhost:8080/cart" \
  -b session_cookies.txt

# 6. Logout
curl -X POST "http://localhost:8080/auth/logout" \
  -b session_cookies.txt
```

### Scenario 3: Cart Validation Testing

```bash
# 1. Try to add non-existent product (should fail - 404)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "NONEXISTENT-SKU", "quantity": 1}' \
  -v

# 2. Try to add inactive product (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "INACTIVE-PROD", "quantity": 1}' \
  -v

# 3. Try to add item with zero quantity (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": 0}' \
  -v

# 4. Try to add item with negative quantity (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": -5}' \
  -v

# 5. Try to add item with excessive quantity (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": 1000}' \
  -v

# 6. Try to add item without SKU (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}' \
  -v

# 7. Try to add item with blank SKU (should fail - 400)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "", "quantity": 1}' \
  -v

# 8. Add out-of-stock product (should succeed - backorder allowed)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "OUT-OF-STOCK-PROD", "quantity": 1}' \
  -c cart_cookies.txt \
  -v

# 9. Add valid product (should succeed)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "ELEC-001", "quantity": 2}' \
  -c cart_cookies.txt \
  -v
```

---

## Expected Response Formats

### Cart Response
```json
{
  "cartId": "cart:anon:550e8400-e29b-41d4-a716-446655440001",
  "items": [
    {
      "sku": "ELEC-001",
      "quantity": 2,
      "addedAt": "2026-01-30T12:00:00Z"
    },
    {
      "sku": "BOOK-001",
      "quantity": 1,
      "addedAt": "2026-01-30T12:05:00Z"
    }
  ],
  "totalItems": 3,
  "lastModified": "2026-01-30T12:05:00Z",
  "version": 2
}
```

### Add Item Success Response
```json
{
  "cartId": "cart:anon:550e8400-e29b-41d4-a716-446655440001",
  "items": [
    {
      "sku": "ELEC-001",
      "quantity": 2,
      "addedAt": "2026-01-30T12:00:00Z"
    }
  ],
  "message": "Item added successfully"
}
```

### Error Response (Product Not Found)
```json
{
  "timestamp": "2026-01-30T12:00:00Z",
  "status": 404,
  "error": "Product Not Found",
  "message": "Product not found with SKU: NONEXISTENT-SKU"
}
```

### Error Response (Validation Failed)
```json
{
  "timestamp": "2026-01-30T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "sku": "SKU is required",
    "quantity": "Quantity must be at least 1"
  }
}
```

### Error Response (Product Not Available)
```json
{
  "timestamp": "2026-01-30T12:00:00Z",
  "status": 400,
  "error": "Product Not Available",
  "message": "Product with SKU INACTIVE-PROD is not available for purchase"
}
```

---

## PowerShell Alternative Commands

For Windows PowerShell users:

### Add Item to Cart (Through Gateway)
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/cart/items" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"sku":"ELEC-001","quantity":2}' `
  -SessionVariable cartSession

# Use $cartSession for subsequent requests
Invoke-RestMethod -Uri "http://localhost:8080/cart" `
  -WebSession $cartSession
```

### View Cart
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cart" `
  -WebSession $cartSession
```

### Update Item Quantity
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cart/items/ELEC-001" `
  -Method Put `
  -ContentType "application/json" `
  -Body '{"quantity":5}' `
  -WebSession $cartSession
```

### Remove Item
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cart/items/ELEC-001" `
  -Method Delete `
  -WebSession $cartSession
```

### Clear Cart
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/cart" `
  -Method Delete `
  -WebSession $cartSession
```

---

