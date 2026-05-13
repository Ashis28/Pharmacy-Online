# PharmaOnline — Frontend Documentation

## What is this project?

PharmaOnline is an Angular 21 single-page application (SPA) for an online pharmacy. Customers can browse medicines, add them to a cart, place orders, upload prescriptions, and track deliveries. Admins get a separate dashboard to manage medicines, orders, prescriptions, and send notifications.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Angular 21 (standalone components) | UI framework |
| RxJS | Reactive HTTP calls and event streams |
| Angular Signals | Reactive state (cart, auth user) |
| Angular Router | Client-side navigation with lazy loading |
| Angular HttpClient | REST API calls to the backend |
| Plain CSS | Styling (no preprocessor) |

---

## Project Structure

```
src/
├── app/
│   ├── app.ts                  ← Root component (navbar + router-outlet + footer)
│   ├── app.routes.ts           ← All page routes
│   ├── app.config.ts           ← App bootstrap config (router, http, interceptor)
│   │
│   ├── components/
│   │   ├── navbar/             ← Top navigation bar
│   │   ├── footer/             ← Site footer
│   │   ├── home/               ← Landing page (/home)
│   │   ├── auth/
│   │   │   ├── login/          ← Login page (/login)
│   │   │   └── register/       ← Register page (/register, /register-admin)
│   │   ├── medicines/
│   │   │   └── medicines-list/ ← Browse medicines (/medicines)
│   │   ├── cart/               ← Shopping cart (/cart)
│   │   ├── orders/             ← My orders (/orders)
│   │   ├── payment/            ← Payment page (/payment/:orderId)
│   │   ├── prescriptions/      ← Upload & track prescriptions (/prescriptions)
│   │   ├── notifications/      ← Order notifications (/notifications)
│   │   └── admin/
│   │       ├── admin-layout/   ← Admin sidebar shell
│   │       ├── dashboard/      ← Admin dashboard (/admin/dashboard)
│   │       ├── admin-medicines/← Manage medicines (/admin/medicines)
│   │       ├── admin-orders/   ← Manage orders (/admin/orders)
│   │       ├── admin-prescriptions/ ← Review prescriptions (/admin/prescriptions)
│   │       └── admin-notifications/ ← Send notifications (/admin/notifications)
│   │
│   ├── services/
│   │   ├── auth.service.ts     ← Login, logout, token storage, user signal
│   │   ├── cart.service.ts     ← In-memory cart with signals
│   │   ├── medicine.service.ts ← Medicines & prescriptions API
│   │   ├── order.service.ts    ← Orders API
│   │   ├── payment.service.ts  ← Payment API
│   │   └── admin.service.ts    ← Admin-specific API calls
│   │
│   ├── guards/
│   │   └── auth.guard.ts       ← authGuard, adminGuard, guestGuard
│   │
│   ├── interceptors/
│   │   └── auth.interceptor.ts ← Auto-attaches JWT to every HTTP request
│   │
│   └── models/
│       ├── auth.models.ts      ← LoginRequest, LoginResponse, AuthUser
│       ├── medicine.models.ts  ← Medicine, Prescription
│       ├── order.models.ts     ← Order, OrderItem, CartItem
│       ├── payment.models.ts   ← PaymentRequest, PaymentResponse
│       └── admin.models.ts     ← DashboardStats, NotificationRequest
│
├── styles.css                  ← Global CSS variables and base styles
└── index.html                  ← App shell HTML
```

---

## Routing

All routes are defined in `app.routes.ts`. The app uses **lazy loading** — each component's code is only downloaded when the user navigates to that page.

| URL | Component | Guard |
|-----|-----------|-------|
| `/` | → redirects to `/home` | — |
| `/home` | HomeComponent | — |
| `/medicines` | MedicinesListComponent | — |
| `/login` | LoginComponent | guestGuard |
| `/register` | RegisterComponent | guestGuard |
| `/register-admin` | RegisterComponent (admin mode) | guestGuard |
| `/cart` | CartComponent | — |
| `/orders` | OrdersComponent | authGuard |
| `/payment/:orderId` | PaymentComponent | authGuard |
| `/prescriptions` | PrescriptionsComponent | authGuard |
| `/notifications` | NotificationsComponent | authGuard |
| `/admin` | → redirects to `/admin/dashboard` | adminGuard |
| `/admin/dashboard` | DashboardComponent | adminGuard |
| `/admin/medicines` | AdminMedicinesComponent | adminGuard |
| `/admin/orders` | AdminOrdersComponent | adminGuard |
| `/admin/prescriptions` | AdminPrescriptionsComponent | adminGuard |
| `/admin/notifications` | AdminNotificationsComponent | adminGuard |
| `**` | → redirects to `/home` | — |

### Route Guards

- **authGuard** — blocks guests from protected pages, redirects to `/login`
- **adminGuard** — blocks non-admins; customers go to `/home`, guests go to `/login`
- **guestGuard** — blocks logged-in users from login/register; admins go to `/admin/dashboard`, customers go to `/home`

---

## Authentication

Authentication is handled entirely by `AuthService`.

### How it works

1. User submits email + password on `/login`
2. `AuthService.login()` calls `POST /api/auth/login`
3. Backend returns `{ token, name, id, roles }`
4. Token and user object are saved to `localStorage`
5. `currentUser` signal is updated → navbar re-renders immediately
6. User is redirected based on role (admin → `/admin/dashboard`, customer → `/home`)

### Session persistence

On every page load, `AuthService` reads `localStorage` and restores the session. The user stays logged in across browser refreshes.

### JWT Interceptor

`auth.interceptor.ts` runs before every HTTP request. It reads the token from `AuthService.getToken()` and adds `Authorization: Bearer <token>` to the request headers automatically. If the server returns `401 Unauthorized`, it logs the user out and redirects to `/login`.

---

## Services

### AuthService
- `login(request)` — authenticates and stores session
- `logout()` — clears storage, resets signal, clears cart, redirects to `/login`
- `isLoggedIn()` — returns `true` if a token exists in localStorage
- `isAdmin()` — returns `true` if current user has `ADMIN` role
- `isCustomer()` — returns `true` if current user has `CUSTOMER` role
- `getUserId()` — returns the current user's numeric ID
- `getUserEmail()` — returns the current user's email
- `getToken()` — returns the raw JWT string

### CartService
Manages the shopping cart entirely in memory using Angular signals.

- `cartItems` — readonly signal: list of `{ medicine, quantity }` pairs
- `cartCount` — computed signal: total number of items (sum of quantities)
- `cartTotal` — computed signal: total price
- `addToCart(medicine)` — adds item; redirects to `/login` if not authenticated
- `removeFromCart(medicineId)` — removes an item
- `updateQuantity(medicineId, quantity)` — updates quantity; removes if 0; caps at stock limit
- `clearCart()` — empties the cart

### MedicineService
Calls the catalog-service backend (`/api/catalog`).

- `getAllMedicines()` — fetch all medicines (public)
- `searchMedicines(name)` — search by name (public)
- `addMedicine(medicine)` — create medicine (admin)
- `updateMedicine(id, medicine)` — update medicine (admin)
- `deleteMedicine(id)` — delete medicine (admin)
- `uploadPrescription(customerId, imageUrl, email)` — upload prescription
- `getMyPrescriptions(customerId)` — get customer's prescriptions
- `getPendingPrescriptions()` — get all pending prescriptions (admin)
- `approvePrescription(id)` — approve a prescription (admin)
- `rejectPrescription(id, reason)` — reject with reason (admin)

### OrderService
Calls the order-service backend (`/api/orders`).

- `placeOrder(request, email)` — create a new order
- `getOrderById(id)` — fetch a single order
- `getOrdersByCustomer(customerId)` — fetch customer's order history
- `getAllOrders()` — fetch all orders (admin)
- `updateOrderStatus(id, status)` — change order status (admin)
- `markOrderAsPaid(id)` — mark order as PAID after payment

### PaymentService
Calls the payment-service backend (`/api/payments`).

- `processPayment(request)` — process a payment (UPI/CARD/NET_BANKING/CASH)
- `getPaymentByOrderId(orderId)` — fetch payment record for an order

### AdminService
Aggregates admin operations.

- `getDashboard()` — fetch dashboard stats (total orders, pending prescriptions, low stock, revenue)
- `addMedicine / updateMedicine / deleteMedicine` — medicine CRUD
- `getPendingPrescriptions / approvePrescription / rejectPrescription` — prescription management
- `getAllOrders / updateOrderStatus` — order management
- `sendNotification(request)` — send a notification via notification-service

---

## Key Components

### Navbar
Shows different links based on auth state:
- **Guest**: Login + Register buttons
- **Customer**: Home, Medicines, My Orders, Prescriptions, Notifications, Cart badge, Logout
- **Admin**: Home, Medicines, Admin Panel link, Cart badge, Logout

Reads `auth.currentUser()` signal — updates automatically on login/logout.

### Home Page
- Hero section with animated pill icons and stats
- Features section (4 cards)
- Featured medicines (first 6 from catalog) with Add to Cart buttons
- CTA section for guests

### Medicines List
- Search bar (triggers API search after 2+ characters)
- Filter by type (All / OTC / Rx)
- Sort by name or price
- Grid of medicine cards with stock badges
- Add to Cart button (shows "Login to Buy" for guests)

### Cart
- Lists cart items with quantity controls (−/+) and remove button
- Stock limit enforced — + button disabled at max stock
- Order summary with delivery address input
- Place Order button → creates order → redirects to payment page

### Orders
- Lists all customer orders sorted newest first
- Expandable cards showing items, delivery address, and progress tracker
- Status badges with color coding
- "Pay Now" button for PENDING orders

### Payment
- Order summary on the left
- Payment method selector (UPI, Card, Net Banking, Cash on Delivery)
- UPI: shows QR code generated via Google Charts API
- Card: opens a Razorpay-style modal with live card preview
- Processing overlay with animated steps
- Success screen with transaction details

### Prescriptions
- Upload form (paste image URL)
- List of uploaded prescriptions with status badges
- Document-style cards showing prescription image and status

### Admin Dashboard
- 4 stat cards: Total Orders, Pending Prescriptions, Low Stock Items, Monthly Revenue
- Quick action buttons
- Recent orders table (last 5)
- Low stock alert list

### Admin Medicines
- Table of all medicines with stock highlighting
- Add/Edit modal with form validation
- Delete confirmation dialog

### Admin Orders
- Table of all orders with status filter dropdown
- Inline status update dropdown per row
- Expandable rows showing order items

### Admin Prescriptions
- Cards for PENDING prescriptions with Approve/Reject buttons
- Rejection reason modal

### Admin Notifications
- Form to send custom notifications to any email
- Notification type selector
- Session-based sent log (last 10)

---

## Order Status Flow

```
PENDING → PAID → PACKED → SHIPPED → DELIVERED
                                  ↘ CANCELLED (any stage)
```

- **PENDING** — order placed, awaiting payment
- **PAID** — payment confirmed
- **PACKED** — admin has packed the order
- **SHIPPED** — order dispatched for delivery
- **DELIVERED** — order received by customer
- **CANCELLED** — order cancelled by admin

---

## CSS Architecture

All styles use plain CSS (no preprocessor). CSS custom properties (variables) are defined globally in `src/styles.css`:

```css
:root {
  --bg: #f8fafc;          /* page background */
  --surface: #ffffff;     /* card/panel background */
  --surface-2: #f1f5f9;   /* input/secondary background */
  --border: rgba(30,41,59,0.14);
  --text: #1e293b;
  --muted: rgba(15,23,42,0.65);
  --primary: #2563eb;
  --primary-2: #1d4ed8;
  --success: #16a34a;
  --warning: #f59e0b;
  --danger: #ef4444;
  --shadow-sm / --shadow-md
  --radius: 14px;
}
```

Auth page shared styles (`auth-shared.css`) are loaded globally via `angular.json` so both login and register pages inherit them.

---

## Running the App

```bash
cd pharma-frontend
npm install
npm start          # starts dev server at http://localhost:4200
```

The app proxies API calls to `http://localhost:8888` via `proxy.conf.json`. Make sure the backend gateway is running on port 8888.

---

## Building for Production

```bash
npm run build
```

Output goes to `dist/pharma-frontend/browser/`.

---

## Environment Configuration

API base URL is set in `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8888'
};
```

Change `apiUrl` for production deployments.

---

## Known Limitations / Notes

- **Prescription upload** accepts image URLs only (no file upload). In production you'd integrate with a file storage service (S3, Cloudinary, etc.).
- **Payment processing** is simulated — the backend always returns SUCCESS. Real integration would use Razorpay/Stripe SDKs.
- **Notifications** are derived client-side from order history, not from a real-time notification system.
- **Cart** is in-memory only — it clears on logout and page refresh after logout.
- **Token expiry** is handled by the interceptor catching 401 responses and logging the user out.
