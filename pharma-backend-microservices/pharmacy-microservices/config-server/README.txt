================================================================================
  CONFIG SERVER - Centralized Configuration Management
================================================================================

Port: 8888
Purpose: Serves externalized configuration to all microservices

All services can fetch their configuration from this server instead of using
local application.yml files. Services are configured with fail-fast=false so
they fall back to local config if Config Server is unavailable.

Start order: Start this AFTER eureka-server but BEFORE all other services.
