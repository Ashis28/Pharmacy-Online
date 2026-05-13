================================================================================
  EUREKA SERVER - README
================================================================================

PORT: 8761

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Netflix Eureka service registry. All microservices register themselves here on
startup and query it to discover other services by name (e.g. "catalog-service")
rather than hardcoded IPs. Enables load balancing and dynamic scaling.

--------------------------------------------------------------------------------
REGISTERED SERVICES
--------------------------------------------------------------------------------
  auth-service          port 9091
  catalog-service       port 9092
  order-service         port 9093
  admin-service         port 9094
  payment-service       port 9095
  notification-service  port 9096
  gateway-service       port 8888

--------------------------------------------------------------------------------
DASHBOARD
--------------------------------------------------------------------------------
Access the Eureka web UI at: http://localhost:8761

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
- Does not register itself (register-with-eureka: false)
- Does not fetch registry (fetch-registry: false)
- All other services point to http://localhost:8761/eureka/

================================================================================
