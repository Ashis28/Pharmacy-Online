# PharmaOnline — How to Start the Project

## Prerequisites (must be running before starting)
- **MySQL** on port 3306 (password: `yosker`)
- **RabbitMQ** on port 5672 (auto-starts as Windows service)
- **Java 17+** installed
- **Node.js 18+** and **Angular CLI** installed

---

## Option A — One-Click Start (Recommended)

Open **PowerShell as Administrator** in the project root folder:

```powershell
# Terminal 1 — Start all backend services
powershell -ExecutionPolicy Bypass -File start-all-services.ps1

# Terminal 2 — Start frontend (open a NEW terminal)
cd pharma-frontend
npm start
```

Wait ~60 seconds for all services to boot, then open: **http://localhost:4200**

---

## Option B — Manual Start (Step by Step)

Open a **PowerShell** terminal in the project root. Run each block in order:

### Step 1 — Start Eureka Server (Service Registry)
```powershell
java -jar eureka-server\target\eureka-server-1.0.0.jar
```
Wait until you see: `Started EurekaServerApplication`  
Check: http://localhost:8761

### Step 2 — Start Core Services (open 6 NEW terminals, one per service)

**Auth Service** (port 9091):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar auth-service\target\auth-service-1.0.0.jar
```

**Catalog Service** (port 9092):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar catalog-service\target\catalog-service-1.0.0.jar
```

**Order Service** (port 9093):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar order-service\target\order-service-1.0.0.jar
```

**Admin Service** (port 9094):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar admin-service\target\admin-service-1.0.0.jar
```

**Payment Service** (port 9095):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar payment-service\target\payment-service-1.0.0.jar
```

**Notification Service** (port 9096):
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar notification-service\target\notification-service-1.0.0.jar
```

### Step 3 — Start Gateway (after all services are up)
```powershell
java -DDB_PASS=yosker -DDB_USER=root -DDB_HOST=localhost -jar gateway-service\target\gateway-service-1.0.0.jar
```
Wait until you see: `Netty started on port 8888`

### Step 4 — Start Frontend
```powershell
cd pharma-frontend
npm start
```
Wait until you see: `Local: http://localhost:4200/`

---

## Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | http://localhost:4200 | Angular UI |
| **API Gateway** | http://localhost:8888 | All API calls go here |
| **Eureka Dashboard** | http://localhost:8761 | Service registry |
| **Swagger UI** | http://localhost:8888/swagger-ui.html | API docs & testing |
| **RabbitMQ UI** | http://localhost:15672 | Message queue (guest/guest) |

---

## Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@pharmacy.com | admin123 |
| Customer | john@example.com | customer123 |

---

## Quick API Test (PowerShell)

```powershell
# Login
$lr = Invoke-RestMethod -Uri "http://localhost:8888/api/auth/login" `
  -Method POST -Body '{"email":"john@example.com","password":"customer123"}' `
  -ContentType "application/json"
Write-Host "Logged in as: $($lr.name) (id=$($lr.id))"

# Browse medicines
$meds = Invoke-RestMethod -Uri "http://localhost:8888/api/catalog/medicines" -Method GET
Write-Host "Medicines: $($meds.Count)"

# Place order
$h = @{"Authorization"="Bearer $($lr.token)";"Content-Type"="application/json";"X-Auth-User"="john@example.com"}
$order = Invoke-RestMethod -Uri "http://localhost:8888/api/orders" -Method POST `
  -Body (@{customerId=[int]$lr.id; deliveryAddress="123 Test"; items=@(@{medicineId=[int]$meds[0].id; quantity=1})} | ConvertTo-Json -Depth 3) `
  -Headers $h
Write-Host "Order: #$($order.id) Rs.$($order.totalAmount)"

# Pay
$pay = Invoke-RestMethod -Uri "http://localhost:8888/api/payments/process" -Method POST `
  -Body (@{orderId=[int]$order.id; customerId=[int]$lr.id; amount=$order.totalAmount; paymentMethod="UPI"} | ConvertTo-Json) `
  -Headers $h
Write-Host "Payment: $($pay.status) TXN=$($pay.transactionId)"
```

---

## RabbitMQ Status

RabbitMQ runs as a **Windows Service** and starts automatically.

- **AMQP port** (5672): Used by order-service and notification-service
- **Management UI** (15672): http://localhost:15672 — login: `guest` / `guest`

To enable Management UI (run once as Administrator):
```powershell
& "C:\Program Files\RabbitMQ Server\rabbitmq_server-4.2.5\sbin\rabbitmq-plugins.bat" enable rabbitmq_management
Restart-Service RabbitMQ
```

**How RabbitMQ is used:**
1. Customer places order → order-service publishes `ORDER_PLACED` event to `pharmacy.exchange`
2. notification-service listens on `notification.queue` and logs the notification
3. If RabbitMQ is down, orders still work (notifications are non-critical)

---

## Zipkin (Distributed Tracing)

Zipkin is **optional** — services work without it.  
To run Zipkin locally:
```powershell
# Using Docker
docker run -d -p 9411:9411 openzipkin/zipkin

# Or download the JAR
java -jar zipkin-server-3.x.x-exec.jar
```
Then open: http://localhost:9411

---

## Rebuild Services (if you change backend code)

```powershell
# Rebuild a specific service
mvn package -DskipTests -f order-service/pom.xml

# Rebuild all services
mvn package -DskipTests
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Service won't start | Check `<service>\logs\err.log` |
| 401 Unauthorized | Token expired — login again |
| 503 Service Unavailable | Downstream service is down — check ports |
| Cart not clearing on logout | Hard refresh browser (Ctrl+Shift+R) |
| Medicines not loading | Check catalog-service is on port 9092 |
| Orders failing | Check order-service logs for DB errors |
