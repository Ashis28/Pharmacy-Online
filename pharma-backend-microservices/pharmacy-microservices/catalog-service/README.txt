================================================================================
  CATALOG SERVICE - README
================================================================================

PORT: 9092
DATABASE: catalog_db (MySQL)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Manages the pharmacy product catalog. Handles medicines (CRUD), medicine
categories, and prescription uploads/approvals. Acts as the central data store
for medicine information that other services (order-service, admin-service)
query via Feign clients.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
Medicines:
  GET    /api/catalog/medicines              - List all medicines (public)
  GET    /api/catalog/medicines/{id}         - Get medicine by ID (public)
  GET    /api/catalog/medicines/search?name= - Search by name (public)
  GET    /api/catalog/medicines/low-stock-count  - Count low stock (ADMIN only)
  POST   /api/catalog/medicines              - Add medicine (ADMIN only)
  PUT    /api/catalog/medicines/{id}         - Update medicine (ADMIN only)
  DELETE /api/catalog/medicines/{id}         - Delete medicine (ADMIN only)

Prescriptions:
  POST   /api/catalog/prescriptions/upload          - Upload prescription (auth required)
  GET    /api/catalog/prescriptions/my?customerId=  - Customer's own prescriptions
  GET    /api/catalog/prescriptions/pending         - All pending (ADMIN only)
  PUT    /api/catalog/prescriptions/{id}/approve    - Approve (ADMIN only)
  PUT    /api/catalog/prescriptions/{id}/reject     - Reject with reason (ADMIN only)

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------

TABLE: categories
  id            BIGINT (PK, auto-increment)
  name          VARCHAR NOT NULL, UNIQUE
  description   VARCHAR

TABLE: medicines
  id                    BIGINT (PK, auto-increment)
  name                  VARCHAR NOT NULL
  description           VARCHAR
  price                 DECIMAL NOT NULL
  stock_quantity        INT NOT NULL
  requires_prescription BOOLEAN
  expiry_date           DATE
  category_id           BIGINT (FK -> categories.id)

TABLE: prescriptions
  id                BIGINT (PK, auto-increment)
  customer_id       BIGINT NOT NULL  (references auth_db users.id logically)
  customer_email    VARCHAR
  image_url         VARCHAR NOT NULL
  status            VARCHAR (enum: PENDING, APPROVED, REJECTED)
  rejection_reason  VARCHAR
  uploaded_at       DATETIME

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

Category
  id            Long
  name          String    -- unique category name (e.g. Antibiotics, Vitamins)
  description   String

Medicine
  id                    Long
  name                  String
  description           String
  price                 BigDecimal
  stockQuantity         Integer
  requiresPrescription  boolean
  expiryDate            LocalDate
  category              Category   -- @ManyToOne -> categories.id (LAZY fetch)

  Low stock threshold: stockQuantity <= 10 (defined in MedicineRepository query)

Prescription
  id                Long
  customerId        Long       -- logical FK to auth_db users.id (no hard constraint)
  customerEmail     String
  imageUrl          String
  status            PrescriptionStatus  (PENDING -> APPROVED or REJECTED)
  rejectionReason   String     -- only populated on REJECTED
  uploadedAt        LocalDateTime

MedicineRequest (input DTO)
  name              String  @NotBlank
  description       String
  price             BigDecimal  @NotNull @Min(0)
  stockQuantity     Integer  @NotNull @Min(0)
  requiresPrescription  boolean
  expiryDate        LocalDate
  categoryId        Long    -- optional, maps to Category

MedicineResponse (output DTO)
  id, name, description, price, stockQuantity,
  requiresPrescription, expiryDate, categoryName (denormalized from Category)

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- JWT validated via JwtAuthFilter on every request
- GET /api/catalog/medicines/** is public (no auth required)
- All write operations (POST/PUT/DELETE) require ADMIN role (@PreAuthorize)
- Prescription approve/reject require ADMIN role
- CSRF disabled (stateless JWT service)

--------------------------------------------------------------------------------
EXCEPTIONS (custom)
--------------------------------------------------------------------------------
MedicineNotFoundException     (404 NOT_FOUND)
  - getById() with non-existent ID
  - update() with non-existent ID
  - delete() with non-existent ID

CategoryNotFoundException     (404 NOT_FOUND)
  - create/update medicine when categoryId does not exist in categories table

PrescriptionNotFoundException (404 NOT_FOUND)
  - approve/reject with non-existent prescription ID

GlobalExceptionHandler also handles:
  AccessDeniedException            (403) - non-ADMIN tries write operations
  MethodArgumentNotValidException  (400) - blank name, negative price/stock
  IllegalArgumentException         (400)
  Exception (generic fallback)     (500)

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
- Consumed by order-service via Feign: GET /api/catalog/medicines/{id}
- Consumed by admin-service via Feign: full CRUD + prescriptions + low-stock-count
- No outbound Feign calls from this service

================================================================================
