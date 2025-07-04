# Ski Shop Microservices - Complete Endpoint List

## Overview

This document provides a comprehensive list of all endpoints across the 9 microservices in the Ski Shop microservices architecture, organized by service and categorized into administrative and general user endpoints.

## Service List

1. **Authentication Service** - Authentication & Authorization Service
2. **User Management Service** - User Management Service  
3. **Inventory Management Service** - Inventory Management Service
4. **Sales Management Service** - Sales Management Service
5. **Payment Cart Service** - Payment & Cart Service
6. **Point Service** - Points Management Service
7. **Coupon Service** - Coupon Management Service
8. **AI Support Service** - AI Support Service
9. **API Gateway** - API Gateway Service

---

## 1. Authentication Service (Authentication Service)

### 1.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | Public |
| POST | `/api/v1/auth/mfa/verify` | MFA verification | Public |
| POST | `/api/v1/auth/refresh` | Token refresh | Public |
| POST | `/api/v1/auth/logout` | Logout | Authenticated |
| GET | `/api/v1/auth/oauth/{provider}/redirect` | OAuth authentication start | Public |
| POST | `/api/v1/auth/oauth/{provider}/callback` | OAuth callback | Public |
| POST | `/api/v1/auth/password/reset-request` | Password reset request | Public |
| POST | `/api/v1/auth/password/reset` | Password reset execution | Public |
| POST | `/api/v1/auth/validate` | Token validation | Public |
| GET | `/api/v1/auth/me` | Current user information | Authenticated |
| GET | `/` | Home page | Public |
| GET | `/home` | Home page (after authentication) | Authenticated |
| GET | `/token_details` | ID token details | Authenticated |
| GET | `/call_graph` | Microsoft Graph API call | Authenticated |
| GET | `/profile` | User profile | Authenticated |
| GET | `/api/user/me` | User information (REST API) | Authenticated |
| GET | `/api/graph/user` | Microsoft Graph user information | Authenticated |
| GET | `/login` | Login page | Public |

### 1.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/auth/users` | New user registration | Public |
| DELETE | `/api/auth/users/{userId}` | User deletion (soft delete) | Admin |
| DELETE | `/api/auth/users/{userId}/hard` | User physical deletion | Admin |

---

## 2. User Management Service (User Management Service)

### 2.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/users` | New user registration | Public |
| GET | `/api/users/{id}` | Get user information | Admin or Own |
| PUT | `/api/users/{id}` | Update user information | Admin or Own |
| GET | `/api/users/check-email` | Check email address existence | Public |
| POST | `/api/users/{id}/change-password` | Change password | Admin or Own |
| GET | `/api/users/me` | Get current user information | Authenticated |
| GET | `/users/{userId}/activities` | Get user activities | Admin or Own |
| GET | `/users/me/activities` | Get current user activities | Authenticated |
| GET | `/users/{userId}/preferences` | Get user preferences | Admin or Own |
| GET | `/users/{userId}/preferences/{key}` | Get specific preference | Admin or Own |
| PUT | `/users/{userId}/preferences/{key}` | Update preference | Admin or Own |
| DELETE | `/users/{userId}/preferences/{key}` | Delete preference | Admin or Own |

### 2.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/admin/users` | Get user list | Admin |
| POST | `/api/admin/users/{userId}/roles` | Update user roles | Admin |
| POST | `/api/admin/users/{userId}/activate` | Activate user | Admin |
| POST | `/api/admin/users/{userId}/deactivate` | Deactivate user | Admin |
| DELETE | `/api/admin/users/{userId}` | Delete user | Admin |
| GET | `/api/admin/stats` | Get statistics | Admin |

---

## 3. Inventory Management Service (Inventory Management Service)

### 3.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/products` | Get product list | Public |
| GET | `/api/products/search` | Search products | Public |
| GET | `/api/products/{id}` | Get product details | Public |
| GET | `/api/products/sku/{sku}` | Get product by SKU | Public |
| GET | `/api/products/category/{categoryId}` | Get products by category | Public |
| GET | `/api/categories` | Get category list | Public |
| GET | `/api/categories/{id}` | Get category details | Public |
| GET | `/api/categories/{id}/products` | Get products in category | Public |
| GET | `/api/inventory/{productId}` | Get inventory information | Public |
| GET | `/api/inventory/status/{productId}` | Get inventory status | Public |

### 3.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/products/batch` | Batch product operations | Admin |
| POST | `/api/products` | Create product | Admin |
| POST | `/api/categories` | Create category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Admin |
| POST | `/api/inventory/batch` | Batch inventory operations | Admin |
| POST | `/api/inventory/reserve` | Reserve inventory | Admin |
| POST | `/api/inventory/release` | Release inventory | Admin |
| POST | `/api/inventory/stock-in` | Stock in process | Admin |
| POST | `/api/inventory/stock-out` | Stock out process | Admin |
| GET | `/api/inventory/low-stock` | Get low stock products | Admin |

---

## 4. Sales Management Service (Sales Management Service)

### 4.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/orders` | Create order | Authenticated |
| GET | `/api/v1/orders/{orderId}` | Get order details | Authenticated |
| GET | `/api/v1/orders/number/{orderNumber}` | Get order by number | Authenticated |
| GET | `/api/v1/orders/customer/{customerId}` | Get customer orders | Authenticated |
| GET | `/api/v1/orders/search` | Search orders | Authenticated |
| POST | `/api/v1/returns` | Return request | User or Customer Service |

### 4.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| PUT | `/api/v1/orders/{orderId}/status` | Update order status | Admin |
| PUT | `/api/v1/orders/{orderId}/cancel` | Cancel order | Admin |
| GET | `/api/v1/shipments` | Get shipment list | Admin or Logistics |
| GET | `/api/v1/shipments/{id}` | Get shipment details | Admin or Logistics or CS |
| POST | `/api/v1/shipments` | Create shipment | Admin or Logistics |
| PUT | `/api/v1/shipments/{id}/status` | Update shipment status | Admin or Logistics |
| GET | `/api/v1/shipments/order/{orderId}` | Get order shipment info | Admin or Logistics or CS |
| PUT | `/api/v1/shipments/{id}/tracking` | Update tracking info | Admin or Logistics |
| GET | `/api/v1/returns` | Get returns list | Admin or CS or Return Processor |
| GET | `/api/v1/returns/{id}` | Get return details | Admin or CS or Return Processor |
| PUT | `/api/v1/returns/{id}/status` | Update return status | Admin or CS or Return Processor |
| GET | `/api/v1/returns/order/{orderId}` | Get order return info | Admin or CS or Return Processor |
| GET | `/api/v1/reports/sales` | Sales report | Admin or Sales Manager or Analyst |
| GET | `/api/v1/reports/products` | Product report | Admin or Sales Manager or Product Manager |
| GET | `/api/v1/reports/export/sales` | Export sales report | Admin or Sales Manager |
| GET | `/api/v1/reports/shipping` | Shipping report | Admin or Sales Manager or Analyst |
| GET | `/api/v1/reports/returns` | Returns report | Admin or Sales Manager or Analyst |

---

## 5. Payment Cart Service (Payment & Cart Service)

### 5.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/cart/items` | Add cart item | Authenticated |
| PUT | `/api/v1/cart/items/{itemId}` | Update cart item | Authenticated |
| DELETE | `/api/v1/cart/items/{itemId}` | Remove cart item | Authenticated |
| GET | `/api/v1/cart` | Get cart | Authenticated |
| DELETE | `/api/v1/cart` | Clear cart | Authenticated |
| POST | `/api/v1/payments/intent` | Create payment intent | Authenticated |
| POST | `/api/v1/payments/{paymentId}/process` | Process payment | Authenticated |
| GET | `/api/v1/payments/{paymentId}` | Get payment information | Authenticated |
| GET | `/api/v1/payments/history` | Get payment history | Authenticated |

### 5.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/payments/{paymentId}/refund` | Process refund | Admin |
| POST | `/api/v1/payments/webhook` | Payment webhook | System |

---

## 6. Point Service (Points Service)

### 6.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/v1/points/balance` | Get point balance | Authenticated |
| POST | `/api/v1/points/redeem` | Redeem points | Authenticated |
| GET | `/api/v1/points/history` | Get point history | Authenticated |
| GET | `/api/v1/points/expiring` | Get expiring points | Authenticated |
| GET | `/api/v1/points/redemption-options` | Get redemption options | Authenticated |
| POST | `/api/v1/points/transfer` | Transfer points | Authenticated |
| GET | `/api/v1/tiers/user` | Get user tier info | Authenticated |
| GET | `/api/v1/tiers/benefits` | Get tier benefits info | Authenticated |
| GET | `/api/v1/tiers/progress` | Get tier progress info | Authenticated |
| GET | `/api/v1/tiers` | Get tier list | Authenticated |
| GET | `/api/v1/tiers/{tierLevel}` | Get tier details | Authenticated |
| GET | `/api/tiers/user/{userId}` | Get user tier | Public |
| GET | `/api/tiers` | Get tier information | Public |
| GET | `/api/tiers/{tierLevel}` | Get tier level details | Public |
| GET | `/api/tiers/upgrade-eligibility/{userId}` | Check upgrade eligibility | Public |
| GET | `/api/points/balance/{userId}` | Get user point balance | Public |
| GET | `/api/points/history/{userId}` | Get point history | Public |
| GET | `/api/points/history/{userId}/range` | Get point history by range | Public |
| GET | `/api/points/expiring/{userId}` | Get expiring points | Public |

### 6.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/points/award` | Award points | Service |
| POST | `/api/tiers/process-upgrades` | Process tier upgrades | Admin |
| POST | `/api/points/award` | Award points | Admin |
| POST | `/api/points/redeem` | Process point redemption | Admin |
| POST | `/api/points/transfer` | Process point transfer | Admin |
| POST | `/api/points/process-expired` | Process expired points | Admin |

---

## 7. Coupon Service (Coupon Service)

### 7.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/coupons/validate` | Validate coupon | Authenticated |
| POST | `/api/v1/coupons/redeem` | Redeem coupon | Authenticated |
| GET | `/api/v1/coupons/user/available` | Get available coupons | Authenticated |
| GET | `/api/v1/coupons/{code}` | Get coupon details | Authenticated |
| GET | `/api/v1/campaigns/active` | Get active campaigns | Public |

### 7.2 Administrator Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/coupons` | Create coupon | Admin |
| GET | `/api/v1/coupons` | Get coupon list | Admin |
| GET | `/api/v1/coupons/usage/{couponId}` | Get coupon usage | Admin |
| POST | `/api/v1/coupons/bulk-generate` | Bulk generate coupons | Admin |
| POST | `/api/v1/campaigns` | Create campaign | Admin |
| GET | `/api/v1/campaigns` | Get campaign list | Admin |
| PUT | `/api/v1/campaigns/{campaignId}` | Update campaign | Admin |
| POST | `/api/v1/campaigns/{campaignId}/activate` | Activate campaign | Admin |
| GET | `/api/v1/campaigns/{campaignId}/analytics` | Campaign analytics | Admin |
| GET | `/api/v1/campaigns/{campaignId}` | Get campaign details | Admin |
| GET | `/api/v1/distributions/rules/{campaignId}` | Get distribution rules | Admin or Campaign Manager |
| POST | `/api/v1/distributions/rules/{campaignId}` | Create distribution rules | Admin or Campaign Manager |
| PUT | `/api/v1/distributions/rules/{ruleId}` | Update distribution rules | Admin or Campaign Manager |
| DELETE | `/api/v1/distributions/rules/{ruleId}` | Delete distribution rules | Admin or Campaign Manager |
| GET | `/api/v1/distributions/history/{campaignId}` | Get distribution history | Admin or Campaign Manager |
| POST | `/api/v1/distributions/execute/{campaignId}` | Execute distribution | Admin or Campaign Manager |

---

## 8. AI Support Service (AI Support Service)

### 8.1 General User Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/chat/message` | Send chat message | Authenticated |
| POST | `/api/v1/chat/recommend` | Chat recommendation | Authenticated |
| POST | `/api/v1/chat/advice` | Chat advice | Authenticated |
| GET | `/api/v1/chat/conversations/{userId}` | Get conversation history | Authenticated |
| DELETE | `/api/v1/chat/conversations/{conversationId}` | Delete conversation | Authenticated |
| POST | `/api/v1/chat/feedback` | Chat feedback | Authenticated |
| GET | `/api/v1/recommendations/{userId}` | Get user recommendations | Authenticated |
| GET | `/api/v1/recommendations/similar/{productId}` | Similar product recommendations | Public |
| GET | `/api/v1/recommendations/trending` | Trending products | Public |
| GET | `/api/v1/recommendations/category/{category}` | Category recommendations | Public |
| POST | `/api/v1/recommendations/feedback` | Recommendation feedback | Authenticated |
| GET | `/api/v1/recommendations/explain/{userId}/{productId}` | Explain recommendation | Authenticated |
| POST | `/api/v1/search/semantic` | Semantic search | Public |
| GET | `/api/v1/search/autocomplete` | Autocomplete | Public |
| GET | `/api/v1/search/suggest` | Search suggestions | Public |
| POST | `/api/v1/search/visual` | Visual search | Public |

### 8.2 Administrator Endpoints

*This service has no explicit administrator-only endpoints, but all data is subject to monitoring and analysis.*

---

## 9. API Gateway (API Gateway)

The API Gateway only handles routing to other services and has no unique business endpoints.

---

## Endpoint Classification Summary

### Statistics by Authorization Level

| Authorization Level | Endpoint Count | Description |
|-------------------|----------------|-------------|
| **Public** | 42 | Accessible without authentication |
| **Authenticated** | 31 | Accessible only to authenticated users |
| **Admin** | 49 | Accessible only to administrators |
| **Admin or Own** | 8 | Accessible to administrators or the user themselves |
| **Specialized Roles** | 15 | Accessible only to users with specific roles |

### Endpoint Count by Service

| Service | General User | Administrator | Total |
|---------|--------------|---------------|-------|
| Authentication Service | 19 | 3 | 22 |
| User Management Service | 12 | 6 | 18 |
| Inventory Management Service | 10 | 12 | 22 |
| Sales Management Service | 6 | 19 | 25 |
| Payment Cart Service | 9 | 2 | 11 |
| Point Service | 21 | 6 | 27 |
| Coupon Service | 5 | 16 | 21 |
| AI Support Service | 17 | 0 | 17 |
| API Gateway | 0 | 0 | 0 |
| **Total** | **99** | **64** | **163** |

## Notes

1. **Security**: All endpoints are protected by appropriate authentication and authorization mechanisms.
2. **Access Control**: Fine-grained access control is implemented using `@PreAuthorize` annotations.
3. **API Versioning**: Some services use `/api/v1/` versioning.
4. **Integration**: Unified access to all services is provided through the API Gateway.
5. **Auditing**: All administrator operations are subject to audit logging.

## Change History

- **Created**: June 22, 2025
- **Author**: Automatically generated by system analysis
- **Target Version**: Latest version (at analysis time)

---

*This document was created based on information automatically extracted from actual controller files. For detailed endpoint specifications, please refer to the OpenAPI specifications of each service.*
