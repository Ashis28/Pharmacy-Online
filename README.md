================================================================================
  PHARMACY MICROSERVICES PLATFORM - PROJECT README
================================================================================

  Spring Boot 3.2.5 | Spring Cloud 2023.0.1 | Java 17 | MySQL | RabbitMQ

================================================================================
PROJECT OVERVIEW
================================================================================

A full-stack pharmacy management platform built as a microservices architecture.
Customers can browse medicines, upload prescriptions, and place orders. Admins
manage inventory, approve prescriptions, and monitor the dashboard. Payments are
processed and notifications are sent asynchronously via RabbitMQ.

--------------------------------------------------------------------------------
SERVICES SUMMARY
--------------------------------------------------------------------------------

Service               Port   Database       Purpose
--------------------  -----  -------------  ------------------------------------
eureka-server         8761   none           Service registry (Netflix Eureka)
gateway-service       8888   none           API gateway, JWT routing, load balancer
auth-service          9091   auth_db        User registration, login, JWT issuance
catalog-service       9092   catalog_db     Medicine catalog, categories, prescriptions
order-service         9093   order_db       Order placement and lifecycle management
admin-service         9094   admin_db       Admin dashboard, audit logging, orchestration
payment-service       9095   payment_db     Payment processing and transaction records
notification-service  9096   none           Async notifications via RabbitMQ

--------------------------------------------------------------------------------
ARCHITECTURE DIAGRAM (text)
--------------------------------------------------------------------------------

  Client
    |
    v
  [gateway-service :8888]  <-- validates JWT, routes, injects X-Auth-User
    |         |         |         |          |           |
    v         v         v         v          v           v
 auth-svc  catalog-svc order-svc admin-svc payment-svc notif-svc
  :9091     :9092       :9093     :9094      :9095       :9096
    |         |           |         |                      ^
    |         |           |         |                      |
    |         +<----------+         |                   RabbitMQ
    |         +<--------------------+                   (async)
    |         |
    v         v
  auth_db  catalog_db  order_db  admin_db  payment_db

  All services register with Eureka (:8761)

--------------------------------------------------------------------------------
INTER-SERVICE CALL MAP
--------------------------------------------------------------------------------

order-service
  -> catalog-service   (Feign) GET /api/catalog/medicines/{id}   [price lookup]
  -> payment-service   (Feign) POST /api/payments/process        [optional]
  -> notification-service (RabbitMQ) ORDER_PLACED event          [async]

admin-service
  -> catalog-service   (Feign) full medicine CRUD + prescriptions
  -> order-service     (Feign) order listing, status update, revenue

gateway-service
  -> all services      (HTTP routing via Eureka lb://)

--------------------------------------------------------------------------------
DATABASE SCHEMA OVERVIEW
--------------------------------------------------------------------------------

auth_db
  users           (id, name, email, password)
  user_roles      (user_id FK, role)

catalog_db
  categories      (id, name, description)
  medicines       (id, name, description, price, stock_quantity,
                   requires_prescription, expiry_date, category_id FK)
  prescriptions   (id, customer_id, customer_email, image_url,
                   status, rejection_reason, uploaded_at)

order_db
  orders          (id, customer_id, customer_email, status,
                   total_amount, delivery_address, created_at)
  order_items     (id, order_id FK, medicine_id, medicine_name,
                   quantity, unit_price)

admin_db
  audit_logs      (id, admin_email, action, target_entity,
                   target_id, details, timestamp)

payment_db
  payments        (id, order_id, customer_id, amount,
                   payment_method, status, transaction_id, created_at)

NOTE: Cross-database foreign keys (e.g. order.customer_id -> auth_db.users.id)
are LOGICAL only — no hard DB constraints across schemas. Referential integrity
is enforced at the application layer.

--------------------------------------------------------------------------------
SECURITY MODEL
--------------------------------------------------------------------------------

1. User logs in via POST /api/auth/login -> receives JWT (HS256, 24h expiry)
2. Client sends JWT in Authorization: Bearer <token> header
3. gateway-service validates JWT, extracts email, adds X-Auth-User header
4. Downstream services re-validate JWT via their own JwtAuthFilter
5. Role-based access enforced via @PreAuthorize("hasRole('ADMIN')")

Roles:
  CUSTOMER    - browse medicines, upload prescriptions, place orders
  ADMIN       - full access including dashboard, inventory, prescription approval
  PHARMACIST  - defined in enum, endpoints not yet differentiated

CSRF:
  Enabled ONLY in auth-service (the only service with form-like state-changing
  endpoints that a browser might call directly). All other services are pure
  REST APIs consumed by the gateway and are CSRF-exempt (stateless JWT).
  Clients fetch a CSRF token via GET /api/auth/csrf before calling protected
  auth endpoints.

================================================================================
CUSTOM EXCEPTIONS CREATED
================================================================================

auth-service:
  UserAlreadyExistsException    - signup with duplicate email
  UserNotFoundException         - login with unregistered email
  InvalidCredentialsException   - login with wrong password

catalog-service:
  MedicineNotFoundException     - medicine ID not found (get/update/delete)
  CategoryNotFoundException     - categoryId not found when creating/updating medicine
  PrescriptionNotFoundException - prescription ID not found (approve/reject)

order-service:
  OrderNotFoundException        - order ID not found (get/update)
  InvalidOrderStatusException   - invalid status string on status update

payment-service:
  PaymentNotFoundException      - no payment record for given orderId
  PaymentProcessingException    - reserved for real gateway failure responses

admin-service:
  AdminServiceException         - general admin business rule violations

All services have a GlobalExceptionHandler (@RestControllerAdvice) that returns:
  {
    "timestamp": "2026-03-25T10:00:00",
    "status": 409,
    "error": "Conflict",
    "message": "Email already registered: user@example.com"
  }

================================================================================
EDGE CASES HANDLED
================================================================================

Authentication:
  - Signup with email that already exists -> 409 Conflict
  - Login with email not in database -> 404 Not Found
  - Login with correct email but wrong password -> 401 Unauthorized
  - Signup with invalid email format -> 400 Validation Failed (fieldErrors)
  - Signup with blank name or password -> 400 Validation Failed
  - Signup with invalid role string (e.g. "SUPERUSER") -> 400 Bad Request
  - Signup with null roles -> defaults to CUSTOMER role automatically

Catalog:
  - Get/update/delete medicine with non-existent ID -> 404 Not Found
  - Create medicine with non-existent categoryId -> 404 Not Found
  - Create medicine with negative price or stock -> 400 Validation Failed
  - Create medicine with blank name -> 400 Validation Failed
  - Approve/reject prescription with non-existent ID -> 404 Not Found
  - Non-ADMIN user attempts write operations -> 403 Forbidden

Orders:
  - Get/update order with non-existent ID -> 404 Not Found
  - Update order with invalid status string -> 400 Bad Request
    (valid: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED)
  - Place order referencing non-existent medicine ID -> 400 (Feign 404 mapped)
  - catalog-service or payment-service down during order placement -> 503
  - Notification publish failure is silently swallowed (non-critical path)
  - Place order with null customerId or empty items list -> 400 Validation Failed

Payments:
  - Get payment for orderId with no payment record -> 404 Not Found
  - Null amount or orderId in payment request -> 400 Bad Request

Admin:
  - Downstream catalog-service returns 404 -> mapped to 404 Not Found
  - Downstream service returns 401 -> mapped to 401 Unauthorized
  - Any downstream service unavailable -> 503 Service Unavailable
  - Non-ADMIN user accesses any admin endpoint -> 403 Forbidden
  - Revenue calculation uses month-start to today range automatically

================================================================================
CONFLICTS AND DESIGN DECISIONS
================================================================================

1. CROSS-DATABASE FOREIGN KEYS
   Problem: customer_id in orders/prescriptions logically references auth_db
   users, but MySQL cannot enforce FK constraints across schemas.
   Decision: Logical-only references. Application layer trusts the gateway to
   pass a valid X-Auth-User header. No hard constraint enforcement.

2. PRICE/NAME SNAPSHOT IN ORDER ITEMS
   Problem: Medicine prices and names can change after an order is placed.
   Decision: order_items stores medicine_name and unit_price as snapshots at
   order time. Historical orders always reflect what the customer paid.

3. CSRF SCOPE
   Problem: Enabling CSRF on all services would break Feign inter-service calls
   (Feign doesn't send CSRF tokens).
   Decision: CSRF enabled only on auth-service (the only browser-facing auth
   endpoint). All other services are stateless REST APIs behind the gateway
   and are CSRF-exempt by design.

4. ADMIN SERVICE AS ORCHESTRATOR
   Problem: Admin could directly share catalog/order databases, but that creates
   tight coupling.
   Decision: admin-service has its own DB (audit_logs only) and delegates all
   data operations to catalog-service and order-service via Feign. This keeps
   each service's data ownership clean.

5. NOTIFICATION FAILURE ISOLATION
   Problem: If RabbitMQ is down, order placement should not fail.
   Decision: NotificationPublisher wraps publish in try-catch and logs warnings.
   Order is saved successfully regardless of notification outcome.

6. STATELESS JWT vs SESSIONS
   Problem: Spring Security defaults to session-based auth.
   Decision: All services use SessionCreationPolicy.STATELESS. JWT is the sole
   auth mechanism. No HttpSession is created anywhere.

7. ORDER STATUS ENUM MISMATCH (bug found during testing)
   Problem: Initial exception message listed "CONFIRMED" as a valid status, but
   the actual OrderStatus enum has PAID (not CONFIRMED).
   Resolution: Fixed exception message and test to use correct enum values:
   PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED.

8. EUREKA DEPENDENCY IN TESTS
   Problem: @SpringBootTest loads Eureka client which tries to connect to
   localhost:8761 during tests, causing failures in CI/offline environments.
   Decision: Test application.yml files set eureka.client.enabled=false and
   exclude EurekaClientAutoConfiguration. H2 in-memory DB used for JPA tests.

9. RABBITMQ DEPENDENCY IN ORDER-SERVICE TESTS
   Problem: Order-service tests would fail if RabbitMQ is not running.
   Decision: Test application.yml excludes RabbitAutoConfiguration. All
   RabbitMQ interactions are mocked with Mockito in unit tests.

================================================================================
RUNNING THE PROJECT
================================================================================

Prerequisites:
  - Java 17+
  - Maven 3.8+
  - MySQL 8+ running on port 3306 (password: Janu@0307)
  - RabbitMQ running on port 5672 (guest/guest)

Start order:
  1. eureka-server      (wait for it to be ready)
  2. gateway-service
  3. auth-service
  4. catalog-service
  5. order-service
  6. admin-service
  7. payment-service
  8. notification-service

Build all:
  mvn clean install -DskipTests

Run tests for all modules:
  mvn test

Run tests for a single service:
  mvn test -pl auth-service

================================================================================
TEST COVERAGE SUMMARY
================================================================================

Service               Test Class                          Tests
--------------------  ----------------------------------  ------
auth-service          AuthServiceTest                     7
auth-service          AuthControllerTest                  6
catalog-service       MedicineServiceTest                 11
catalog-service       PrescriptionServiceTest             7
order-service         OrderServiceTest                    9
payment-service       PaymentServiceTest                  4
admin-service         AdminServiceTest                    7
notification-service  NotificationListenerTest            2
                                                   TOTAL: 53

All tests use JUnit 5 + Mockito. No real DB or external service required.

================================================================================
