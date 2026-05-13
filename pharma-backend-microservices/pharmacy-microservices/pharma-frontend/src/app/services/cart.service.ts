/**
 * CartService — Manages the shopping cart entirely in memory (no backend needed).
 *
 * WHY IN MEMORY?
 *   The cart is temporary — it only exists while the user is shopping.
 *   Once they place an order, the cart is cleared. There's no need to
 *   persist it to a database. Using Angular signals keeps it reactive.
 *
 * HOW SIGNALS WORK HERE:
 *   `items` is a private signal (writable). Components can't modify it directly.
 *   `cartItems` is a readonly view of `items` — components read this.
 *   `cartCount` and `cartTotal` are computed signals — they automatically
 *   recalculate whenever `items` changes. No manual refresh needed.
 *
 * STOCK ENFORCEMENT:
 *   The cart enforces stock limits. You can never add more items than
 *   what's available in the backend's stockQuantity field.
 */

import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Medicine } from '../models/medicine.models';
import { CartItem } from '../models/order.models';

@Injectable({ providedIn: 'root' })
export class CartService {
  private router = inject(Router); // used to redirect to login if not authenticated

  /**
   * The internal cart state — a list of {medicine, quantity} pairs.
   * Private so only this service can modify it.
   * Components read via `cartItems` (readonly).
   */
  private items = signal<CartItem[]>([]);

  /**
   * Public readonly view of the cart items.
   * Components use this in templates: `@for (item of cart.cartItems(); ...)`
   */
  cartItems = this.items.asReadonly();

  /**
   * Computed signal: total number of individual items in the cart.
   * UI effect: shows the red badge number on the 🛒 cart icon in the navbar.
   * Automatically updates whenever items change.
   * Example: 2 Paracetamol + 3 Vitamin D = cartCount of 5
   */
  cartCount = computed(() => this.items().reduce((sum, i) => sum + i.quantity, 0));

  /**
   * Computed signal: total price of all items in the cart.
   * UI effect: shows the "Total ₹X.XX" in the cart page and order summary.
   * Automatically recalculates when any item quantity or price changes.
   */
  cartTotal = computed(() =>
    this.items().reduce((sum, i) => sum + i.medicine.price * i.quantity, 0)
  );

  /**
   * Adds a medicine to the cart, or increments its quantity if already present.
   *
   * UI effect:
   *   - If not logged in: redirects to /login and returns false
   *   - If added: cart badge count increases, cart total updates
   *   - If already at max stock: silently ignores (no error shown)
   *
   * WHY CHECK LOGIN HERE?
   *   We want to prevent anonymous users from adding items. The token check
   *   is a quick client-side gate. The real auth check happens on the backend
   *   when the order is actually placed.
   *
   * @returns true if item was added, false if user needs to login first
   */
  addToCart(medicine: Medicine): boolean {
    // Quick client-side login check — token in localStorage means logged in
    const token = localStorage.getItem('pharma_token');
    if (!token) {
      // Redirect to login with a return URL so user comes back after logging in
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: '/medicines', message: 'Please login to add items to cart' }
      });
      return false;
    }

    const current = this.items();
    const existing = current.find(i => i.medicine.id === medicine.id);

    if (existing) {
      // Medicine already in cart — increment quantity, but respect stock limit
      if (existing.quantity >= (medicine.stockQuantity ?? Infinity)) {
        return true; // Already at max stock — silently ignore the click
      }
      // Create a new array with the updated quantity (signals require immutable updates)
      this.items.set(current.map(i =>
        i.medicine.id === medicine.id ? { ...i, quantity: i.quantity + 1 } : i
      ));
    } else {
      // New medicine — add to cart with quantity 1
      this.items.set([...current, { medicine, quantity: 1 }]);
    }
    return true;
  }

  /**
   * Removes a medicine completely from the cart.
   * UI effect: item disappears from cart list, count and total update.
   * Triggered by the 🗑️ delete button on each cart item.
   */
  removeFromCart(medicineId: number): void {
    this.items.set(this.items().filter(i => i.medicine.id !== medicineId));
  }

  /**
   * Sets the quantity of a specific cart item.
   * UI effect: quantity number updates, total price recalculates.
   * Triggered by the − and + buttons on each cart item.
   *
   * STOCK CAP: quantity is capped at medicine.stockQuantity.
   * This prevents ordering more than what's available.
   * The + button is also disabled in the HTML when at max stock.
   *
   * @param medicineId — which item to update
   * @param quantity   — the new desired quantity (0 or negative = remove)
   */
  updateQuantity(medicineId: number, quantity: number): void {
    if (quantity <= 0) {
      // Quantity of 0 or less means "remove from cart"
      this.removeFromCart(medicineId);
      return;
    }
    // Cap at available stock — never allow ordering more than what's in stock
    this.items.set(this.items().map(i => {
      if (i.medicine.id !== medicineId) return i; // leave other items unchanged
      const maxQty = i.medicine.stockQuantity ?? quantity; // fallback if stockQuantity is null
      const capped = Math.min(quantity, maxQty);           // enforce the stock limit
      return { ...i, quantity: capped };
    }));
  }

  /**
   * Empties the entire cart.
   * UI effect: cart page shows "Your cart is empty", badge shows 0.
   * Called after: successful order placement, user logout.
   */
  clearCart(): void {
    this.items.set([]);
  }
}
