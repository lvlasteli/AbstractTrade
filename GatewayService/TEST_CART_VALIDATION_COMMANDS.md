# Cart Item Addition with Product Validation - Test CURL Commands

This document contains curl commands to test the product validation flow when adding items to cart.

## Prerequisites

- Gateway Service running on `http://localhost:8080`
- Product Service running on `http://localhost:8081`
- Cart Service running on `http://localhost:8082`
- Test products must exist in the Product Service database

## Environment Variables

```bash
GATEWAY_URL="http://localhost:8080"
```

---

## 1. Add Valid Product to Cart (Success)

Add an active product with available stock to cart.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 2
  }' \
  -v
```

**Expected Response**: `201 Created`
```json
{
  "cartId": "...",
  "items": [...],
  "message": "Item added successfully"
}
```

---

## 2. Add Out-of-Stock Product to Cart (Success)

Users can add out-of-stock products and wait for restocking.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-OUT-OF-STOCK",
    "quantity": 1
  }' \
  -v
```

**Expected Response**: `201 Created`
```json
{
  "cartId": "...",
  "items": [...],
  "message": "Item added successfully"
}
```

---

## 3. Add Large Quantity (Success)

Add a product with large quantity (users can backorder).

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 100
  }' \
  -v
```

**Expected Response**: `201 Created`

---

## 4. Add Non-Existent Product (Fail - 404)

Try to add a product that doesn't exist.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "NONEXISTENT-SKU",
    "quantity": 1
  }' \
  -v
```

**Expected Response**: `404 Not Found`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 404,
  "error": "Product Not Found",
  "message": "Product not found with SKU: NONEXISTENT-SKU"
}
```

---

## 5. Add Inactive Product (Fail - 400)

Try to add a product that is inactive.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "INACTIVE-PROD",
    "quantity": 1
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Product Not Available",
  "message": "Product with SKU INACTIVE-PROD is not available for purchase"
}
```

---

## 6. Missing SKU (Fail - 400 Validation)

Try to add item without SKU.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 1
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "sku": "SKU is required"
  }
}
```

---

## 7. Blank SKU (Fail - 400 Validation)

Try to add item with blank SKU.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "",
    "quantity": 1
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "sku": "SKU is required"
  }
}
```

---

## 8. Missing Quantity (Fail - 400 Validation)

Try to add item without quantity.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001"
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "quantity": "Quantity is required"
  }
}
```

---

## 9. Zero Quantity (Fail - 400 Validation)

Try to add item with zero quantity.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 0
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "quantity": "Quantity must be at least 1"
  }
}
```

---

## 10. Negative Quantity (Fail - 400 Validation)

Try to add item with negative quantity.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": -5
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "quantity": "Quantity must be at least 1"
  }
}
```

---

## 11. Quantity Exceeds Maximum (Fail - 400 Validation)

Try to add item with quantity > 999.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 1000
  }' \
  -v
```

**Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "quantity": "Quantity must not exceed 999"
  }
}
```

---

## 12. Add Item with Authentication (Authenticated Cart)

Add item to cart as an authenticated user.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -H "Cookie: AUTH_SESSION=your-session-token" \
  -d '{
    "sku": "PROD-001",
    "quantity": 3
  }' \
  -v
```

**Expected Response**: `201 Created`
- Cart will be associated with the authenticated user

---

## 13. Add Item as Anonymous User (Anonymous Cart)

Add item without authentication (anonymous cart).

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 1
  }' \
  -c cookies.txt \
  -v
```

**Expected Response**: `201 Created`
- Response will include `Set-Cookie` header with `ANON_CART_ID`
- Cookie is saved to `cookies.txt`

---

## 14. Continue Adding Items with Anonymous Cart Cookie

Use the anonymous cart cookie from previous request.

```bash
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "sku": "PROD-002",
    "quantity": 2
  }' \
  -v
```

**Expected Response**: `201 Created`
- Items will be added to the same anonymous cart

---

## 15. Direct Product Validation Endpoint (Optional)

Test the Product Service validation endpoint directly.

```bash
# Get product by SKU with quantity validation
curl -X GET "http://localhost:8081/products/sku/PROD-001?quantity=5" \
  -H "X-Gateway-Request: true" \
  -v

# Get product by SKU without quantity
curl -X GET "http://localhost:8081/products/sku/PROD-001" \
  -H "X-Gateway-Request: true" \
  -v
```

**Expected Response**: `200 OK`
```json
{
  "id": "...",
  "sku": "PROD-001",
  "name": "Product Name",
  "price": 99.99,
  "stock": 10,
  "isActive": true,
  "categoryId": "...",
  "categoryName": "Category",
  "createdAt": "2026-01-29T..."
}
```

---

## Testing Workflow

### Complete Test Scenario

```bash
# 1. Add first product to anonymous cart
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -d '{"sku": "PROD-001", "quantity": 2}' \
  -c cart_cookies.txt -v

# 2. Add another product to same cart
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "PROD-002", "quantity": 1}' -v

# 3. Try to add inactive product (should fail)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "INACTIVE-PROD", "quantity": 1}' -v

# 4. Add out-of-stock product (should succeed)
curl -X POST "http://localhost:8080/cart/items" \
  -H "Content-Type: application/json" \
  -b cart_cookies.txt \
  -d '{"sku": "OUT-OF-STOCK-PROD", "quantity": 5}' -v

# 5. View cart
curl -X GET "http://localhost:8080/cart" \
  -b cart_cookies.txt -v
```

---

## Notes

- **Stock Validation**: Stock levels are NOT validated. Users can add out-of-stock items.
- **Validation Rules**:
  - ✅ Product must exist
  - ✅ Product must be active (`isActive = true`)
  - ✅ SKU is required (not blank)
  - ✅ Quantity must be between 1 and 999
- **Cart Identity**: 
  - Authenticated users: cart is associated with user ID
  - Anonymous users: cart ID is stored in `ANON_CART_ID` cookie
- **Error Responses**: All errors return consistent JSON format with timestamp, status, error type, and message

---

## Troubleshooting

### Product Service Returns 403 (Gateway Access Denied)
Ensure the request includes the `X-Gateway-Request: true` header when calling Product Service directly.

### Cart Service Not Creating Cart
Check that the Cart Service is running and Redis is accessible.

### Validation Not Working
Verify that the Product Service has the product data seeded and `isActive` flags are set correctly.
