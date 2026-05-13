/**
 * CartComponent — The shopping cart page (/cart).
 *
 * WHAT IT SHOWS:
 *   Empty cart: "Your cart is empty" message with a "Browse Medicines" link.
 *
 *   Cart with items:
 *     Left side: List of cart items with − / + quantity buttons and 🗑️ remove button.
 *     Right side: Order summary with item totals, delivery address input, and "Place Order" button.
 *
 * HOW QUANTITY CONTROLS WORK:
 *   The − button calls cart.updateQuantity(id, currentQty - 1).
 *   If quantity reaches 0, CartService automatically removes the item.
 *   The + button calls cart.updateQuantity(id, currentQty + 1).
 *   CartService caps the quantity at medicine.stockQuantity (stock limit).
 *   The + button is disabled in the HTML when at max stock.
 *
 * HOW PLACE ORDER WORKS:
 *   1. Validates: user is logged in, address is entered, cart is not empty
 *   2. Builds the order request: customerId, deliveryAddress, items[]
 *   3. Calls orderService.placeOrder() → backend creates the order
 *   4. On success: clears the cart, navigates to /payment/:orderId
 *   5. On error: shows the error message from the backend
 *
 * WHY NAVIGATE TO PAYMENT?
 *   The order is created first (status = PENDING), then the user pays.
 *   This way, if payment fails, the order still exists and can be retried.
 */

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cart.html',
  styleUrls: ['./cart.scss']
})
export class CartComponent {
  cart         = inject(CartService);
  auth         = inject(AuthService);
  orderService = inject(OrderService);
  router       = inject(Router);

  /** Two-way bound to the delivery address textarea in the template */
  deliveryAddress = '';

  /** Shows the loading spinner on the "Place Order" button while the API call is in progress */
  loading = false;

  /** Shows validation or API error messages above the Place Order button */
  error = '';

  /**
   * Places the order when the customer clicks "Place Order".
   *
   * Validation steps (in order):
   *   1. Must be logged in (redirects to /login if not)
   *   2. Delivery address must not be empty
   *   3. Cart must not be empty
   *
   * On success:
   *   - Cart is cleared (items disappear from navbar badge)
   *   - User is redirected to /payment/:orderId to complete payment
   *
   * On error:
   *   - Error message from backend is shown (e.g. "Insufficient stock for Vitamin D3")
   *   - loading spinner stops
   */
  placeOrder() {
    // Guard: must be logged in
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    // Guard: delivery address is required
    if (!this.deliveryAddress.trim()) {
      this.error = 'Please enter a delivery address.';
      return;
    }

    // Guard: cart must have items
    if (this.cart.cartItems().length === 0) {
      this.error = 'Your cart is empty.';
      return;
    }

    this.loading = true;
    this.error = '';

    const user       = this.auth.currentUser();
    const customerId = this.auth.getUserId();

    // Build the order request payload
    const request = {
      customerId,
      deliveryAddress: this.deliveryAddress,
      // Map each cart item to { medicineId, quantity }
      items: this.cart.cartItems().map(i => ({
        medicineId: i.medicine.id!,
        quantity: i.quantity
      }))
    };

    // Send to backend — order-service creates the order and decrements stock
    this.orderService.placeOrder(request, user?.email ?? '').subscribe({
      next: (order) => {
        this.loading = false;
        this.cart.clearCart();                          // empty the cart
        this.router.navigate(['/payment', order.id]);  // go to payment page
      },
      error: (err) => {
        this.loading = false;
        // Show the backend's error message (e.g. insufficient stock)
        this.error = err.error?.message || 'Failed to place order. Please try again.';
      }
    });
  }
}
