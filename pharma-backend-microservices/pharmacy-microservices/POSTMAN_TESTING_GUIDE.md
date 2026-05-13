# Pharmacy Microservices — Postman Testing Guide

All requests go through the **API Gateway** at `http://localhost:8888`.
You never call individual service ports directly.

---

## Base URL

```
http://localhost:8888
```

---

## How Authentication Works

1. Register a user → get nothing back except a success message
2. Login → get a **JWT token** in the response
3. Copy that token
4. For every protected request, add a header:
   ```
   Authorization: Bearer <your_token>
   ```

---

## STEP 1 — Register an Admin User

**POST** `http://localhost:8888/api/auth/signup`

Headers:
```
Content-Type: application/json
```

Body (raw JSON):
```json
{
  "name": "Admin User",
  "email": "admin@pharmacy.com",
  "password": "admin123",
  "roles": ["ADMIN"]
}
```

Expected response `201 Created`:
```
User registered successfully
```

---

## STEP 2 — Register a Customer User

**POST** `http://localhost:8888/api/auth/signup`

Body:
```json
{
  "name": "John Customer",
  "email": "john@example.com",
  "password": "customer123",
  "roles": ["CUSTOMER"]
}
```

Expected response `201 Created`:
```
User registered successfully
```

---

## STEP 3 — Login as Admin (get token)

**POST** `http://localhost:8888/api/auth/login`

Headers:
```
Content-Type: application/json
```

Body:
```json
{
  "email": "admin@pharmacy.com",
  "password": "admin123"
}
```

Expected response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "Admin User",
  "roles": ["ADMIN"]
}
```

> **Save this token as `ADMIN_TOKEN`** — you'll use it for all admin requests.

---

## STEP 4 — Login as Customer (get token)

**POST** `http://localhost:8888/api/auth/login`

Body:
```json
{
  "email": "john@example.com",
  "password": "customer123"
}
```

> **Save this token as `CUSTOMER_TOKEN`**

---

## STEP 5 — Add Medicines (Admin only)

**POST** `http://localhost:8888/api/catalog/medicines`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body — Medicine 1 (no prescription needed):
```json
{
  "name": "Paracetamol 500mg",
  "description": "Pain reliever and fever reducer",
  "price": 25.00,
  "stockQuantity": 200,
  "requiresPrescription": false,
  "expiryDate": "2027-12-31",
  "categoryId": null
}
```

Expected response `201 Created`:
```json
{
  "id": 1,
  "name": "Paracetamol 500mg",
  "description": "Pain reliever and fever reducer",
  "price": 25.00,
  "stockQuantity": 200,
  "requiresPrescription": false,
  "expiryDate": "2027-12-31",
  "categoryName": null
}
```

Add a second medicine:

Body — Medicine 2 (prescription required):
```json
{
  "name": "Amoxicillin 250mg",
  "description": "Antibiotic for bacterial infections",
  "price": 85.50,
  "stockQuantity": 50,
  "requiresPrescription": true,
  "expiryDate": "2026-06-30",
  "categoryId": null
}
```

Add a third medicine:

```json
{
  "name": "Vitamin C 1000mg",
  "description": "Immune system booster",
  "price": 120.00,
  "stockQuantity": 300,
  "requiresPrescription": false,
  "expiryDate": "2028-01-01",
  "categoryId": null
}
```

---

## STEP 6 — Browse Medicines (Public — no token needed)

**GET** `http://localhost:8888/api/catalog/medicines`

No headers needed.

Expected response `200 OK`:
```json
[
  { "id": 1, "name": "Paracetamol 500mg", "price": 25.00, ... },
  { "id": 2, "name": "Amoxicillin 250mg", "price": 85.50, ... },
  { "id": 3, "name": "Vitamin C 1000mg",  "price": 120.00, ... }
]
```

---

## STEP 7 — Get Medicine by ID

**GET** `http://localhost:8888/api/catalog/medicines/1`

No token needed.

---

## STEP 8 — Search Medicines

**GET** `http://localhost:8888/api/catalog/medicines/search?name=para`

No token needed.

---

## STEP 9 — Update a Medicine (Admin only)

**PUT** `http://localhost:8888/api/catalog/medicines/1`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "name": "Paracetamol 500mg",
  "description": "Pain reliever and fever reducer — updated",
  "price": 30.00,
  "stockQuantity": 180,
  "requiresPrescription": false,
  "expiryDate": "2027-12-31",
  "categoryId": null
}
```

---

## STEP 10 — Upload a Prescription (Customer)

**POST** `http://localhost:8888/api/catalog/prescriptions/upload`

Headers:
```
Content-Type: application/json
Authorization: Bearer <CUSTOMER_TOKEN>
X-Auth-User: john@example.com
```

Body:
```json
{
  "customerId": "1",
  "imageUrl": "https://example.com/prescriptions/rx_john_001.jpg"
}
```

Expected response `200 OK`:
```json
{
  "id": 1,
  "customerId": 1,
  "customerEmail": "john@example.com",
  "imageUrl": "https://example.com/prescriptions/rx_john_001.jpg",
  "status": "PENDING",
  "rejectionReason": null,
  "uploadedAt": "2026-03-25T10:00:00"
}
```

---

## STEP 11 — View My Prescriptions (Customer)

**GET** `http://localhost:8888/api/catalog/prescriptions/my?customerId=1`

Headers:
```
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## STEP 12 — View Pending Prescriptions (Admin)

**GET** `http://localhost:8888/api/catalog/prescriptions/pending`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

---

## STEP 13 — Approve a Prescription (Admin)

**PUT** `http://localhost:8888/api/catalog/prescriptions/1/approve`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

No body needed.

Expected response:
```json
{
  "id": 1,
  "status": "APPROVED",
  ...
}
```

---

## STEP 14 — Reject a Prescription (Admin)

**PUT** `http://localhost:8888/api/catalog/prescriptions/1/reject`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "reason": "Prescription is expired or illegible"
}
```

---

## STEP 15 — Place an Order (Customer)

**POST** `http://localhost:8888/api/orders`

Headers:
```
Content-Type: application/json
Authorization: Bearer <CUSTOMER_TOKEN>
X-Auth-User: john@example.com
```

Body:
```json
{
  "customerId": 1,
  "deliveryAddress": "123 Main Street, Hyderabad, Telangana 500001",
  "items": [
    {
      "medicineId": 1,
      "quantity": 2
    },
    {
      "medicineId": 3,
      "quantity": 1
    }
  ]
}
```

Expected response `201 Created`:
```json
{
  "id": 1,
  "customerId": 1,
  "customerEmail": "john@example.com",
  "status": "PENDING",
  "totalAmount": 170.00,
  "deliveryAddress": "123 Main Street, Hyderabad, Telangana 500001",
  "createdAt": "2026-03-25T10:05:00",
  "items": [
    { "medicineId": 1, "medicineName": "Paracetamol 500mg", "quantity": 2, "unitPrice": 30.00 },
    { "medicineId": 3, "medicineName": "Vitamin C 1000mg",  "quantity": 1, "unitPrice": 120.00 }
  ]
}
```

> A notification is also published to RabbitMQ automatically.

---

## STEP 16 — Get Order by ID

**GET** `http://localhost:8888/api/orders/1`

Headers:
```
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## STEP 17 — Get All Orders for a Customer

**GET** `http://localhost:8888/api/orders/customer/1`

Headers:
```
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## STEP 18 — Process Payment

**POST** `http://localhost:8888/api/payments/process`

Headers:
```
Content-Type: application/json
Authorization: Bearer <CUSTOMER_TOKEN>
```

Body:
```json
{
  "orderId": 1,
  "customerId": 1,
  "amount": 170.00,
  "paymentMethod": "UPI"
}
```

Expected response `200 OK`:
```json
{
  "paymentId": 1,
  "orderId": 1,
  "amount": 170.00,
  "status": "SUCCESS",
  "transactionId": "TXN-A1B2C3D4",
  "createdAt": "2026-03-25T10:06:00"
}
```

---

## STEP 19 — Get Payment by Order ID

**GET** `http://localhost:8888/api/payments/order/1`

Headers:
```
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## STEP 20 — Admin: Get All Orders

**GET** `http://localhost:8888/api/orders`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

---

## STEP 21 — Admin: Update Order Status

**PUT** `http://localhost:8888/api/orders/1/status`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "status": "PAID"
}
```

Valid status values: `PENDING` → `PAID` → `PACKED` → `SHIPPED` → `DELIVERED` → `CANCELLED`

---

## STEP 22 — Admin Dashboard (via Admin Service)

**GET** `http://localhost:8888/api/admin/dashboard`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

Expected response:
```json
{
  "totalOrders": 1,
  "pendingPrescriptions": 0,
  "lowStockCount": 0,
  "monthlyRevenue": 170.00
}
```

---

## STEP 23 — Admin: Add Medicine via Admin Service

**POST** `http://localhost:8888/api/admin/medicines`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "name": "Ibuprofen 400mg",
  "description": "Anti-inflammatory pain reliever",
  "price": 45.00,
  "stockQuantity": 150,
  "requiresPrescription": false,
  "expiryDate": "2027-09-30",
  "categoryId": null
}
```

---

## STEP 24 — Admin: Update Medicine via Admin Service

**PUT** `http://localhost:8888/api/admin/medicines/1`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "name": "Paracetamol 500mg",
  "description": "Updated description",
  "price": 28.00,
  "stockQuantity": 250,
  "requiresPrescription": false,
  "expiryDate": "2027-12-31",
  "categoryId": null
}
```

---

## STEP 25 — Admin: Delete Medicine

**DELETE** `http://localhost:8888/api/admin/medicines/4`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

Expected response `204 No Content`

---

## STEP 26 — Admin: Update Order Status via Admin Service

**PUT** `http://localhost:8888/api/admin/orders/1/status`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "status": "SHIPPED"
}
```

---

## STEP 27 — Send a Notification (direct HTTP)

**POST** `http://localhost:8888/api/notifications/send`

Headers:
```
Content-Type: application/json
Authorization: Bearer <ADMIN_TOKEN>
```

Body:
```json
{
  "recipientEmail": "john@example.com",
  "subject": "Your order has been shipped",
  "message": "Order #1 is on its way. Expected delivery in 2-3 days.",
  "type": "ORDER_SHIPPED"
}
```

Expected response `200 OK`:
```
Notification processed for: john@example.com
```

---

## STEP 28 — Admin: Get Order Revenue

**GET** `http://localhost:8888/api/orders/revenue?from=2026-01-01&to=2026-12-31`

Headers:
```
Authorization: Bearer <ADMIN_TOKEN>
```

Expected response:
```json
{
  "revenue": 170.00
}
```

---

## Quick Reference — All Endpoints

| # | Method | URL | Auth | Role |
|---|--------|-----|------|------|
| 1 | POST | `/api/auth/signup` | None | — |
| 2 | POST | `/api/auth/login` | None | — |
| 3 | GET | `/api/catalog/medicines` | None | — |
| 4 | GET | `/api/catalog/medicines/{id}` | None | — |
| 5 | GET | `/api/catalog/medicines/search?name=` | None | — |
| 6 | POST | `/api/catalog/medicines` | Bearer | ADMIN |
| 7 | PUT | `/api/catalog/medicines/{id}` | Bearer | ADMIN |
| 8 | DELETE | `/api/catalog/medicines/{id}` | Bearer | ADMIN |
| 9 | GET | `/api/catalog/medicines/low-stock-count` | Bearer | ADMIN |
| 10 | POST | `/api/catalog/prescriptions/upload` | Bearer + X-Auth-User | CUSTOMER |
| 11 | GET | `/api/catalog/prescriptions/my?customerId=` | Bearer | CUSTOMER |
| 12 | GET | `/api/catalog/prescriptions/pending` | Bearer | ADMIN |
| 13 | PUT | `/api/catalog/prescriptions/{id}/approve` | Bearer | ADMIN |
| 14 | PUT | `/api/catalog/prescriptions/{id}/reject` | Bearer | ADMIN |
| 15 | POST | `/api/orders` | Bearer + X-Auth-User | CUSTOMER |
| 16 | GET | `/api/orders/{id}` | Bearer | ANY |
| 17 | GET | `/api/orders/customer/{customerId}` | Bearer | ANY |
| 18 | GET | `/api/orders` | Bearer | ADMIN |
| 19 | PUT | `/api/orders/{id}/status` | Bearer | ADMIN |
| 20 | GET | `/api/orders/count` | Bearer | ADMIN |
| 21 | GET | `/api/orders/revenue?from=&to=` | Bearer | ADMIN |
| 22 | POST | `/api/payments/process` | Bearer | ANY |
| 23 | GET | `/api/payments/order/{orderId}` | Bearer | ANY |
| 24 | GET | `/api/admin/dashboard` | Bearer | ADMIN |
| 25 | POST | `/api/admin/medicines` | Bearer | ADMIN |
| 26 | PUT | `/api/admin/medicines/{id}` | Bearer | ADMIN |
| 27 | DELETE | `/api/admin/medicines/{id}` | Bearer | ADMIN |
| 28 | GET | `/api/admin/prescriptions/pending` | Bearer | ADMIN |
| 29 | PUT | `/api/admin/prescriptions/{id}/approve` | Bearer | ADMIN |
| 30 | PUT | `/api/admin/prescriptions/{id}/reject` | Bearer | ADMIN |
| 31 | GET | `/api/admin/orders` | Bearer | ADMIN |
| 32 | PUT | `/api/admin/orders/{id}/status` | Bearer | ADMIN |
| 33 | POST | `/api/notifications/send` | Bearer | ANY |

---

## Postman Setup Tips

**Create a Collection variable for the token:**
1. After login, copy the token value
2. In Postman, go to your Collection → Variables
3. Add variable `ADMIN_TOKEN` and `CUSTOMER_TOKEN`
4. In each request Authorization tab → select `Bearer Token` → value: `{{ADMIN_TOKEN}}`

**Environment variables to set:**
```
BASE_URL = http://localhost:8888
ADMIN_TOKEN = (paste after login)
CUSTOMER_TOKEN = (paste after login)
```

---

## Service Health Checks

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| Gateway | http://localhost:8888 |

---

## Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `401 Unauthorized` | Missing or expired token | Login again and update token |
| `403 Forbidden` | Wrong role (e.g. CUSTOMER hitting ADMIN endpoint) | Use correct user token |
| `404 Not Found` | Wrong ID or path | Check the ID exists |
| `400 Bad Request` | Missing required fields | Check request body matches schema |
| `500 Internal Server Error` | Service down or DB not ready | Check docker-compose logs |
