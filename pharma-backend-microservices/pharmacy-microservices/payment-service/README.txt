================================================================================
  PAYMENT SERVICE - README
================================================================================

PORT: 9095
DATABASE: payment_db (MySQL)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Handles payment processing for orders. Receives payment requests from
order-service (or directly from the gateway), records the transaction, and
returns a payment confirmation with a unique transaction ID. Currently simulates
payment success (no real payment gateway integration) — designed to be extended
with Razorpay, Stripe, or similar.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
POST   /api/payments/process          - Process a payment for an order
GET    /api/payments/order/{orderId}  - Get payment record for an order

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------

TABLE: payments
  id                BIGINT (PK, auto-increment)
  order_id          BIGINT NOT NULL   (logical FK to order_db orders.id)
  customer_id       BIGINT NOT NULL   (logical FK to auth_db users.id)
  amount            DECIMAL NOT NULL
  payment_method    VARCHAR           (e.g. CARD, UPI, CASH)
  status            VARCHAR (enum: PENDING, SUCCESS, FAILED)
  transaction_id    VARCHAR           (format: TXN-XXXXXXXX, generated UUID prefix)
  created_at        DATETIME

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

Payment
  id              Long
  orderId         Long          -- logical ref to order_db orders.id
  customerId      Long          -- logical ref to auth_db users.id
  amount          BigDecimal
  paymentMethod   String
  status          PaymentStatus -- default PENDING, set to SUCCESS on process
  transactionId   String        -- "TXN-" + 8-char uppercase UUID fragment
  createdAt       LocalDateTime -- auto-set on creation

PaymentStatus (enum)
  PENDING   -- initial state
  SUCCESS   -- payment processed successfully
  FAILED    -- payment failed (for future gateway integration)

PaymentRequest (input DTO)
  orderId         Long
  customerId      Long
  amount          BigDecimal
  paymentMethod   String

PaymentResponse (output DTO)
  paymentId       Long    -- payment.id
  orderId         Long
  amount          BigDecimal
  status          String
  transactionId   String
  createdAt       LocalDateTime

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- JWT validated via JwtAuthFilter
- All endpoints require authentication
- CSRF disabled (stateless JWT service)

--------------------------------------------------------------------------------
EXCEPTIONS (custom)
--------------------------------------------------------------------------------
PaymentNotFoundException      (404 NOT_FOUND)
  - getByOrderId() when no payment record exists for the given orderId

PaymentProcessingException    (422 UNPROCESSABLE_ENTITY)
  - Reserved for future use when real payment gateway returns a failure

GlobalExceptionHandler also handles:
  AccessDeniedException        (403)
  IllegalArgumentException     (400) - e.g. null amount or orderId
  Exception (generic fallback) (500)

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
- Called by order-service via PaymentClient Feign (optional payment step)
- Gateway routes /api/payments/** to this service with JWT filter
- No outbound Feign calls from this service

================================================================================
