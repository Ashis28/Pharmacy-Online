================================================================================
  NOTIFICATION SERVICE - README
================================================================================

PORT: 9096
MESSAGE BROKER: RabbitMQ (port 5672)

--------------------------------------------------------------------------------
PURPOSE
--------------------------------------------------------------------------------
Listens to RabbitMQ for notification events published by other services
(primarily order-service) and processes them. Currently logs notifications to
console. Designed as the integration point for email (JavaMailSender), SMS
(Twilio), or push notifications (Firebase) — swap in the provider without
touching any other service.

--------------------------------------------------------------------------------
ENDPOINTS
--------------------------------------------------------------------------------
POST   /api/notifications/send   - Manually trigger a notification (internal use)

--------------------------------------------------------------------------------
DATABASE TABLES
--------------------------------------------------------------------------------
None. This service has no persistence layer. All state is event-driven via
RabbitMQ messages.

--------------------------------------------------------------------------------
ENTITY / VARIABLE RELATIONS
--------------------------------------------------------------------------------

NotificationRequest (RabbitMQ message DTO)
  recipientEmail  String  -- destination email address
  subject         String  -- notification subject line
  message         String  -- notification body
  type            String  -- event type, e.g. ORDER_PLACED

RabbitMQ Config:
  Exchange:     pharmacy.exchange  (DirectExchange)
  Queue:        notification.queue
  Routing Key:  notification.key
  Binding:      notification.queue <- notification.key <- pharmacy.exchange

--------------------------------------------------------------------------------
SECURITY
--------------------------------------------------------------------------------
- All HTTP endpoints are permitAll (internal service, not customer-facing)
- CSRF disabled (stateless, no session)
- RabbitMQ connection secured by username/password (default: guest/guest)

--------------------------------------------------------------------------------
EXCEPTIONS
--------------------------------------------------------------------------------
GlobalExceptionHandler handles:
  IllegalArgumentException     (400) - invalid request body
  Exception (generic fallback) (500)

Note: RabbitMQ listener errors are logged but do not crash the service.
Failed message processing is swallowed to prevent queue poisoning.

--------------------------------------------------------------------------------
INTER-SERVICE RELATIONS
--------------------------------------------------------------------------------
Consumed from (RabbitMQ publishers):
  order-service  -> publishes ORDER_PLACED events on order creation

HTTP endpoint consumed by:
  Internal/manual testing only via POST /api/notifications/send

No outbound Feign calls from this service.

================================================================================
