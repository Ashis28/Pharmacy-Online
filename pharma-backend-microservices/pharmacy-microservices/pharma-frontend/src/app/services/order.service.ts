/**
 * OrderService — All HTTP calls related to orders.
 *
 * ORDER LIFECYCLE:
 *   PENDING → (payment) → PAID → (admin) → PACKED → SHIPPED → DELIVERED
 *   Any status can be set to CANCELLED by admin.
 *
 * WHO CALLS WHAT:
 *   - placeOrder()          → CartComponent (when customer clicks "Place Order")
 *   - getOrdersByCustomer() → OrdersComponent (customer's order history page)
 *   - getAllOrders()         → AdminOrdersComponent (admin sees all orders)
 *   - markOrderAsPaid()     → PaymentComponent (after successful payment)
 *   - updateOrderStatus()   → AdminOrdersComponent (admin changes status)
 *
 * X-Auth-User HEADER:
 *   The order-service controller reads the customer's email from this header.
 *   The gateway normally injects it from the JWT, but we also send it
 *   explicitly here for reliability.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Order, PlaceOrderRequest, UpdateOrderStatusRequest, RevenueResponse } from '../models/order.models';

@Injectable({ providedIn: 'root' })
export class OrderService {
  // Base URL for order-service endpoints
  private readonly API = `${environment.apiUrl}/api/orders`;

  constructor(private http: HttpClient) {}

  /**
   * Places a new order for the logged-in customer.
   * UI effect: cart is cleared, user is redirected to /payment/:orderId.
   *
   * The backend:
   *   1. Validates stock for each item
   *   2. Saves the order with PENDING status
   *   3. Decrements stock in catalog-service
   *   4. Publishes ORDER_PLACED event to RabbitMQ (triggers notification)
   *
   * @param request — customerId, deliveryAddress, items (medicineId + quantity)
   * @param customerEmail — sent as X-Auth-User header for the backend to record
   */
  placeOrder(request: PlaceOrderRequest, customerEmail: string): Observable<Order> {
    return this.http.post<Order>(this.API, request, {
      headers: { 'X-Auth-User': customerEmail } // backend records this as the order owner
    });
  }

  /**
   * Fetches a single order by its ID.
   * UI effect: populates the payment page with order details (items, total, address).
   * Called on: PaymentComponent.ngOnInit()
   */
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.API}/${id}`);
  }

  /**
   * Fetches all orders placed by a specific customer.
   * UI effect: populates the "My Orders" page with the customer's order history.
   * Orders are sorted newest-first in the component.
   */
  getOrdersByCustomer(customerId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API}/customer/${customerId}`);
  }

  /**
   * Fetches ALL orders in the system (admin only).
   * UI effect: populates the admin orders management table.
   * Requires ADMIN role — backend enforces with @PreAuthorize.
   */
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.API);
  }

  /**
   * Updates an order's status (admin only).
   * UI effect: the status badge on the order row changes color and text.
   * Valid statuses: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED
   */
  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.API}/${id}/status`, { status });
  }

  /**
   * Marks an order as PAID after successful payment.
   * UI effect: order status changes from PENDING to PAID on the orders page.
   * Called by PaymentComponent after the payment API returns SUCCESS.
   */
  markOrderAsPaid(id: number): Observable<Order> {
    return this.http.put<Order>(`${this.API}/${id}/pay`, {});
  }

  /**
   * Gets the total count of all orders in the system.
   * UI effect: shows "Total Orders: X" on the admin dashboard stat card.
   */
  getOrderCount(): Observable<number> {
    return this.http.get<number>(`${this.API}/count`);
  }

  /**
   * Gets total revenue from DELIVERED orders within a date range.
   * UI effect: shows "Monthly Revenue: ₹X" on the admin dashboard.
   *
   * @param from — start date in ISO format (yyyy-MM-dd)
   * @param to   — end date in ISO format (yyyy-MM-dd)
   */
  getRevenue(from: string, to: string): Observable<RevenueResponse> {
    return this.http.get<RevenueResponse>(`${this.API}/revenue?from=${from}&to=${to}`);
  }
}
