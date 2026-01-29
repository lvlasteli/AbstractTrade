# AuthService - cURL Test Commands

## Access Methods

The AuthService can be accessed in two ways:

1. **Through Gateway Service (Recommended)** - Port `8080`
   - Gateway automatically adds required headers
   - Includes rate limiting and IP blocking
   - Production-ready approach

2. **Direct to AuthService (Testing Only)** - Port `8081`
   - Requires `X-Gateway-Request` header with secret value
   - Must originate from allowed IP (localhost/127.0.0.1 for local dev)
   - Default secret: `gateway-secret-change-me`

---

## Option 1: Through Gateway Service (Recommended)

**Base URL:** `http://localhost:8080`

The Gateway automatically handles authentication headers and forwards requests to AuthService.

### 1. User Registration

#### Register a new user
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "testuser",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

#### Register with minimal fields
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "minimal@example.com",
    "username": "minimaluser",
    "password": "Password123"
  }'
```

### 2. User Login

#### Login with email
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "SecurePass123!"
  }' \
  -c cookies.txt \
  -v
```

#### Login with username
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "SecurePass123!"
  }' \
  -c cookies.txt \
  -v
```

**Note:** The `-c cookies.txt` flag saves the session cookie. Use `-b cookies.txt` in subsequent requests to send the cookie.

### 3. Session Management

#### Get current user info
```bash
curl -X GET http://localhost:8080/auth/session/current \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

#### Refresh session
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -b cookies.txt \
  -H "Content-Type: application/json" \
  -c cookies.txt
```

#### Logout
```bash
curl -X POST http://localhost:8080/auth/logout \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

### 4. Password Recovery

#### Request password reset (forgot password)
```bash
curl -X POST http://localhost:8080/auth/password/forgot \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

**Note:** This always returns success to prevent email enumeration, even if the email doesn't exist.

#### Reset password with token
```bash
curl -X POST http://localhost:8080/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_RESET_TOKEN_HERE",
    "newPassword": "NewSecurePass123!"
  }'
```

### 5. Health Check

#### Check service health
```bash
curl -X GET http://localhost:8080/actuator/health \
  -H "Content-Type: application/json"
```

---

## Option 2: Direct to AuthService (Testing Only)

**Base URL:** `http://localhost:8081`  
**Required Header:** `X-Gateway-Request: gateway-secret-change-me`  
**Note:** Only works from localhost/127.0.0.1 in local development

### 1. User Registration

#### Register a new user
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "email": "user@example.com",
    "username": "testuser",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

#### Register with minimal fields
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "email": "minimal@example.com",
    "username": "minimaluser",
    "password": "Password123"
  }'
```

### 2. User Login

#### Login with email
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "identifier": "user@example.com",
    "password": "SecurePass123!"
  }' \
  -c cookies.txt \
  -v
```

#### Login with username
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "identifier": "testuser",
    "password": "SecurePass123!"
  }' \
  -c cookies.txt \
  -v
```

### 3. Session Management

#### Validate session (using cookie)
```bash
curl -X GET http://localhost:8081/auth/session/validate \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

#### Validate session (using header)
```bash
curl -X GET http://localhost:8081/auth/session/validate \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -H "X-Session-Id: YOUR_SESSION_ID_HERE" \
  -H "Content-Type: application/json"
```

#### Get current user info
```bash
curl -X GET http://localhost:8081/auth/session/current \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

#### Refresh session
```bash
curl -X POST http://localhost:8081/auth/refresh \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b cookies.txt \
  -H "Content-Type: application/json" \
  -c cookies.txt
```

#### Logout
```bash
curl -X POST http://localhost:8081/auth/logout \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

### 4. Password Recovery

#### Request password reset (forgot password)
```bash
curl -X POST http://localhost:8081/auth/password/forgot \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "email": "user@example.com"
  }'
```

#### Reset password with token
```bash
curl -X POST http://localhost:8081/auth/password/reset \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Request: gateway-secret-change-me" \
  -d '{
    "token": "YOUR_RESET_TOKEN_HERE",
    "newPassword": "NewSecurePass123!"
  }'
```

### 5. Health Check

#### Check service health (no header required)
```bash
curl -X GET http://localhost:8081/actuator/health \
  -H "Content-Type: application/json"
```

---

## Complete Test Flow (Through Gateway)

### 1. Register a new user
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "username": "newuser",
    "password": "TestPassword123!",
    "firstName": "Jane",
    "lastName": "Smith"
  }'
```

### 2. Login with the new user
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "newuser@example.com",
    "password": "TestPassword123!"
  }' \
  -c cookies.txt \
  -v
```

### 3. Get current user info
```bash
curl -X GET http://localhost:8080/auth/session/current \
  -b cookies.txt
```

### 4. Refresh session
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```

### 5. Logout
```bash
curl -X POST http://localhost:8080/auth/logout \
  -b cookies.txt
```

---

## Error Testing

### Invalid login credentials
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "WrongPassword"
  }'
```

### Invalid registration (duplicate email)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "anotheruser",
    "password": "Password123"
  }'
```

### Invalid registration (weak password)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "weak@example.com",
    "username": "weakuser",
    "password": "123"
  }'
```

### Access protected endpoint without session
```bash
curl -X GET http://localhost:8080/auth/session/current
```

### Invalid password reset token
```bash
curl -X POST http://localhost:8080/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "invalid-token-12345",
    "newPassword": "NewPassword123!"
  }'
```

### Direct access to AuthService without gateway header (should fail)
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "SecurePass123!"
  }'
```

---

## PowerShell Alternative Commands

For Windows PowerShell users:

### Register (Through Gateway)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/auth/register" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"email":"user@example.com","username":"testuser","password":"SecurePass123!","firstName":"John","lastName":"Doe"}'
```

### Login (Through Gateway, save cookies)
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"identifier":"user@example.com","password":"SecurePass123!"}' `
  -SessionVariable session

# Use $session for subsequent requests
Invoke-RestMethod -Uri "http://localhost:8080/auth/session/current" `
  -WebSession $session
```

### Direct to AuthService (with header)
```powershell
$headers = @{
    "Content-Type" = "application/json"
    "X-Gateway-Request" = "gateway-secret-change-me"
}

Invoke-RestMethod -Uri "http://localhost:8081/auth/register" `
  -Method Post `
  -Headers $headers `
  -Body '{"email":"user@example.com","username":"testuser","password":"SecurePass123!","firstName":"John","lastName":"Doe"}'
```

---

## Notes

- **Recommended Access**: Use Gateway Service (port 8080) for all requests in production and testing
- **Direct Access**: Direct access to AuthService (port 8081) requires:
  - `X-Gateway-Request` header with secret value (default: `gateway-secret-change-me`)
  - Request must originate from allowed IP (localhost/127.0.0.1 for local dev)
- **Session Cookies**: The service uses HttpOnly cookies, so cookies are automatically handled by curl when using `-c` (save) and `-b` (load) flags
- **Base URLs**: 
  - Gateway: `http://localhost:8080`
  - AuthService: `http://localhost:8081`
- **Content-Type**: All POST requests require `Content-Type: application/json`
- **Password Requirements**: Minimum 8 characters
- **Username Requirements**: 3-100 characters, alphanumeric and underscores only
- **Email Format**: Must be valid email format
- **Gateway Security**: AuthService endpoints are protected and only accept requests from Gateway Service. Direct access is only for local testing.
