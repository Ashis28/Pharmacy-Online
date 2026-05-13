/**
 * NavbarComponent — The top navigation bar shown on all non-admin pages.
 *
 * WHAT IT SHOWS:
 *   Guest (not logged in):
 *     → Logo | Login button | Register button
 *
 *   Logged-in Customer:
 *     → Logo | Home | Medicines | My Orders | Prescriptions | 🔔 Notifications
 *     → 🛒 Cart icon with item count badge | Username | Logout button
 *
 *   Logged-in Admin:
 *     → Logo | Home | Medicines | Admin Panel link
 *     → 🛒 Cart icon | Username | Logout button
 *
 * HOW IT KNOWS WHO IS LOGGED IN:
 *   It reads `auth.currentUser()` — a reactive signal from AuthService.
 *   When the user logs in or out, the signal changes and the navbar
 *   automatically re-renders to show the correct links. No manual refresh needed.
 *
 * CART BADGE:
 *   `cart.cartCount()` is a computed signal that sums all item quantities.
 *   The red badge on the 🛒 icon shows this number and updates in real-time
 *   as items are added or removed.
 *
 * MOBILE HAMBURGER MENU:
 *   On small screens, the links collapse into a hamburger (☰) menu.
 *   `menuOpen` tracks whether it's open or closed.
 *   `toggleMenu()` flips it. `closeMenu()` closes it (called after clicking a link).
 */

import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class NavbarComponent {
  // Inject services — auth tells us who is logged in, cart tells us item count
  auth = inject(AuthService);
  cart = inject(CartService);

  /**
   * Tracks whether the mobile hamburger menu is open.
   * UI effect: adds/removes the 'open' CSS class on the nav-links div,
   * which shows/hides the menu on mobile screens.
   */
  menuOpen = false;

  /**
   * Toggles the mobile menu open/closed.
   * Called when the hamburger button (☰) is clicked.
   * UI effect: menu slides in or out on mobile.
   */
  toggleMenu() { this.menuOpen = !this.menuOpen; }

  /**
   * Closes the mobile menu.
   * Called after clicking any navigation link so the menu closes automatically.
   * UI effect: menu slides out after navigation.
   */
  closeMenu() { this.menuOpen = false; }

  /**
   * Logs out the current user.
   * Calls AuthService.logout() which:
   *   1. Clears localStorage (token + user data)
   *   2. Resets the currentUser signal (navbar switches to guest mode)
   *   3. Clears the cart
   *   4. Redirects to /login
   * Also closes the mobile menu.
   */
  logout() {
    this.auth.logout();
    this.closeMenu();
  }
}
