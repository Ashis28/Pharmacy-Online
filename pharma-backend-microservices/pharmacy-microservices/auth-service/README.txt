================================================================================
  AUTH SERVICE - README
================================================================================

PORT: 9091
DATABASE: auth_db (MySQL)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Handles all user authentication and authorization for the pharmacy platform.
Responsible for user registration (signup), login, JWT token generation, and
CSRF token provisioning. Every other service trusts the JWT tokens issued here.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
POST   /api/auth/signup     - Register a new user
POST   /api/auth/login      - Login and receive a JWT token
GET    /api/auth/csrf        - Fetch a CSRF token (cookie-based, for clients)

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------

TABLE: users
  id          BIGINT (PK, auto-increment)
  name        VARCHAR NOT NULL
  email       VARCHAR NOT NULL, UNIQUE
  password    VARCHAR NOT NULL  (BCrypt encoded)

TABLE: user_roles  (element collection, joined to users)
  user_id     BIGINT (FK -> users.id)
  role        VARCHAR  (enum: CUSTOMER, ADMIN, PHARMACIST)

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

User
  id          Long
  name        String
  email       String          -- unique login identifier
  password    String          -- BCrypt hashed, never stored plain
  roles       Set<Role>       -- @ElementCollection, stored in user_roles table

Role (enum)
  CUSTOMER    -- default role when none specified at signup
  ADMIN       -- full platform access
  PHARMACIST  -- pharmacy staff access

SignupRequest  ->  User  (mapped in AuthService.signup)
  name        -> user.name
  email       -> user.email
  password    -> encoder.encode(password) -> user.password
  roles       -> Set<Role> parsed from strings -> user.roles

LoginRequest  ->  JWT token  (via JwtService)
  email       -> looked up in UserRepository
  password    -> matched against BCrypt hash

LoginResponse
  token       String  -- signed HS256 JWT
  name        String  -- user's display name
  roles       Set<String>

JWT Claims stored in token:
  sub         email
  roles       list of role names
  iat         issued-at timestamp
  exp         expiry (default 86400000ms = 24 hours)

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- CSRF is ENABLED with CookieCsrfTokenRepository (httpOnly=false so JS can read)
- /api/auth/login and /api/auth/signup are CSRF-exempt (bootstrap endpoints)
- All other state-changing calls require X-XSRF-TOKEN header
- Sessions are STATELESS (no server-side session)
- Passwords encoded with BCryptPasswordEncoder

--------------------------------------------------------------------------------
EXCEPTIONS (custom)
--------------------------------------------------------------------------------
UserAlreadyExistsException   (409 CONFLICT)
  - Thrown when signup is attempted with an email already in the database

UserNotFoundException        (404 NOT_FOUND)
  - Thrown when login email does not match any registered user

InvalidCredentialsException  (401 UNAUTHORIZED)
  - Thrown when the provided password does not match the stored BCrypt hash

GlobalExceptionHandler also handles:
  MethodArgumentNotValidException  (400) - blank/invalid email, blank password
  IllegalArgumentException         (400) - invalid role string at signup
  Exception (generic fallback)     (500)

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
- Standalone service; no Feign clients
- All other services validate JWT tokens issued by this service
- Gateway-service routes /api/auth/** directly (no JWT filter on this route)

================================================================================
