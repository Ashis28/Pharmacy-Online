/**
 * app.routes.ts — Defines ALL the pages (routes) in the application.
 *
 * WHAT IS ROUTING?
 *   Routing maps a URL path to a component. When the user visits /home,
 *   Angular loads HomeComponent. When they visit /medicines, it loads
 *   MedicinesListComponent. This file is the complete map of all pages.
 *
 * LAZY LOADING (loadComponent):
 *   Instead of loading ALL components when the app starts, we use lazy loading.
 *   Each route uses loadComponent() which only downloads the component's code
 *   when the user actually navigates to that page.
 *   This makes the initial app load much faster.
 *
 * ROUTE GUARDS (canActivate):
 *   Guards run before a page loads to check if the user is allowed.
 *   - authGuard  → user must be logged in
 *   - adminGuard → user must be logged in AND have ADMIN role
 *   - guestGuard → user must NOT be logged in (for login/register pages)
 *
 * NESTED ROUTES (children):
 *   Admin routes are nested under /admin. The AdminLayoutComponent provides
 *   the sidebar, and child routes render inside it via a nested <router-outlet>.
 *
 * ROUTE DATA (data: { isAdmin: true }):
 *   Extra data passed to a component via ActivatedRoute.snapshot.data.
 *   Used to tell RegisterComponent to show the admin registration form.
 */

import { Routes } from '@angular/router';
import { authGuard, adminGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [

  // ── Default redirect ─────────────────────────────────────────────────────
  {
    path: '',
    redirectTo: 'home',  // visiting / redirects to /home
    pathMatch: 'full'    // only match if the ENTIRE path is empty
  },

  // ── Public pages — no login required ────────────────────────────────────
  {
    path: 'home',
    // Lazy load: HomeComponent code is only downloaded when user visits /home
    loadComponent: () => import('./components/home/home').then(m => m.HomeComponent)
  },
  {
    path: 'medicines',
    loadComponent: () => import('./components/medicines/medicines-list/medicines-list').then(m => m.MedicinesListComponent)
  },

  // ── Auth pages — only for guests (not logged-in users) ──────────────────
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login').then(m => m.LoginComponent),
    canActivate: [guestGuard]  // logged-in users are redirected away from this page
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register').then(m => m.RegisterComponent),
    canActivate: [guestGuard]  // same — logged-in users don't need to register
  },
  {
    path: 'register-admin',
    loadComponent: () => import('./components/auth/register/register').then(m => m.RegisterComponent),
    data: { isAdmin: true },   // tells RegisterComponent to show admin token field
    canActivate: [guestGuard]
  },

  // ── Customer pages — require login ───────────────────────────────────────
  {
    path: 'cart',
    // Cart doesn't require login to VIEW, but adding items does (handled in CartService)
    loadComponent: () => import('./components/cart/cart').then(m => m.CartComponent)
  },
  {
    path: 'orders',
    loadComponent: () => import('./components/orders/orders').then(m => m.OrdersComponent),
    canActivate: [authGuard]   // must be logged in to see order history
  },
  {
    path: 'payment/:orderId',  // :orderId is a URL parameter (e.g. /payment/42)
    loadComponent: () => import('./components/payment/payment').then(m => m.PaymentComponent),
    canActivate: [authGuard]   // must be logged in to pay
  },
  {
    path: 'prescriptions',
    loadComponent: () => import('./components/prescriptions/prescriptions').then(m => m.PrescriptionsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'notifications',
    loadComponent: () => import('./components/notifications/notifications').then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  },

  // ── Admin pages — require ADMIN role ────────────────────────────────────
  {
    path: 'admin',
    // AdminLayoutComponent provides the sidebar shell for all admin pages
    loadComponent: () => import('./components/admin/admin-layout/admin-layout').then(m => m.AdminLayoutComponent),
    canActivate: [adminGuard], // blocks non-admins from ALL /admin/* pages
    children: [
      // Default admin page
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      // Each child renders inside AdminLayoutComponent's <router-outlet>
      {
        path: 'dashboard',
        loadComponent: () => import('./components/admin/dashboard/dashboard').then(m => m.DashboardComponent)
      },
      {
        path: 'medicines',
        loadComponent: () => import('./components/admin/admin-medicines/admin-medicines').then(m => m.AdminMedicinesComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./components/admin/admin-orders/admin-orders').then(m => m.AdminOrdersComponent)
      },
      {
        path: 'prescriptions',
        loadComponent: () => import('./components/admin/admin-prescriptions/admin-prescriptions').then(m => m.AdminPrescriptionsComponent)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./components/admin/admin-notifications/admin-notifications').then(m => m.AdminNotificationsComponent)
      }
    ]
  },

  // ── Catch-all — redirect unknown URLs to home ────────────────────────────
  {
    path: '**',       // ** matches any path not matched above
    redirectTo: 'home'
  }
];
