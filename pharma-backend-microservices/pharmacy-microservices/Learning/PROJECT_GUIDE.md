# PharmaOnline — Complete Project Guide

> A beginner-friendly walkthrough of the entire PharmaOnline microservices project.
> Read this top to bottom to understand every part of the system.

---

## Table of Contents

1. [What Is This Project?](#1-what-is-this-project)
2. [Big Picture Architecture](#2-big-picture-architecture)
3. [How a Request Flows Through the System](#3-how-a-request-flows-through-the-system)
4. [Backend Services — Detailed](#4-backend-services--detailed)
5. [Frontend — Detailed](#5-frontend--detailed)
6. [Database Design](#6-database-design)
7. [Security — How JWT Works](#7-security--how-jwt-works)
8. [Messaging — How RabbitMQ Works](#8-messaging--how-rabbitmq-works)
9. [Key User Flows (Step by Step)](#9-key-user-flows-step-by-step)
10. [Running the Project](#10-running-the-project)
11. [Testing](#11-testing)
12. [Common Questions](#12-common-questions)

---

## 1. What Is This Project?

PharmaOnline is an **online pharmacy platform** built as a microservices application.

**What customers can do:**
- Browse and search medicines
- Add medicines to a shopping cart
- Place orders with a delivery address
- Pay via UPI, Card (Razorpay), Net Banking, or Cash on Delivery
- Upload prescription images for Rx medicines
- View order history and track delivery status
- Receive notifications about their orders

**What admins can do:**
- Add, edit, and delete medicines from the catalog
- Review and approve/reject customer prescriptions
- Update order statuses (Packed → Shipped → Delivered)
- View dashboard stats (total orders, revenue, low stock)
- Send manual notifications to customers

---

## 2. Big Picture Architecture

```
Browser (Angular)
      │
      │  HTTP requests (port 4200 → 8888)
      ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (port 8888)                   │
│  • Single entry point for ALL API calls                     │
│  • Validates JWT tokens on every request                    │
│  • Routes requests to the correct microservice              │
│  • Adds X-Auth-User header to downstream requests           │
└──────┬──────────┬──────────┬──────────┬──────────┬──────────┘
       │          │          │          │          │
       ▼          ▼          ▼          ▼          ▼
  Auth-Svc   Catalog-Svc  Order-Svc  Payment-Svc  Admin-Svc
  (9091)      (9092)       (9093)     (9095)       (9094)
       │          │          │                        │
       │          │          │  RabbitMQ              │
       │          │          └──────────────────────► Notification-Svc
       │          │                                   (9096)
       │          │
       └──────────┴──── All register with Eureka (8761)
                        All get config from Config Server (8085)
```

**Key principle:** The frontend NEVER talks directly to individual services.
Everything goes through the Gateway at port 8888.

---

## 3. How a Request Flows Through the System

### Example: Customer places an order

```
1. Customer clicks "Place Order" in the browser
   │
2. Angular CartComponent calls orderService.placeOrder()
   │
3. HTTP POST http://localhost:8888/api/orders
   (with Authorization: Bearer <jwt-token> header, added by authInterceptor)
   │
4. API Gateway receives the request
   ├── Validates the JWT token (checks signature + expiry)
   ├── Extracts email from token → adds X-Auth-User header
   └── Routes to order-service (port 9093) via Eureka load balancer
   │
5. OrderController.placeOrder() receives the request
   │
6. OrderService.placeOrder() runs:
   ├── For each item: calls CatalogClient.getMedicineById() → GET catalog-service
   ├── Validates stock is sufficient
   ├── Saves the order to order_db (MySQL)
   └── Publishes ORDER_PLACED event to RabbitMQ
   │
7. After transaction commits:
   └── OrderController calls decrementStockForOrder()
       └── CatalogClient.decrementStock() → PATCH catalog-service
           └── catalog-service runs atomic UPDATE on medicines table
   │
8. RabbitMQ delivers ORDER_PLACED event to notification-service
   └── NotificationListener.handleNotification() → EmailService.sendEmail()
   │
9. Response flows back: order-service → gateway → browser
   │
10. Angular navigates to /payment/:orderId
```

---

## 4. Backend Services — Detailed

### 4.1 Eureka Server (port 8761)

**What it is:** A service registry. Every microservice registers itself here on startup.

**Why it exists:** Without Eureka, the gateway would need hardcoded IP addresses for each service.
With Eureka, services find each other by name (e.g. "catalog-service") and Eureka provides the actual address.

**How to check it:** Open http://localhost:8761 — you'll see all registered services.

---

### 4.2 Config Server (port 8085)

**What it is:** A centralized configuration server. Stores config for all services.

**Why it exists:** Without it, each service has its own application.yml with database credentials, JWT secrets, etc.
If you need to change the JWT secret, you'd have to update 7 files. With Config Server, you update one file.

**How it works:**
- Config files live in `config-server/src/main/resources/config/`
- Each service has its own file: `auth-service.yml`, `catalog-service.yml`, etc.
- On startup, each service calls `http://localhost:8085/auth-service/default` to get its config
- The `optional:` prefix means services still start even if Config Server is down

**What it serves:**
```
auth-service     → datasource URL, JWT secret, admin token
catalog-service  → datasource URL, JWT secret
order-service    → datasource URL, JWT secret, RabbitMQ config
payment-service  → datasource URL, JWT secret
admin-service    → datasource URL, JWT secret
notification-svc → RabbitMQ config
gateway-service  → JWT secret
```

---

### 4.3 API Gateway (port 8888)

**What it is:** The single entry point for all API calls from the browser.

**Why it exists:**
- Security: validates JWT tokens in one place instead of every service
- Routing: maps URL paths to the correct microservice
- Simplicity: frontend only needs to know one URL (port 8888)

**How JWT validation works in the gateway:**
```java
// JwtAuthFilter.java
1. Check if path is public (login, signup, GET medicines) → skip validation
2. Read Authorization header → extract "Bearer <token>"
3. Validate token signature using the JWT secret
4. Extract email (subject) and roles from token
5. Add X-Auth-User and X-Auth-Roles headers to the forwarded request
6. If token is missing/invalid → return 401 Unauthorized
```

**Route mapping:**
```
/api/auth/**         → auth-service (9091)
/api/catalog/**      → catalog-service (9092)
/api/orders/**       → order-service (9093)
/api/admin/**        → admin-service (9094)
/api/payments/**     → payment-service (9095)
/api/notifications/** → notification-service (9096)
```

---

### 4.4 Auth Service (port 9091)

**What it does:** User registration and login.

**Key endpoints:**
```
POST /api/auth/signup  → Register new user (CUSTOMER or ADMIN)
POST /api/auth/login   → Login, returns JWT token
```

**How signup works:**
```java
1. Check if email already exists → throw UserAlreadyExistsException if yes
2. Parse roles (default to CUSTOMER if none provided)
3. If ADMIN role requested → validate adminToken against configured secret
4. Hash the password with BCrypt (never store plain text!)
5. Save user to auth_db
6. Return "User registered successfully"
```

**How login works:**
```java
1. Find user by email → throw UserNotFoundException if not found
2. Compare submitted password with BCrypt hash → throw InvalidCredentialsException if wrong
3. Generate JWT token with email as subject and roles as claim
4. Return token + user details
```

**What is BCrypt?**
BCrypt is a password hashing algorithm. It's one-way — you can't reverse it.
When a user logs in, BCrypt hashes their submitted password and compares it to the stored hash.
Even if the database is stolen, attackers can't recover the original passwords.

**What is JWT?**
JSON Web Token — a signed string that proves who you are.
Format: `header.payload.signature`
The payload contains: email, roles, issued-at, expiry.
The signature is created with a secret key — only the server can create valid tokens.
The client sends the token on every request; the server verifies the signature.

---

### 4.5 Catalog Service (port 9092)

**What it does:** Manages the medicine catalog and prescriptions.

**Key endpoints:**
```
GET    /api/catalog/medicines              → All medicines (public)
GET    /api/catalog/medicines/{id}         → Single medicine (public)
GET    /api/catalog/medicines/search?name= → Search by name (public)
POST   /api/catalog/medicines              → Add medicine (ADMIN only)
PUT    /api/catalog/medicines/{id}         → Update medicine (ADMIN only)
DELETE /api/catalog/medicines/{id}         → Delete medicine (ADMIN only)
PATCH  /api/catalog/medicines/{id}/stock   → Decrement stock (internal, called by order-service)
GET    /api/catalog/medicines/low-stock-count → Count of low-stock medicines

POST   /api/catalog/prescriptions/upload  → Upload prescription (customer)
GET    /api/catalog/prescriptions/my      → Customer's prescriptions
GET    /api/catalog/prescriptions/pending → All pending (ADMIN only)
PUT    /api/catalog/prescriptions/{id}/approve → Approve (ADMIN only)
PUT    /api/catalog/prescriptions/{id}/reject  → Reject (ADMIN only)
```

**How stock decrement works (atomic):**
```sql
-- Instead of: read → subtract → save (race condition risk)
-- We use a single atomic UPDATE:
UPDATE medicines
SET stock_quantity = stock_quantity - :quantity
WHERE id = :id AND stock_quantity >= :quantity
-- Returns 0 rows if stock is insufficient → triggers error
```

---

### 4.6 Order Service (port 9093)

**What it does:** Manages the order lifecycle from placement to delivery.

**Order status flow:**
```
PENDING → PAID → PACKED → SHIPPED → DELIVERED
                                  ↘ CANCELLED (any time, by admin)
```

**How placeOrder works:**
```java
1. For each item in the request:
   a. Call CatalogClient.getMedicineById() → get name and price
   b. Check stock is sufficient → throw if not
   c. Create OrderItem with snapshotted name and price
2. Calculate total amount
3. Save order to order_db (status = PENDING)
4. Publish ORDER_PLACED event to RabbitMQ
5. Return the saved order

After transaction commits (in the controller):
6. Call CatalogClient.decrementStock() for each item
   (done AFTER commit to avoid holding DB transaction open during HTTP calls)
```

**Why snapshot the price?**
When an order is placed, we copy the medicine's current price into the order item.
This way, if the admin changes the price later, old orders still show the correct price.

**Feign Client:**
```java
@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @GetMapping("/api/catalog/medicines/{id}")
    MedicineDto getMedicineById(@PathVariable Long id);

    @PatchMapping("/api/catalog/medicines/{id}/stock")
    void decrementStock(@PathVariable Long id, @RequestParam int quantity);
}
```
Feign is a declarative HTTP client. You define an interface and Spring generates the implementation.
It uses Eureka to find the catalog-service's actual address automatically.

---

### 4.7 Payment Service (port 9095)

**What it does:** Processes payments and stores payment records.

**How it works (simulated):**
```java
1. Receive payment request (orderId, customerId, amount, paymentMethod)
2. Generate a unique transaction ID: "TXN-" + random 8 chars
3. Save payment record with status = SUCCESS
4. Return payment details
```

In production, step 2 would call Razorpay/Stripe API to actually charge the card.

**Payment methods supported:** UPI, CARD, NET_BANKING, CASH

---

### 4.8 Admin Service (port 9094)

**What it does:** Aggregates data from other services for admin operations.

**How the dashboard works:**
```java
// AdminService calls other services via Feign:
1. orderClient.getTotalOrderCount()     → order-service
2. catalogClient.getPendingPrescriptions() → catalog-service
3. catalogClient.getLowStockCount()     → catalog-service
4. orderClient.getRevenue(from, to)     → order-service
// Combines all results into a DashboardResponse
```

**Audit logging:**
Every admin action (add medicine, update order, approve prescription) is logged
to an audit_log table with: action, entity, entityId, adminEmail, timestamp.

---

### 4.9 Notification Service (port 9096)

**What it does:** Receives notification events and sends emails.

**Two ways to trigger notifications:**
1. **RabbitMQ** (async): order-service publishes ORDER_PLACED event → notification-service consumes it
2. **HTTP** (direct): POST /api/notifications/send → admin sends manual notification

**Email configuration:**
```yaml
# Set these environment variables to enable real email:
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password
```
When MAIL_ENABLED=false (default), emails are just logged — no actual sending.

---

## 5. Frontend — Detailed

### 5.1 Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Angular | 21 | UI framework |
| TypeScript | 5.9 | Type-safe JavaScript |
| Angular Signals | built-in | Reactive state management |
| Angular Router | built-in | Client-side navigation |
| Angular HttpClient | built-in | HTTP API calls |
| SCSS | — | Styling |

### 5.2 Project Structure

```
pharma-frontend/src/app/
├── app.ts              ← Root component (navbar + router-outlet + footer)
├── app.routes.ts       ← All page routes with guards
├── app.config.ts       ← App bootstrap configuration
│
├── services/           ← All HTTP calls and state management
│   ├── auth.service.ts     ← Login, logout, JWT storage, user state
│   ├── cart.service.ts     ← Shopping cart (in-memory, signals)
│   ├── medicine.service.ts ← Catalog API calls
│   ├── order.service.ts    ← Order API calls
│   ├── payment.service.ts  ← Payment API calls
│   └── admin.service.ts    ← Admin API calls
│
├── guards/             ← Route protection
│   └── auth.guard.ts       ← authGuard, adminGuard, guestGuard
│
├── interceptors/       ← HTTP middleware
│   └── auth.interceptor.ts ← Auto-attach JWT to every request
│
├── models/             ← TypeScript interfaces (data shapes)
│   ├── auth.models.ts
│   ├── medicine.models.ts
│   ├── order.models.ts
│   └── payment.models.ts
│
└── components/         ← UI components (one folder per page)
    ├── navbar/         ← Top navigation bar
    ├── footer/         ← Site footer
    ├── home/           ← Landing page
    ├── auth/
    │   ├── login/      ← Login page
    │   └── register/   ← Registration page (customer + admin)
    ├── medicines/      ← Medicine catalog page
    ├── cart/           ← Shopping cart page
    ├── orders/         ← Customer order history
    ├── payment/        ← Payment page (UPI QR, Razorpay modal, COD)
    ├── prescriptions/  ← Customer prescription management
    ├── notifications/  ← Customer notifications
    └── admin/          ← All admin pages
        ├── admin-layout/       ← Sidebar shell for admin pages
        ├── dashboard/          ← Admin dashboard with stats
        ├── admin-medicines/    ← Medicine CRUD management
        ├── admin-orders/       ← Order status management
        ├── admin-prescriptions/← Prescription review
        └── admin-notifications/← Manual notification sender
```

### 5.3 How Angular Signals Work

Signals are Angular's reactive state system (introduced in Angular 17+).

```typescript
// Creating a signal
private items = signal<CartItem[]>([]);

// Reading a signal (in component or template)
const currentItems = items();  // call it like a function

// Updating a signal
items.set([...currentItems, newItem]);

// Computed signal — auto-recalculates when dependencies change
cartCount = computed(() => items().reduce((sum, i) => sum + i.quantity, 0));
```

When a signal changes, Angular automatically re-renders only the parts of the UI
that read that signal. No manual change detection needed.

### 5.4 How Lazy Loading Works

```typescript
// In app.routes.ts:
{
  path: 'medicines',
  loadComponent: () => import('./components/medicines/medicines-list/medicines-list')
                        .then(m => m.MedicinesListComponent)
}
```

The `import()` is a dynamic import — the browser only downloads the component's
JavaScript bundle when the user actually navigates to /medicines.
This makes the initial app load much faster.

### 5.5 How the Auth Interceptor Works

```
Every HTTP request:
  1. authInterceptor runs first
  2. Gets JWT token from localStorage
  3. If token exists: adds "Authorization: Bearer <token>" header
  4. Sends the modified request
  5. If response is 401: logs user out and redirects to /login
```

This means every service method (getAllMedicines, placeOrder, etc.) automatically
sends the JWT without any extra code.

---

## 6. Database Design

Each service has its own MySQL database (microservices principle: database per service).

### auth_db
```
users
  id          BIGINT PK AUTO_INCREMENT
  name        VARCHAR
  email       VARCHAR UNIQUE
  password    VARCHAR (BCrypt hash)
  roles       (stored as separate table or JSON)
```

### catalog_db
```
medicines
  id                    BIGINT PK
  name                  VARCHAR
  description           TEXT
  price                 DECIMAL(10,2)
  stock_quantity        INT
  requires_prescription BOOLEAN
  expiry_date           DATE
  category_id           BIGINT FK → categories

categories
  id    BIGINT PK
  name  VARCHAR UNIQUE

prescriptions
  id               BIGINT PK
  customer_id      BIGINT
  customer_email   VARCHAR
  image_url        VARCHAR
  status           ENUM(PENDING, APPROVED, REJECTED)
  rejection_reason VARCHAR
  uploaded_at      DATETIME
```

### order_db
```
orders
  id               BIGINT PK
  customer_id      BIGINT
  customer_email   VARCHAR
  status           ENUM(PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED)
  total_amount     DECIMAL(10,2)
  delivery_address VARCHAR
  created_at       DATETIME

order_items
  id            BIGINT PK
  order_id      BIGINT FK → orders
  medicine_id   BIGINT
  medicine_name VARCHAR (snapshotted at order time)
  quantity      INT
  unit_price    DECIMAL(10,2) (snapshotted at order time)
```

### payment_db
```
payments
  id             BIGINT PK
  order_id       BIGINT
  customer_id    BIGINT
  amount         DECIMAL(10,2)
  payment_method VARCHAR
  status         ENUM(SUCCESS, FAILED, PENDING)
  transaction_id VARCHAR (TXN-XXXXXXXX)
  created_at     DATETIME
```

---

## 7. Security — How JWT Works

### Token Generation (auth-service)
```
1. User logs in with email + password
2. Backend verifies credentials
3. Backend creates JWT:
   Header:  { "alg": "HS256", "typ": "JWT" }
   Payload: { "sub": "user@email.com", "roles": ["CUSTOMER"], "iat": 1234, "exp": 5678 }
   Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
4. Returns: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9.SIGNATURE"
```

### Token Usage (every request)
```
Browser → Gateway: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Gateway validates: signature correct? not expired?
If valid: extracts email + roles, adds X-Auth-User header, forwards request
If invalid: returns 401 Unauthorized
```

### Role-Based Access
```
@PreAuthorize("hasRole('ADMIN')")  ← only ADMIN role can call this endpoint
@PreAuthorize("hasRole('CUSTOMER')")  ← only CUSTOMER role
// No annotation = any authenticated user
```

### Admin Registration Security
```
To create an admin account, you need the admin token: PHARMA-ADMIN-2026
This is validated on the backend — the frontend just sends it.
Wrong token → 400 Bad Request with "Invalid token." message
The token can be changed via ADMIN_TOKEN environment variable.
```

---

## 8. Messaging — How RabbitMQ Works

RabbitMQ is a message broker — it lets services communicate asynchronously.

### Why async messaging?
When an order is placed, we want to send a notification email.
But we don't want the order placement to fail if the email service is down.
By publishing to RabbitMQ, the order is saved successfully regardless of
whether the notification service is running.

### How it works in this project:
```
order-service                    RabbitMQ                notification-service
     │                              │                           │
     │  publish(ORDER_PLACED)       │                           │
     │ ─────────────────────────►  │                           │
     │                              │  deliver(ORDER_PLACED)    │
     │                              │ ────────────────────────► │
     │                              │                           │ handleNotification()
     │                              │                           │ → sendEmail()
```

### Configuration:
```
Exchange: pharmacy.exchange (TopicExchange)
Queue:    notification.queue (durable — survives broker restart)
Routing:  notification.send
```

### What happens if RabbitMQ is down?
The order is still placed successfully. The notification is just not sent.
The `try/catch` around the publish call ensures it's non-critical.

---

## 9. Key User Flows (Step by Step)

### Flow 1: Customer Registration
```
1. Visit /register
2. Fill in name, email, password, confirm password
3. Password strength meter shows in real-time (Weak/Fair/Strong/Very Strong)
4. Click "Create Account"
5. Frontend validates: fields not empty, passwords match, length ≥ 6
6. POST /api/auth/signup → auth-service
7. Backend: check email unique, hash password, save user
8. Success → redirect to /login after 2 seconds
```

### Flow 2: Customer Login
```
1. Visit /login
2. Enter email + password
3. Click "Sign In"
4. POST /api/auth/login → auth-service
5. Backend: verify password, generate JWT
6. Frontend: store JWT in localStorage, update currentUser signal
7. Navbar immediately shows logged-in links
8. Admin → /admin/dashboard, Customer → /home
```

### Flow 3: Browse and Buy Medicine
```
1. Visit /medicines
2. Browse the catalog (no login needed)
3. Click "Add to Cart" on a medicine
   - If not logged in → redirect to /login
   - If logged in → item added to cart, badge count increases
4. Visit /cart
5. Adjust quantities (capped at stock limit)
6. Enter delivery address
7. Click "Place Order"
8. POST /api/orders → order-service
   - Validates stock, saves order, decrements stock, publishes notification
9. Redirect to /payment/:orderId
10. Select payment method (UPI/Card/Net Banking/COD)
11. For UPI: scan QR code, then click Pay
    For Card: fill Razorpay modal, click Pay
    For COD: click "Confirm Order" → immediate confirmation
12. Processing animation (5 seconds) → Success screen
13. Order status updated to PAID
```

### Flow 4: Admin Reviews Prescription
```
1. Customer uploads prescription image URL at /prescriptions
2. Prescription saved with PENDING status
3. Admin visits /admin/prescriptions
4. Sees the prescription image and customer details
5. Clicks ✅ Approve or ❌ Reject
6. If reject: types a reason in the modal
7. Status updated to APPROVED or REJECTED
8. Customer sees updated status on their prescriptions page
```

---

## 10. Running the Project

### Prerequisites
- Java 17+
- Node.js 18+ and Angular CLI
- MySQL on port 3306 (password: yosker)
- RabbitMQ on port 5672

### Start Order (important — must follow this order)
```
1. Start Eureka Server first (wait for it to be ready)
2. Start Config Server
3. Start all microservices (can be parallel):
   - auth-service (9091)
   - catalog-service (9092)
   - order-service (9093)
   - admin-service (9094)
   - payment-service (9095)
   - notification-service (9096)
4. Wait ~40 seconds for all services to register with Eureka
5. Start Gateway (8888) — needs services to be registered first
6. Start Frontend (4200)
```

### One-Click Start
```powershell
# In project root:
powershell -ExecutionPolicy Bypass -File start-all-services.ps1

# In a separate terminal:
cd pharma-frontend
npm start
```

### Test Accounts
| Role | Email | Password |
|---|---|---|
| Admin | admin@pharmacy.com | admin123 |
| Customer | john@example.com | customer123 |

### Service URLs
| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8888 |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8085 |
| Swagger UI | http://localhost:8888/swagger-ui.html |
| RabbitMQ UI | http://localhost:15672 (guest/guest) |

---

## 11. Testing

### Backend Unit Tests (JUnit + Mockito)
```bash
# Run all backend tests
mvn test -pl auth-service,catalog-service,order-service,payment-service,notification-service

# Run a specific service
mvn test -pl catalog-service
```

**What's tested:**
- auth-service: signup validation, login, JWT generation
- catalog-service: medicine CRUD, stock decrement, prescription management
- order-service: order placement, stock validation, status updates
- payment-service: payment processing, all payment methods
- notification-service: email service, listener, disabled mode

### Frontend Unit Tests (Jest)
```bash
cd pharma-frontend
npm run test:jest           # run all tests
npm run test:jest:coverage  # run with coverage report
```

**What's tested:**
- CartService: add/remove/update/clear, stock limits, computed signals
- AuthService: login/logout, token storage, role checks
- Route Guards: authGuard, adminGuard, guestGuard
- RegisterComponent: password strength, form validation, error handling
- All service HTTP methods: correct URLs, headers, payloads

### Code Coverage (JaCoCo)
```bash
mvn verify -pl catalog-service
# Opens: catalog-service/target/site/jacoco/index.html
# Current coverage: 99% instructions, 100% branches
```

---

## 12. Common Questions

**Q: Why are there 9 separate services instead of one big app?**
A: Microservices allow each service to be deployed, scaled, and updated independently.
If the payment service goes down, customers can still browse medicines.
Each service has its own database, so a catalog DB failure doesn't affect orders.

**Q: Why does the frontend use localStorage for the JWT?**
A: localStorage persists across page refreshes. If we used memory (a variable),
the user would be logged out every time they refresh the page.
The trade-off is that localStorage is accessible to JavaScript, so XSS attacks
could steal the token. In production, HttpOnly cookies would be more secure.

**Q: Why does the cart not persist after page refresh?**
A: The cart is stored in an Angular signal (in-memory). This is intentional —
the cart is temporary. If you want persistence, you'd store it in localStorage
or in a backend database. For this demo, in-memory is simpler.

**Q: What happens if a service is down?**
A: The gateway returns 503 Service Unavailable. The frontend shows an error message.
Services that are non-critical (like notifications) use try/catch so their failure
doesn't affect the main flow.

**Q: How do I add a new admin?**
A: Go to /register-admin and enter the admin token: PHARMA-ADMIN-2026
To change the token, set the ADMIN_TOKEN environment variable when starting auth-service.

**Q: Why is Zipkin mentioned but not required?**
A: Zipkin is for distributed tracing — it shows how a request flows through all services.
It's optional. Services work fine without it. To use it:
`docker run -d -p 9411:9411 openzipkin/zipkin`
Then open http://localhost:9411

**Q: What is the difference between @Transactional and non-transactional?**
A: @Transactional wraps a method in a database transaction. If anything fails,
all database changes are rolled back. The stock decrement is done OUTSIDE the
order transaction because Feign HTTP calls shouldn't be inside a DB transaction
(it holds the connection open during the HTTP call, which is slow and risky).

