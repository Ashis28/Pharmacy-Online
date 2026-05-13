================================================================================
  ORDER SERVICE - README
================================================================================

PORT: 9093
DATABASE: order_db (MySQL)
MESSAGE BROKER: RabbitMQ (port 5672)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Manages the full order lifecycle for the pharmacy platform. Customers place
orders referencing medicines from catalog-service. On order placement, medicine
prices are fetched in real-time from catalog-service via Feign, totals are
calculated, and an async notification is published to RabbitMQ for
notification-service to process. Admins can view all orders and update statuses.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
POST   /api/orders                        - Place a new order (auth required)
GET    /api/orders/{id}                   - Get order by ID (auth required)
GET    /api/orders/customer/{customerId}  - Get orders for a customer
GET    /api/orders                        - Get all orders (ADMIN only)
PUT    /api/orders/{id}/status            - Update order status (ADMIN only)
GET    /api/orders/count                  - Total order count (ADMIN only)
GET    /api/orders/revenue?from=&to=      - Revenue in date range (ADMIN only)

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------

TABLE: orders
  id                BIGINT (PK, auto-increment)
  customer_id       BIGINT NOT NULL  (logical FK to auth_db users.id)
  customer_email    VARCHAR
  status            VARCHAR (enum: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED)
  total_amount      DECIMAL
  delivery_address  VARCHAR
  created_at        DATETIME

TABLE: order_items
  id              BIGINT (PK, auto-increment)
  order_id        BIGINT (FK -> orders.id, CASCADE ALL)
  medicine_id     BIGINT  (logical FK to catalog_db medicines.id)
  medicine_name   VARCHAR  (denormalized snapshot at time of order)
  quantity        INT NOT NULL
  unit_price      DECIMAL NOT NULL  (snapshot at time of order)

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

Order
  id                Long
  customerId        Long          -- logical ref to auth_db users.id
  customerEmail     String        -- passed via X-Auth-User header from gateway
  status            OrderStatus   -- default PENDING
  totalAmount       BigDecimal    -- sum of (unitPrice * quantity) for all items
  deliveryAddress   String
  createdAt         LocalDateTime
  items             List<OrderItem>  -- @OneToMany CASCADE ALL, orphanRemoval

OrderStatus (enum)
  PENDING -> PAID -> PACKED -> SHIPPED -> DELIVERED
  PENDING -> CANCELLED  (at any point before DELIVERED)

OrderItem
  id            Long
  order         Order       -- @ManyToOne LAZY -> orders.id
  medicineId    Long        -- snapshot from catalog-service at order time
  medicineName  String      -- snapshot (catalog name may change later)
  quantity      Integer
  unitPrice     BigDecimal  -- snapshot (price may change later)

OrderRequest (input DTO)
  customerId      Long  @NotNull
  items           List<OrderItemRequest>  @NotEmpty
    medicineId    Long  @NotNull
    quantity      Integer  @NotNull
  deliveryAddress String

OrderResponse (output DTO)
  id, customerId, customerEmail, status, totalAmount,
  deliveryAddress, createdAt,
  items: List<ItemDto> { medicineId, medicineName, quantity, unitPrice }

MedicineDto (Feign response from catalog-service)
  id, name, price, stockQuantity, requiresPrescription

NotificationRequest (published to RabbitMQ)
  recipientEmail, subject, message, type="ORDER_PLACED"

--------------------------------------------------------------------------------
MESSAGING
--------------------------------------------------------------------------------
Exchange:    pharmacy.exchange  (direct)
Routing Key: notification.key
Queue:       notification.queue
Publisher:   NotificationPublisher (fire-and-forget, failures are swallowed)

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- JWT validated via JwtAuthFilter
- All endpoints require authentication
- ADMIN-only endpoints protected with @PreAuthorize("hasRole('ADMIN')")
- CSRF disabled (stateless JWT service)
- Customer email injected from X-Auth-User header (set by gateway after JWT validation)

--------------------------------------------------------------------------------
EXCEPTIONS (custom)
--------------------------------------------------------------------------------
OrderNotFoundException        (404 NOT_FOUND)
  - getById() with non-existent order ID
  - updateStatus() with non-existent order ID

InvalidOrderStatusException   (400 BAD_REQUEST)
  - updateStatus() called with a string not in OrderStatus enum
  - Valid values: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED

GlobalExceptionHandler also handles:
  FeignException.NotFound      (400) - medicine ID not found in catalog-service
  FeignException (generic)     (503) - catalog-service or payment-service down
  AccessDeniedException        (403) - non-ADMIN on admin endpoints
  MethodArgumentNotValidException (400) - null customerId, empty items list
  Exception (generic fallback) (500)

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
Feign clients (outbound):
  CatalogClient  -> catalog-service  GET /api/catalog/medicines/{id}
  PaymentClient  -> payment-service  (payment processing, optional)

Consumed by:
  admin-service  -> GET /api/orders, PUT /api/orders/{id}/status,
                    GET /api/orders/count, GET /api/orders/revenue

Publishes to:
  notification-service via RabbitMQ (async, non-blocking)

================================================================================
