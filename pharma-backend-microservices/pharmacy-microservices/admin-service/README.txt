================================================================================
  ADMIN SERVICE - README
================================================================================

PORT: 9094
DATABASE: admin_db (MySQL)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Provides a unified admin dashboard and management interface. Rather than
owning its own medicine or order data, it acts as an orchestrator — delegating
all operations to catalog-service and order-service via Feign clients, then
recording every admin action in a local audit log. All endpoints require the
ADMIN role.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
GET    /api/admin/dashboard                    - Aggregated stats dashboard
POST   /api/admin/medicines                    - Add a new medicine
PUT    /api/admin/medicines/{id}               - Update a medicine
DELETE /api/admin/medicines/{id}               - Delete a medicine
GET    /api/admin/prescriptions/pending        - List pending prescriptions
PUT    /api/admin/prescriptions/{id}/approve   - Approve a prescription
PUT    /api/admin/prescriptions/{id}/reject    - Reject a prescription (with reason)
GET    /api/admin/orders                       - List all orders
PUT    /api/admin/orders/{id}/status           - Update order status

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------

TABLE: audit_logs
  id              BIGINT (PK, auto-increment)
  admin_email     VARCHAR   -- email from JWT SecurityContext
  action          VARCHAR   -- e.g. ADD_MEDICINE, APPROVE_PRESCRIPTION
  target_entity   VARCHAR   -- e.g. Medicine, Order, Prescription
  target_id       VARCHAR   -- string ID of the affected record
  details         VARCHAR   -- human-readable description
  timestamp       DATETIME  -- auto-set to LocalDateTime.now()

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

AuditLog
  id            Long
  adminEmail    String    -- pulled from SecurityContextHolder at action time
  action        String    -- one of: ADD_MEDICINE, UPDATE_MEDICINE, DELETE_MEDICINE,
                             APPROVE_PRESCRIPTION, REJECT_PRESCRIPTION,
                             UPDATE_ORDER_STATUS
  targetEntity  String    -- "Medicine", "Prescription", "Order"
  targetId      String    -- string form of the entity's ID
  details       String    -- e.g. "Added: Amoxicillin", "Rejected: Illegible"
  timestamp     LocalDateTime

DashboardResponse (aggregated from downstream services)
  totalOrders           long        -- from order-service /api/orders/count
  pendingPrescriptions  long        -- count from catalog-service /prescriptions/pending
  lowStockCount         long        -- from catalog-service /medicines/low-stock-count
  monthlyRevenue        BigDecimal  -- from order-service /api/orders/revenue (month-to-date)

MedicineRequest / MedicineResponse  -- mirrors catalog-service DTOs (passed through)
PrescriptionResponse                -- mirrors catalog-service DTO (passed through)
OrderResponse                       -- mirrors order-service DTO (passed through)

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- JWT validated via JwtAuthFilter
- Entire controller annotated @PreAuthorize("hasRole('ADMIN')")
- CSRF disabled (stateless JWT service)
- Admin email extracted from SecurityContextHolder for audit logging

--------------------------------------------------------------------------------
EXCEPTIONS (custom)
--------------------------------------------------------------------------------
AdminServiceException         (400 BAD_REQUEST)
  - General admin-layer business rule violations

GlobalExceptionHandler also handles:
  FeignException.NotFound      (404) - resource not found in downstream service
  FeignException.Unauthorized  (401) - downstream service rejected the token
  FeignException (generic)     (503) - catalog-service or order-service unavailable
  AccessDeniedException        (403) - non-ADMIN user attempts access
  MethodArgumentNotValidException (400) - invalid medicine fields
  Exception (generic fallback) (500)

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
Feign clients (outbound):
  CatalogClient -> catalog-service
    POST   /api/catalog/medicines
    PUT    /api/catalog/medicines/{id}
    DELETE /api/catalog/medicines/{id}
    GET    /api/catalog/prescriptions/pending
    PUT    /api/catalog/prescriptions/{id}/approve
    PUT    /api/catalog/prescriptions/{id}/reject
    GET    /api/catalog/medicines/low-stock-count

  OrderClient -> order-service
    GET    /api/orders
    PUT    /api/orders/{id}/status
    GET    /api/orders/count
    GET    /api/orders/revenue

No other services call admin-service directly.

================================================================================
