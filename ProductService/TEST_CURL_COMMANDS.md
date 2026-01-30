# ProductService - cURL Test Commands

## Access Methods

The ProductService can be accessed in two ways:

1. **Through Gateway Service (Recommended)** - Port `8080`
   - Gateway automatically adds required headers
   - Production-ready approach

2. **Direct to ProductService (Testing Only)** - Port `8083`
   - Requires `X-Gateway-Request` header with secret value or set `auth.security.gateway.enabled=false`
   - Default secret: `gateway-secret-change-me`

---

## Option 1: Through Gateway Service (Recommended)

**Base URL:** `http://localhost:8080`

The Gateway automatically handles authentication headers and forwards requests to ProductService.

### 1. List All Products (Paginated)

#### Get first page (default: 20 items)
```bash
curl -X GET "http://localhost:8080/products" \
  -H "Content-Type: application/json"
```

#### Get specific page with custom size
```bash
curl -X GET "http://localhost:8080/products?page=0&size=10" \
  -H "Content-Type: application/json"
```

#### Get second page
```bash
curl -X GET "http://localhost:8080/products?page=1&size=20" \
  -H "Content-Type: application/json"
```

#### Test pagination validation (should fail)
```bash
curl -X GET "http://localhost:8080/products?page=-1&size=20" \
  -H "Content-Type: application/json"
```

```bash
curl -X GET "http://localhost:8080/products?page=0&size=101" \
  -H "Content-Type: application/json"
```

### 2. Get Product by ID

#### Get a specific product
```bash
curl -X GET "http://localhost:8080/products/550e8400-e29b-41d4-a716-446655440001" \
  -H "Content-Type: application/json"
```

**Note:** Replace the UUID with an actual product ID from your database. Example UUIDs from init script:
- Electronics: `550e8400-e29b-41d4-a716-446655440001` (category)
- Books: `550e8400-e29b-41d4-a716-446655440002` (category)

#### Test with invalid UUID (should return 404)
```bash
curl -X GET "http://localhost:8080/products/00000000-0000-0000-0000-000000000000" \
  -H "Content-Type: application/json"
```

### 3. Search Products

#### Search by name or description
```bash
curl -X GET "http://localhost:8080/products/search?q=wireless" \
  -H "Content-Type: application/json"
```

#### Search with pagination
```bash
curl -X GET "http://localhost:8080/products/search?q=coffee&page=0&size=5" \
  -H "Content-Type: application/json"
```

#### Search for non-existent products
```bash
curl -X GET "http://localhost:8080/products/search?q=nonexistentproductxyz" \
  -H "Content-Type: application/json"
```

### 4. List All Categories

#### Get all categories (ordered by name)
```bash
curl -X GET "http://localhost:8080/products/categories" \
  -H "Content-Type: application/json"
```

### 5. Get Category by Slug with Products

#### Get category with product count
```bash
curl -X GET "http://localhost:8080/products/categories/electronics" \
  -H "Content-Type: application/json"
```

#### Get category with pagination parameters (for products)
```bash
curl -X GET "http://localhost:8080/products/categories/electronics?page=0&size=10" \
  -H "Content-Type: application/json"
```

**Note:** Category slugs from init script:
- Electronics: `electronics`
- Books: `books`
- Anime & Manga: `anime-manga`
- Toys: `toys`
- Beauty: `beauty`
- Automotive: `automotive`
- Food & Beverages: `food-beverages`
- Health: `health`
- Clothing: `clothing`
- Board Games: `board-games`

---

## Option 2: Direct to ProductService (Testing Only)

**Base URL:** `http://localhost:8083`

**Required Header:** `X-Gateway-Request: gateway-secret-change-me`

### 1. List All Products (Paginated)

```bash
curl -X GET "http://localhost:8083/products?page=0&size=20" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

### 2. Get Product by ID

```bash
curl -X GET "http://localhost:8083/products/550e8400-e29b-41d4-a716-446655440001" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

### 3. Search Products

```bash
curl -X GET "http://localhost:8083/products/search?q=bluetooth&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

### 4. List All Categories

```bash
curl -X GET "http://localhost:8083/products/categories" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

### 5. Get Category by Slug

```bash
curl -X GET "http://localhost:8083/products/categories/electronics?page=0&size=20" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me"
```

---

## Testing Scenarios

### Test Pagination

```bash
# First page
curl -X GET "http://localhost:8080/products?page=0&size=5"

# Second page
curl -X GET "http://localhost:8080/products?page=1&size=5"

# Third page
curl -X GET "http://localhost:8080/products?page=2&size=5"
```

### Test Search Functionality

```bash
# Search for electronics
curl -X GET "http://localhost:8080/products/search?q=electronics"

# Search for books
curl -X GET "http://localhost:8080/products/search?q=book"

# Search for anime
curl -X GET "http://localhost:8080/products/search?q=anime"

# Case-insensitive search
curl -X GET "http://localhost:8080/products/search?q=WIRELESS"
```

### Test Category Filtering

```bash
# Get Electronics category
curl -X GET "http://localhost:8080/products/categories/electronics"

# Get Books category
curl -X GET "http://localhost:8080/products/categories/books"

# Get Anime & Manga category
curl -X GET "http://localhost:8080/products/categories/anime-manga"

# Get Toys category
curl -X GET "http://localhost:8080/products/categories/toys"

# Get Beauty category
curl -X GET "http://localhost:8080/products/categories/beauty"
```

### Test Error Handling

```bash
# Invalid product ID (404)
curl -X GET "http://localhost:8080/products/invalid-uuid"

# Invalid category slug (404)
curl -X GET "http://localhost:8080/products/categories/invalid-slug"

# Invalid pagination (400)
curl -X GET "http://localhost:8080/products?page=-5&size=200"
```

### Test Category Caching

```bash
# First request (should hit database and cache)
curl -X GET "http://localhost:8080/products/categories/electronics"

# Second request (should hit cache)
curl -X GET "http://localhost:8080/products/categories/electronics"
```

---

## Expected Response Formats

### Product Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Wireless Bluetooth Headphones",
  "description": "Premium noise-cancelling wireless headphones...",
  "price": 199.99,
  "sku": "ELEC-001",
  "stock": 150,
  "imageUrl": "https://example.com/images/products/headphones.jpg",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "categoryName": "Electronics",
  "isActive": true,
  "createdAt": "2025-01-27T10:00:00Z"
}
```

### Paginated Products Response
```json
{
  "content": [
    {
      "id": "...",
      "name": "...",
      ...
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false
}
```

### Category Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Electronics",
  "description": "Latest electronic devices and gadgets",
  "slug": "electronics",
  "imageUrl": "https://example.com/images/categories/electronics.jpg",
  "productCount": 10,
  "createdAt": "2025-01-27T10:00:00Z"
}
```

### Categories List Response
```json
[
  {
    "id": "...",
    "name": "...",
    ...
  }
]
```

### Error Response
```json
{
  "timestamp": "2025-01-27T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: ..."
}
```
## Notes

- All endpoints are **public** (no authentication required)
- Page numbers start at **0**
- Search is **case-insensitive** and searches both name and description
- Categories are **cached in Redis** for 30 days (configurable)
- Products are **always fetched fresh** from the database (not cached)
- Categories are accessed by **slug** (e.g., `electronics`, `books`, `anime-manga`) instead of UUID
- Product IDs in examples are UUIDs from the `init-postgres-product-db.sql` script
