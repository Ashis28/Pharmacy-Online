================================================================================
  GATEWAY SERVICE - README
================================================================================

PORT: 8888

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Single entry point for all client requests. Routes incoming HTTP traffic to the
correct downstream microservice using Spring Cloud Gateway with Eureka-based
load balancing. Validates JWT tokens on protected routes before forwarding,
and injects the authenticated user's email as the X-Auth-User header so
downstream services know who is making the request without re-validating the token.

--------------------------------------------------------------------------------
ROUTING TABLE
--------------------------------------------------------------------------------
Path Prefix          -> Target Service       JWT Filter
/api/auth/**         -> auth-service         NO  (public: login/signup)
/api/catalog/**      -> catalog-service      YES
/api/orders/**       -> order-service        YES
/api/admin/**        -> admin-service        YES
/api/payments/**     -> payment-service      YES
/api/notifications/**-> notification-service YES

All routes use lb:// (load-balanced) URIs resolved via Eureka.

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- JwtAuthFilter validates the Authorization: Bearer <token> header
- On valid token: extracts email, sets X-Auth-User header, forwards request
- On invalid/missing token: returns 401 Unauthorized immediately
- JWT secret must match the secret used by auth-service

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
- Registers with Eureka (port 8761) for service discovery
- Forwards to all other services; no direct DB access
- Does not call any service via Feign

================================================================================
