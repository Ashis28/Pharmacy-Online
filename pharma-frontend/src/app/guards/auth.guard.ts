/**
 * Route Guards — Control which pages users can access based on their auth state.
 *
 * WHAT IS A ROUTE GUARD?
 *   A guard is a function that runs BEFORE Angular loads a page component.
 *   It returns true (allow navigation) or false (block navigation).
 *   If false, it also redirects the user to the appropriate page.
 *
 * WHY THREE GUARDS?
 *   authGuard  — "You must be logged in to see this page"
 *                Used for: /orders, /payment, /prescriptions, /notifications
 *
 *   adminGuard — "You must be logged in AND be an admin"
 *                Used for: /admin/* (all admin pages)
 *
 *   guestGuard — "You must NOT be logged in to see this page"
 *                Used for: /login, /register
 *                (prevents logged-in users from seeing the login page)
 *
 * HOW THEY WORK:
 *   Each guard uses inject() to get AuthService and Router.
 *   inject() is Angular's way of getting services inside functional guards
 *   (as opposed to class-based guards which use constructor injection).
 *
 * WHERE THEY ARE APPLIED:
 *   See app.routes.ts — each route has a `canActivate: [guardName]` property.
 */

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * authGuard — Protects pages that require any logged-in user.
 *
 * Logic:
 *   - Logged in? → Allow access (return true)
 *   - Not logged in? → Redirect to /login (return false)
 *
 * UI effect: If a guest tries to visit /orders directly, they are
 * automatically sent to the login page instead.
 */
export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) return true; // user has a valid token — allow

  // No token found — send to login page
  router.navigate(['/login']);
  return false;
};

/**
 * adminGuard — Protects pages that require ADMIN role specifically.
 *
 * Logic:
 *   - Logged in AND admin? → Allow access (return true)
 *   - Logged in but NOT admin? → Redirect to /home (they're a customer)
 *   - Not logged in at all? → Redirect to /login
 *
 * UI effect: If a customer tries to visit /admin/dashboard directly,
 * they are sent to /home. If a guest tries, they go to /login.
 */
export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn() && auth.isAdmin()) return true; // admin — allow

  // Redirect based on whether they're logged in or not
  router.navigate([auth.isLoggedIn() ? '/home' : '/login']);
  return false;
};

/**
 * guestGuard — Protects pages that should only be seen by guests (not logged-in users).
 *
 * Logic:
 *   - NOT logged in? → Allow access (return true) — they can see login/register
 *   - Logged in as admin? → Redirect to /admin/dashboard
 *   - Logged in as customer? → Redirect to /home
 *
 * UI effect: If an already-logged-in admin tries to visit /login,
 * they are automatically sent to /admin/dashboard instead.
 * This prevents the awkward situation of a logged-in user seeing the login form.
 */
export const guestGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) return true; // not logged in — allow access to login/register

  // Already logged in — redirect to their home page
  router.navigate([auth.isAdmin() ? '/admin/dashboard' : '/home']);
  return false;
};
