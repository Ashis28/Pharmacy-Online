/**
 * DashboardComponent — The admin dashboard overview page (/admin/dashboard).
 *
 * WHAT IT SHOWS:
 *   4 stat cards:
 *     📦 Total Orders       — total number of orders ever placed
 *     📋 Pending Prescriptions — prescriptions waiting for review
 *     ⚠️ Low Stock Items    — medicines with stock ≤ 10 units
 *     💰 Monthly Revenue    — sum of paid orders this calendar month
 *
 *   Quick Action buttons: Add Medicine, Review Prescriptions, Update Orders, Send Notification
 *   Recent Orders table: last 5 orders with status badges
 *   Low Stock Alert list: medicines that need restocking
 *
 * HOW DATA IS LOADED (forkJoin):
 *   forkJoin fires 3 API calls SIMULTANEOUSLY and waits for ALL to complete.
 *   This is much faster than calling them one by one (sequential would take 3x longer).
 *   If any call fails, catchError returns an empty array so the dashboard still loads.
 *
 *   The 3 parallel calls:
 *   1. getAllOrders()          → to count total orders and calculate revenue
 *   2. getPendingPrescriptions() → to count pending prescriptions
 *   3. getAllMedicines()       → to count and list low-stock medicines
 *
 * WHY CALCULATE LOW STOCK ON FRONTEND?
 *   We already fetch all medicines for the dashboard. Counting low-stock items
 *   client-side (filter where stockQuantity ≤ 10) avoids an extra API call.
 *   This is consistent with the medicines table which uses the same threshold.
 *
 * MONTHLY REVENUE CALCULATION:
 *   We filter orders where:
 *   - status is PAID, PACKED, SHIPPED, or DELIVERED (i.e. payment was received)
 *   - createdAt is within the current calendar month
 *   Then sum their totalAmount values.
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AdminService } from '../../../services/admin.service';
import { MedicineService } from '../../../services/medicine.service';
import { OrderService } from '../../../services/order.service';
import { DashboardStats } from '../../../models/admin.models';
import { Order } from '../../../models/order.models';
import { Medicine } from '../../../models/medicine.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  adminService    = inject(AdminService);
  medicineService = inject(MedicineService);
  orderService    = inject(OrderService);

  /** The 4 stat card values — null while loading */
  stats: DashboardStats | null = null;

  /** Last 5 orders, sorted newest first — shown in the Recent Orders table */
  recentOrders: Order[] = [];

  /** Medicines with stock ≤ 10 — shown in the Low Stock Alert widget */
  lowStockMedicines: Medicine[] = [];

  /** Controls the skeleton loading state */
  loading = false;

  /** Error message if dashboard data fails to load */
  error = '';

  /**
   * Lifecycle hook — loads all dashboard data when the page opens.
   */
  ngOnInit() {
    this.loadDashboard();
  }

  /**
   * Loads all dashboard data using 3 parallel API calls.
   *
   * forkJoin fires all 3 calls at the same time and waits for all to finish.
   * catchError on each call returns an empty array if that call fails,
   * so the dashboard still renders even if one service is down.
   *
   * UI effect: skeleton cards → populated stat cards + recent orders table + low stock list
   */
  loadDashboard() {
    this.loading = true;
    this.error = '';

    forkJoin({
      // Call 1: all orders (for total count, revenue, and recent orders table)
      orders: this.orderService.getAllOrders().pipe(catchError(() => of([]))),

      // Call 2: pending prescriptions (for the pending count stat card)
      prescriptions: this.adminService.getPendingPrescriptions().pipe(catchError(() => of([]))),

      // Call 3: all medicines (for low stock count and list)
      medicines: this.medicineService.getAllMedicines().pipe(catchError((err) => {
        console.error('Medicines fetch failed:', err);
        return of([]);
      }))
    }).subscribe({
      next: ({ orders, prescriptions, medicines }) => {

        // Count medicines with stock ≤ 10 (same threshold as the medicines table)
        const lowStockCount = (medicines as any[]).filter(
          (m: any) => m.stockQuantity !== null && m.stockQuantity !== undefined && m.stockQuantity <= 10
        ).length;

        // Build the low stock list for the alert widget
        this.lowStockMedicines = (medicines as Medicine[]).filter(
          m => m.stockQuantity !== null && m.stockQuantity !== undefined && m.stockQuantity <= 10
        );

        // Calculate monthly revenue: sum of paid orders created this month
        const now = new Date();
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1); // 1st of current month
        const monthlyRevenue = (orders as Order[])
          .filter(o => {
            const paidStatuses = ['PAID', 'PACKED', 'SHIPPED', 'DELIVERED'];
            const orderDate = o.createdAt ? new Date(o.createdAt) : new Date(0);
            // Only count orders that were paid AND created this month
            return paidStatuses.includes(o.status || '') && orderDate >= monthStart;
          })
          .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

        // Populate the 4 stat cards
        this.stats = {
          totalOrders:          (orders as Order[]).length,
          pendingPrescriptions: (prescriptions as any[]).length,
          lowStockCount,
          monthlyRevenue
        };

        // Recent orders: last 5, sorted newest first
        this.recentOrders = (orders as Order[])
          .sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime())
          .slice(0, 5);

        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load dashboard data.';
      }
    });
  }

  /**
   * Returns the CSS class for an order status badge in the recent orders table.
   * UI effect: each status gets a different color badge.
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
