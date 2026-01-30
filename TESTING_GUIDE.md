# AbstractTrade - Docker Services Testing Guide

This guide provides an overview of all services, health checks, and Docker management commands. For detailed API endpoint testing, see the service-specific documentation below.

## API Documentation References

For detailed API endpoint testing and cURL commands, see:

- **Auth Service**: [AuthService/TEST_CURL_COMMANDS.md](./AuthService/TEST_CURL_COMMANDS.md)
  - User registration, login, logout
  - Session management and validation
  - Password reset flow
  - Error scenarios and validation testing

- **Product Service**: [ProductService/TEST_CURL_COMMANDS.md](./ProductService/TEST_CURL_COMMANDS.md)
  - Product listing and pagination
  - Product search
  - Category management
  - Product validation

- **Cart Service**: [CartService/TEST_CURL_COMMANDS.md](./CartService/TEST_CURL_COMMANDS.md)
  - Add/update/remove cart items
  - Anonymous and authenticated cart flows
  - Cart validation scenarios
  - Complete shopping workflows

- **Cart Validation (Gateway)**: [GatewayService/TEST_CART_VALIDATION_COMMANDS.md](./GatewayService/TEST_CART_VALIDATION_COMMANDS.md)
  - Product validation when adding to cart
  - Edge cases and error handling
  - Stock and availability testing

- **Gateway Endpoints**: [GatewayService/ENDPOINTS.md](./GatewayService/ENDPOINTS.md)
  - Complete API endpoint reference
  - Rate limiting and security
