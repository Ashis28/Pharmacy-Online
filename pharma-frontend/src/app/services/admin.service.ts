/**
 * AdminService — HTTP calls for admin-specific operations.
 *
 * WHY A SEPARATE ADMIN SERVICE?
 *   Admin operations (managing medicines, orders, prescriptions) go through
 *   different endpoints than customer operations. Keeping them separate makes
 *   the code easier to understand and maintain.
 *
 * NOTE ON ROUTING:
 *   Some methods call /api/admin/* (admin-service microservice).
 *   Others call /api/catalog/* and /api/orders/* directly.
 *   This is because the admin-service acts as an aggregator for some operations,
 *   while for others it's simpler to call the source service directly.
 *
 * ALL THESE ENDPOINTS REQUIRE ADMIN ROLE.
 * The JWT token (with ADMIN role) is automatically attached by the auth interceptor.
 * The backend enforces authorization with @PreAuthorize("hasRole('ADMIN')").
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardStats, NotificationRequest } from '../models/admin.models';
import { Medicine } from '../models/medicine.models';
import { Order } from '../models/order.models';
import { Prescription } from '../models/medicine.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly API        = `${environment.apiUrl}/api/admin`;        // admin-service
  private readonly NOTIFY_API = `${environment.apiUrl}/api/notifications`; // notification-service

  constructor(private http: HttpClient) {}

  /**
   * Fetches aggregated dashboard statistics from admin-service.
   * UI effect: populates the 4 stat cards on the admin dashboard
   *   (Total Orders, Pending Prescriptions, Low Stock Items, Monthly Revenue).
   *
   * The admin-service calls order-service and catalog-service internally
   * via Feign clients to aggregate this data.
   */
  getDashboard(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.API}/dashboard`);
  }

  /**
   * Creates a new medicine via the catalog-service.
   * UI effect: new medicine row appears in the admin medicines table.
   * Called from AdminMedicinesComponent when admin submits the "Add Medicine" form.
   */
  addMedicine(medicine: Medicine): Observable<Medicine> {
    return this.http.post<Medicine>(`${environment.apiUrl}/api/catalog/medicines`, medicine);
  }

  /**
   * Updates an existing medicine's details.
   * UI effect: the medicine row in the admin table updates with new values.
   * Called from AdminMedicinesComponent when admin submits the "Edit Medicine" form.
   */
  updateMedicine(id: number, medicine: Medicine): Observable<Medicine> {
    return this.http.put<Medicine>(`${environment.apiUrl}/api/catalog/medicines/${id}`, medicine);
  }

  /**
   * Permanently deletes a medicine from the catalog.
   * UI effect: the medicine disappears from the admin table and the public medicines list.
   * Called after admin confirms the delete dialog.
   */
  deleteMedicine(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/catalog/medicines/${id}`);
  }

  // ── Prescription management ─────────────────────────────────────────────

  /**
   * Fetches all prescriptions with PENDING status.
   * UI effect: populates the admin prescriptions review page.
   * Admin can then approve or reject each one.
   */
  getPendingPrescriptions(): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${environment.apiUrl}/api/catalog/prescriptions/pending`);
  }

  /**
   * Approves a prescription — changes status from PENDING to APPROVED.
   * UI effect: the prescription card shows a green "APPROVED" badge.
   * The customer can now purchase prescription-required medicines.
   */
  approvePrescription(id: number): Observable<Prescription> {
    return this.http.put<Prescription>(`${environment.apiUrl}/api/catalog/prescriptions/${id}/approve`, {});
  }

  /**
   * Rejects a prescription with a reason — changes status to REJECTED.
   * UI effect: the prescription card shows a red "REJECTED" badge with the reason text.
   *
   * @param reason — admin's explanation shown to the customer (e.g. "Image too blurry")
   */
  rejectPrescription(id: number, reason: string): Observable<Prescription> {
    return this.http.put<Prescription>(`${environment.apiUrl}/api/catalog/prescriptions/${id}/reject`, { reason });
  }

  // ── Order management ────────────────────────────────────────────────────

  /**
   * Fetches all orders in the system.
   * UI effect: populates the admin orders management table with all customer orders.
   * Admin can then update each order's status (PACKED, SHIPPED, DELIVERED, etc.).
   */
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.apiUrl}/api/orders`);
  }

  /**
   * Updates an order's status.
   * UI effect: the status badge on the order row changes (e.g. PENDING → SHIPPED).
   * Valid statuses: PENDING, PAID, PACKED, SHIPPED, DELIVERED, CANCELLED
   */
  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${environment.apiUrl}/api/orders/${id}/status`, { status });
  }

  // ── Notifications ───────────────────────────────────────────────────────

  /**
   * Sends a direct notification to a user via the notification-service.
   * UI effect: shows success message on the admin notifications page.
   * The notification-service logs it (and sends email if SMTP is configured).
   *
   * @param request — recipientEmail, subject, message, type
   */
  sendNotification(request: NotificationRequest): Observable<string> {
    return this.http.post(`${this.NOTIFY_API}/send`, request, { responseType: 'text' });
  }
}
