/**
 * AuthService — The central authentication brain of the entire frontend.
 *
 * WHY THIS EXISTS:
 *   Every page needs to know "is the user logged in?" and "who are they?".
 *   Instead of each component making its own API calls and storing tokens,
 *   this single service handles ALL auth logic in one place.
 *
 * HOW IT WORKS:
 *   1. On login  → calls backend POST /api/auth/login
 *                → stores JWT token + user info in localStorage
 *                → updates the reactive `currentUser` signal
 *   2. On logout → clears localStorage, resets signal, clears cart, redirects
 *   3. On page refresh → reads localStorage to restore the session automatically
 *
 * WHAT IS A SIGNAL?
 *   `signal<T>` is Angular's reactive state primitive (like useState in React).
 *   When `currentUser` changes, every component that reads it automatically
 *   re-renders. The navbar shows/hides links based on this signal.
 *
 * WHAT IS localStorage?
 *   A browser key-value store that persists across page refreshes.
 *   We store the JWT token here so the user stays logged in even after
 *   closing and reopening the browser tab.
 */

import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SignupRequest, LoginRequest, LoginResponse, AuthUser } from '../models/auth.models';
import { CartService } from './cart.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Base URL for all auth API calls — reads from environment.ts
  private readonly API = `${environment.apiUrl}/api/auth`;

  // Keys used to store data in localStorage
  private readonly TOKEN_KEY = 'pharma_token';  // stores the raw JWT string
  private readonly USER_KEY  = 'pharma_user';   // stores user object as JSON

  /**
   * Reactive signal holding the currently logged-in user.
   * Components read this to show/hide UI elements.
   * null = nobody is logged in.
   * When this changes, Angular automatically updates all components that use it.
   */
  currentUser = signal<AuthUser | null>(null);

  constructor(
    private http: HttpClient,       // makes HTTP requests to the backend
    private router: Router,         // navigates between pages
    private cartService: CartService // needed to clear cart on logout
  ) {
    // On app startup, restore the user from localStorage so the session
    // survives page refreshes. Without this, every refresh would log you out.
    this.currentUser.set(this.loadUser());
  }

  /**
   * Registers a new user account.
   * UI effect: shows success message on register page, then redirects to login.
   *
   * @param request — contains name, email, password, roles, and optional adminToken
   * @returns Observable<string> — the backend returns a plain text success message
   */
  signup(request: SignupRequest): Observable<string> {
    return this.http.post(`${this.API}/signup`, request, { responseType: 'text' });
  }

  /**
   * Logs in a user and stores their session.
   * UI effect: after success, redirects admin → /admin/dashboard, customer → /home.
   *
   * The `.pipe(tap(...))` runs a side-effect AFTER the HTTP call succeeds:
   *   - Saves the JWT token to localStorage (so the interceptor can attach it to future requests)
   *   - Saves the user object to localStorage (for session persistence)
   *   - Updates the `currentUser` signal (triggers navbar re-render)
   *
   * @param request — email and password
   * @returns Observable<LoginResponse> — contains token, name, id, roles
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/login`, request).pipe(
      tap(response => {
        // Build the user object from the login response
        const user: AuthUser = {
          token: response.token,
          name: response.name,
          email: request.email,  // email comes from the request, not the response
          id: response.id,
          roles: response.roles
        };
        // Persist to localStorage so session survives page refresh
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        // Update the reactive signal — this triggers navbar to show logged-in links
        this.currentUser.set(user);
      })
    );
  }

  /**
   * Logs out the current user.
   * UI effect: clears cart, resets navbar to guest state, redirects to /login.
   *
   * Order matters here:
   *   1. Remove token first (so no more authenticated requests can be made)
   *   2. Clear user signal (navbar immediately switches to guest mode)
   *   3. Clear cart (user's cart items are private, shouldn't persist)
   *   4. Navigate to login page
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);       // triggers navbar re-render to guest state
    this.cartService.clearCart();     // empties the shopping cart
    this.router.navigate(['/login']); // sends user to login page
  }

  /**
   * Returns the raw JWT token string from localStorage.
   * Used by the auth interceptor to attach "Authorization: Bearer <token>"
   * to every outgoing HTTP request automatically.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Returns true if a JWT token exists in localStorage.
   * Used by route guards to decide if a page is accessible.
   * Note: this only checks existence, not expiry. The backend validates expiry.
   */
  isLoggedIn(): boolean {
    return !!this.getToken(); // !! converts string|null to boolean
  }

  /**
   * Returns true if the current user has the ADMIN role.
   * UI effect: shows/hides "Admin Panel" link in navbar.
   * Used by adminGuard to protect /admin/* routes.
   */
  isAdmin(): boolean {
    return this.currentUser()?.roles?.includes('ADMIN') ?? false;
  }

  /**
   * Returns true if the current user has the CUSTOMER role.
   * UI effect: shows/hides "My Orders", "Prescriptions" links in navbar.
   */
  isCustomer(): boolean {
    return this.currentUser()?.roles?.includes('CUSTOMER') ?? false;
  }

  /**
   * Returns the current user's numeric ID.
   * Used when placing orders, uploading prescriptions, fetching order history.
   * Falls back to 1 if no user is logged in (should not happen in practice).
   */
  getUserId(): number {
    return this.currentUser()?.id ?? 1;
  }

  /**
   * Returns the current user's email address.
   * Used as the X-Auth-User header when placing orders and uploading prescriptions.
   */
  getUserEmail(): string {
    return this.currentUser()?.email ?? '';
  }

  /**
   * Reads and parses the stored user object from localStorage.
   * Called once on app startup in the constructor.
   * Returns null if nothing is stored or if the JSON is corrupt.
   */
  private loadUser(): AuthUser | null {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      // If localStorage has corrupt data, treat as logged out
      return null;
    }
  }
}
