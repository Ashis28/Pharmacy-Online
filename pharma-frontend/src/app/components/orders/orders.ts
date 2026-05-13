/**
 * OrdersComponent — The "My Orders" page (/orders).
 *
 * WHAT IT SHOWS:
 *   A list of all orders placed by the logged-in customer, sorted newest first.
 *   Each order card shows: Order ID, status badge, total amount, date, items.
 *   Clicking an order expands it to show the item details.
 *   Orders with PENDING status show a "Pay Now" button.
 *
 * ORDER STATUS FLOW:
 *   PENDING → PAID → PACKED → SHIPPED → DELIVERED
 *   (or CANCELLED at any point by admin)
 *
 *   Each status has a different color badge:
 *   PENDING = orange, PAID = blue, PACKED = purple,
 *   SHIPPED = cyan, DELIVERED = green, CANCELLED = red
 *
 * PROGRESS TRACKER:
 *   Each expanded order shows a visual step tracker (like a delivery progress bar).
 *   isStepActive() determines which steps are highlighted based on current status.
 *
 * EXPAND/COLLAPSE:
 *   `expandedOrder` stores the ID of the currently expanded order (or null).
 *   toggleExpand() opens an order if it's closed, or closes it if it's open.
 *   Only one order can be expanded at a time.
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';
import { AuthService } from '../../services/auth.service';
import { Order } from '../../models/order.models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orders.html',
  styleUrls: ['./orders.css']
})
export class OrdersComponent implements OnInit {
  orderService   = inject(OrderService);
  paymentService = inject(PaymentService);
  auth           = inject(AuthService);

  /** All orders for the current customer, sorted newest first */
  orders: Order[] = [];

  /** Controls the loading spinner while fetching orders */
  loading = false;

  /**
   * The ID of the currently expanded order card.
   * null = no order is expanded.
   * When an order is expanded, its items and progress tracker are shown.
   */
  expandedOrder: number | null = null;

  /**
   * Lifecycle hook — fetches orders when the page loads.
   */
  ngOnInit() {
    this.loadOrders();
  }

  /**
   * Fetches all orders for the current customer from the backend.
   * UI effect: order cards appear on the page, sorted newest first.
   */
  loadOrders() {
    this.loading = true;
    const customerId = this.auth.getUserId();

    this.orderService.getOrdersByCustomer(customerId).subscribe({
      next: (orders) => {
        // Sort by creation date, newest first
        this.orders = orders.sort((a, b) =>
          new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()
        );
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Expands or collapses an order card to show/hide item details.
   * UI effect: clicking an order card reveals the items list and progress tracker.
   * Clicking again collapses it.
   *
   * @param orderId — the ID of the order to toggle
   */
  toggleExpand(orderId: number) {
    // If this order is already expanded, collapse it; otherwise expand it
    this.expandedOrder = this.expandedOrder === orderId ? null : orderId;
  }

  /**
   * Returns the CSS class for a status badge.
   * UI effect: each status gets a different color:
   *   PENDING = orange, PAID = blue, PACKED = purple,
   *   SHIPPED = cyan, DELIVERED = green, CANCELLED = red
   */
  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'status-pending',
      PAID:      'status-paid',
      PACKED:    'status-packed',
      SHIPPED:   'status-shipped',
      DELIVERED: 'status-delivered',
      CANCELLED: 'status-cancelled'
    };
    return map[status] || 'status-pending';
  }

  /**
   * Returns the emoji icon for a status.
   * UI effect: shown next to the status text in the order card header.
   */
  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      PENDING: '○', PAID: '●', PACKED: '◉',
      SHIPPED: '→', DELIVERED: '✓', CANCELLED: '✕'
    };
    return map[status] || '○';
  }

  /**
   * Returns true if the order can still be paid (status is PENDING).
   * UI effect: shows/hides the "Pay Now" button on the order card.
   * Once paid, the button disappears.
   */
  canPay(order: Order): boolean {
    return order.status === 'PENDING';
  }

  /**
   * Determines if a step in the progress tracker should be highlighted.
   * UI effect: steps up to and including the current status are shown as active (colored).
   * Steps after the current status are shown as inactive (grey).
   *
   * Example: if status is SHIPPED, then PENDING, PAID, PACKED, SHIPPED are active.
   * DELIVERED is inactive (not yet reached).
   *
   * @param currentStatus — the order's current status
   * @param step          — the step to check (e.g. 'PAID', 'SHIPPED')
   */
  isStepActive(currentStatus: string, step: string): boolean {
    const steps = ['PENDING', 'PAID', 'PACKED', 'SHIPPED', 'DELIVERED'];
    return steps.indexOf(currentStatus) >= steps.indexOf(step);
  }
}
