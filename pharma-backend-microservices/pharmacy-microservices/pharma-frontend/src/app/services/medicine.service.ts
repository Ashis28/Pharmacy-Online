/**
 * MedicineService — All HTTP calls related to medicines and prescriptions.
 *
 * WHY A SEPARATE SERVICE?
 *   Components should not make HTTP calls directly. This service is the
 *   single point of contact for the catalog-service backend. If the API
 *   URL changes, you only update it here — not in every component.
 *
 * PUBLIC vs ADMIN ENDPOINTS:
 *   - GET medicines: no login required (anyone can browse)
 *   - POST/PUT/DELETE medicines: requires ADMIN role (enforced by backend)
 *   - Prescriptions: require login (enforced by route guards + backend)
 *
 * HOW OBSERVABLES WORK:
 *   Every method returns an Observable. Nothing happens until a component
 *   calls .subscribe() on it. The HTTP request fires at that point.
 *   Components handle the response in the `next` callback and errors in `error`.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Medicine, Prescription, PrescriptionRejectRequest } from '../models/medicine.models';

@Injectable({ providedIn: 'root' })
export class MedicineService {
  // Base URL for catalog-service endpoints — reads from environment.ts
  private readonly API = `${environment.apiUrl}/api/catalog`;

  constructor(private http: HttpClient) {}

  // ── Public endpoints — no authentication required ──────────────────────

  /**
   * Fetches all medicines from the catalog.
   * UI effect: populates the medicines list page and home page featured section.
   * Called on: MedicinesListComponent.ngOnInit(), HomeComponent.ngOnInit()
   */
  getAllMedicines(): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(`${this.API}/medicines`);
  }

  /**
   * Fetches a single medicine by its database ID.
   * UI effect: used to show medicine details.
   * Called on: payment page to verify medicine details.
   */
  getMedicineById(id: number): Observable<Medicine> {
    return this.http.get<Medicine>(`${this.API}/medicines/${id}`);
  }

  /**
   * Searches medicines by name (case-insensitive, partial match).
   * UI effect: filters the medicines list as the user types in the search box.
   * Example: searching "vita" returns "Vitamin D3", "Vitamin C", etc.
   */
  searchMedicines(name: string): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(`${this.API}/medicines/search?name=${name}`);
  }

  // ── Admin endpoints — require ADMIN role (enforced by backend) ──────────

  /**
   * Creates a new medicine in the catalog.
   * UI effect: new medicine appears in the admin medicines table.
   * The JWT token (with ADMIN role) is automatically attached by the auth interceptor.
   */
  addMedicine(medicine: Medicine): Observable<Medicine> {
    return this.http.post<Medicine>(`${this.API}/medicines`, medicine);
  }

  /**
   * Updates an existing medicine's details (name, price, stock, etc.).
   * UI effect: the row in the admin medicines table updates with new values.
   */
  updateMedicine(id: number, medicine: Medicine): Observable<Medicine> {
    return this.http.put<Medicine>(`${this.API}/medicines/${id}`, medicine);
  }

  /**
   * Permanently deletes a medicine from the catalog.
   * UI effect: the medicine disappears from the admin table and the public list.
   * Returns void because the backend returns 204 No Content.
   */
  deleteMedicine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/medicines/${id}`);
  }

  /**
   * Gets the count of medicines with stock ≤ 10 units.
   * UI effect: shows the "Low Stock Items" number on the admin dashboard.
   * No auth required — count is non-sensitive data.
   */
  getLowStockCount(): Observable<number> {
    return this.http.get<number>(`${this.API}/medicines/low-stock-count`);
  }

  // ── Prescription endpoints — require login ──────────────────────────────

  /**
   * Uploads a prescription image URL for admin review.
   * UI effect: new prescription appears in the customer's prescription list with PENDING status.
   *
   * WHY X-Auth-User HEADER?
   *   The gateway strips the JWT and adds X-Auth-User (the email) as a plain header.
   *   The prescription controller reads this header to know who uploaded it.
   *   We also send it directly here for direct service-to-service calls.
   */
  uploadPrescription(customerId: number, imageUrl: string, customerEmail: string): Observable<Prescription> {
    return this.http.post<Prescription>(
      `${this.API}/prescriptions/upload`,
      { customerId: customerId.toString(), imageUrl },
      { headers: { 'X-Auth-User': customerEmail } } // tells backend who is uploading
    );
  }

  /**
   * Fetches all prescriptions uploaded by a specific customer.
   * UI effect: populates the prescriptions page with the customer's history.
   */
  getMyPrescriptions(customerId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.API}/prescriptions/my?customerId=${customerId}`);
  }

  /**
   * Fetches all prescriptions awaiting admin review (status = PENDING).
   * UI effect: populates the admin prescriptions management page.
   * Requires ADMIN role — backend enforces with @PreAuthorize.
   */
  getPendingPrescriptions(): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.API}/prescriptions/pending`);
  }

  /**
   * Approves a prescription — changes its status from PENDING to APPROVED.
   * UI effect: the prescription card shows a green "APPROVED" badge.
   * Requires ADMIN role.
   */
  approvePrescription(id: number): Observable<Prescription> {
    return this.http.put<Prescription>(`${this.API}/prescriptions/${id}/approve`, {});
  }

  /**
   * Rejects a prescription with a reason — changes status to REJECTED.
   * UI effect: the prescription card shows a red "REJECTED" badge with the reason.
   * Requires ADMIN role.
   *
   * @param reason — the admin's explanation (e.g. "Image too blurry")
   */
  rejectPrescription(id: number, reason: string): Observable<Prescription> {
    return this.http.put<Prescription>(`${this.API}/prescriptions/${id}/reject`, { reason });
  }
}
