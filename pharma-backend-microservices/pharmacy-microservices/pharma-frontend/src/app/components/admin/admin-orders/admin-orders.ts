/**
 * AdminOrdersComponent — Order management page (/admin/orders).
 *
 * WHAT IT SHOWS:
 *   A table of ALL orders in the system (all customers), sorted newest first.
 *   Status filter dropdown to show only orders of a specific status.
 *   Each row shows: Order ID, Customer email, Amount, Status badge, Date, Actions.
 *   Clicking a row expands it to show the ordered items.
 *   A status dropdown on each row lets admin change the order status.
 *
 * STATUS FILTER:
 *   `filterStatus` is bound to a <select> dropdown.
 *   When changed, applyFilter() re-filters the `orders` array into `filtered`.
 *   The template renders `filtered` (not `orders` directly).
 *   'ALL' shows every order; any other value shows only matching orders.
 *
 * STATUS UPDATE:
 *   Admin selects a new status from the dropdown on a row.
 *   updateStatus() calls adminService.updateOrderStatus() → backend updates the DB.
 *   On success, the order's status is updated in-place (no full reload needed).
 *   A success banner appears for 3 seconds.
 *
 * EXPAND/COLLAPSE:
 *   Same pattern as OrdersComponent — `expandedOrder` stores the expanded order ID.
 *   toggleExpand() opens/closes the items detail view.
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { Order, OrderStatus } from '../../../models/order.models';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-orders.html',
  styleUrls: ['./admin-orders.scss']
})
export class AdminOrdersComponent implements OnInit {
  adminService = inject(AdminService);

  /** All orders in the system */
  orders: Order[] = [];

  /**
   * The filtered subset of orders shown in the table.
   * Updated by applyFilter() whenever filterStatus changes.
   */
  filtered: Order[] = [];

  /** Controls the loading spinner */
  loading = false;

  /**
   * The currently selected status filter.
   * 'ALL' = show all orders.
   * Any other value = show only orders with that status.
   * Bound to the filter <select> dropdown in the template.
   */
  filterStatus = 'ALL';

  /**
   * The ID of the currently expanded order row.
   * null = no row is expanded.
   */
  expandedOrder: number | null = null;

  /** Success banner message (auto-clears after 3 seconds) */
  success = '';

  /** Error message shown if status update fails */
  error = '';

  /** All valid order statuses — used to populate the status update dropdown */
  statusOptions: OrderStatus[] = ['PENDING', 'PAID', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  /**
   * Lifecycle hook — loads all orders when the page opens.
   */
  ngOnInit() {
    this.loadOrders();
  }

  /**
   * Fetches all orders from the backend and applies the current filter.
   * UI effect: the orders table populates, sorted newest first.
   */
  loadOrders() {
    this.loading = true;
    this.adminService.getAllOrders().subscribe({
      next: (data) => {
        // Sort newest first
        this.orders = data.sort((a, b) =>
          new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()
        );
        this.applyFilter(); // apply the current filter to the loaded data
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Filters the orders array based on the selected status.
   * UI effect: the table shows only orders matching the selected status.
   * Called whenever filterStatus changes (via (change) event in template).
   */
  applyFilter() {
    this.filtered = this.filterStatus === 'ALL'
      ? this.orders                                              // show all
      : this.orders.filter(o => o.status === this.filterStatus); // show matching only
  }

  /**
   * Expands or collapses an order row to show/hide item details.
   * UI effect: clicking a row reveals the list of ordered medicines.
   */
  toggleExpand(id: number) {
    this.expandedOrder = this.expandedOrder === id ? null : id;
  }

  /**
   * Updates an order's status.
   * UI effect: the status badge on the row changes immediately (optimistic update).
   * A success banner appears for 3 seconds.
   *
   * @param order  — the order object to update (mutated in-place on success)
   * @param status — the new status string
   */
  updateStatus(order: Order, status: string) {
    this.adminService.updateOrderStatus(order.id!, status).subscribe({
      next: (updated) => {
        // Update the order's status in-place — no need to reload the whole list
        order.status = updated.status;
        this.success = `Order #${order.id} updated to ${status}`;
        setTimeout(() => this.success = '', 3000);
      },
      error: () => { this.error = 'Failed to update status.'; }
    });
  }

  /**
   * Returns the CSS class for a status badge.
   * UI effect: each status gets a different color.
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
    return map[status] || '';
  }
}
