/**
 * NotificationsComponent — The customer's notifications page (/notifications).
 *
 * WHAT IT SHOWS:
 *   A timeline of notification cards derived from the customer's order history.
 *   Each card shows an icon, title, message, and timestamp.
 *   Sorted newest first.
 *
 * HOW NOTIFICATIONS ARE GENERATED:
 *   This app doesn't have a dedicated notifications database table.
 *   Instead, we fetch the customer's orders and GENERATE notification items
 *   from the order statuses. This is a client-side simulation.
 *
 *   For each order:
 *   - Always: "Order Placed" notification
 *   - If PAID/PACKED/SHIPPED/DELIVERED: "Payment Confirmed" notification
 *   - If SHIPPED/DELIVERED: "Order Shipped" notification
 *   - If DELIVERED: "Order Delivered" notification
 *   - If CANCELLED: "Order Cancelled" notification
 *
 * WHY THIS APPROACH?
 *   In a real app, notifications would be stored in a database and pushed
 *   via WebSockets or Server-Sent Events. For this demo, deriving them
 *   from order history gives a realistic notification experience without
 *   the complexity of a real-time notification system.
 *
 * NOTIFICATION TYPES AND COLORS:
 *   ORDER_PLACED    → default (grey)
 *   ORDER_PAID      → success (green)
 *   ORDER_SHIPPED   → info (blue)
 *   ORDER_DELIVERED → success (green)
 *   ORDER_CANCELLED → error (red)
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { Order } from '../../models/order.models';

/** Represents a single notification item in the timeline */
interface NotificationItem {
  icon: string;      // emoji icon (📦, ✅, 🚚, 🎉, ❌)
  title: string;     // notification title
  message: string;   // full notification message
  time: string;      // ISO timestamp (used for sorting and display)
  type: string;      // notification type (ORDER_PLACED, ORDER_PAID, etc.)
  orderId?: number;  // optional link to the related order
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.scss']
})
export class NotificationsComponent implements OnInit {
  orderService = inject(OrderService);
  auth         = inject(AuthService);

  /** Generated notification items, sorted newest first */
  notifications: NotificationItem[] = [];

  /** Controls the loading spinner while fetching orders */
  loading = false;

  /**
   * Lifecycle hook — loads notifications when the page opens.
   */
  ngOnInit() {
    this.loadNotifications();
  }

  /**
   * Fetches the customer's orders and generates notification items from them.
   * UI effect: notification cards appear in the timeline.
   */
  loadNotifications() {
    this.loading = true;
    const customerId = this.auth.getUserId();

    this.orderService.getOrdersByCustomer(customerId).subscribe({
      next: (orders) => {
        this.notifications = this.buildNotifications(orders);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Converts a list of orders into a list of notification items.
   * Each order can generate multiple notifications based on its status.
   * The result is sorted newest first.
   *
   * @param orders — the customer's order history
   * @returns sorted array of NotificationItem objects
   */
  private buildNotifications(orders: Order[]): NotificationItem[] {
    const items: NotificationItem[] = [];

    orders.forEach(order => {
      const date = order.createdAt || new Date().toISOString();

      // Every order generates an "Order Placed" notification
      items.push({
        icon: '📦',
        title: 'Order Placed',
        message: `Your order #${order.id} for ₹${order.totalAmount} has been placed successfully.`,
        time: date,
        type: 'ORDER_PLACED',
        orderId: order.id
      });

      // Payment confirmation — shown for any status beyond PENDING
      if (['PAID', 'PACKED', 'SHIPPED', 'DELIVERED'].includes(order.status || '')) {
        items.push({
          icon: '✅',
          title: 'Payment Confirmed',
          message: `Payment for order #${order.id} (₹${order.totalAmount}) was successful.`,
          time: date,
          type: 'ORDER_PAID',
          orderId: order.id
        });
      }

      // Shipping notification — shown when order is shipped or delivered
      if (['SHIPPED', 'DELIVERED'].includes(order.status || '')) {
        items.push({
          icon: '🚚',
          title: 'Order Shipped',
          message: `Your order #${order.id} has been shipped and is on its way!`,
          time: date,
          type: 'ORDER_SHIPPED',
          orderId: order.id
        });
      }

      // Delivery notification — shown only when delivered
      if (order.status === 'DELIVERED') {
        items.push({
          icon: '🎉',
          title: 'Order Delivered',
          message: `Your order #${order.id} has been delivered. Enjoy your medicines!`,
          time: date,
          type: 'ORDER_DELIVERED',
          orderId: order.id
        });
      }

      // Cancellation notification
      if (order.status === 'CANCELLED') {
        items.push({
          icon: '❌',
          title: 'Order Cancelled',
          message: `Your order #${order.id} has been cancelled.`,
          time: date,
          type: 'ORDER_CANCELLED',
          orderId: order.id
        });
      }
    });

    // Sort all notifications newest first
    return items.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime());
  }

  /**
   * Returns the CSS class for a notification card based on its type.
   * UI effect: different notification types get different border/background colors.
   *   success (green) → DELIVERED, PAID
   *   error (red)     → CANCELLED
   *   info (blue)     → SHIPPED
   *   default (grey)  → PLACED
   */
  getTypeClass(type: string): string {
    if (type.includes('DELIVERED') || type.includes('PAID')) return 'notif-success';
    if (type.includes('CANCELLED'))                          return 'notif-error';
    if (type.includes('SHIPPED'))                            return 'notif-info';
    return 'notif-default';
  }
}
