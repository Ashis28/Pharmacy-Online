/**
 * PrescriptionsComponent — The customer's prescription management page (/prescriptions).
 *
 * WHAT IT SHOWS:
 *   1. Upload section — a text input for a prescription image URL + "Upload" button
 *   2. Prescription history — cards showing all uploaded prescriptions with their status
 *
 * PRESCRIPTION STATUS FLOW:
 *   PENDING → (admin reviews) → APPROVED or REJECTED
 *
 *   PENDING  = ⏳ orange  — waiting for admin to review
 *   APPROVED = ✅ green   — admin approved, customer can buy Rx medicines
 *   REJECTED = ❌ red     — admin rejected with a reason
 *
 * HOW UPLOAD WORKS:
 *   In a real app, the user would upload an image file.
 *   In this app, the user enters an image URL (simpler for demo purposes).
 *   The URL is sent to the backend which stores it as the prescription image.
 *
 * WHY PRESCRIPTIONS?
 *   Some medicines require a valid prescription (requiresPrescription = true).
 *   Customers must upload a prescription and get it approved before buying those medicines.
 *   The admin reviews the image and approves or rejects it.
 *
 * IMAGE ERROR HANDLING:
 *   onImgError() is called if the prescription image URL fails to load.
 *   It replaces the broken image with a placeholder image.
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MedicineService } from '../../services/medicine.service';
import { AuthService } from '../../services/auth.service';
import { Prescription } from '../../models/medicine.models';

@Component({
  selector: 'app-prescriptions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prescriptions.html',
  styleUrls: ['./prescriptions.css']
})
export class PrescriptionsComponent implements OnInit {
  medicineService = inject(MedicineService);
  auth            = inject(AuthService);

  /** All prescriptions uploaded by the current customer */
  prescriptions: Prescription[] = [];

  /** Controls the loading spinner while fetching prescriptions */
  loading = false;

  /** Controls the loading spinner on the Upload button */
  uploading = false;

  /** Two-way bound to the image URL input field */
  imageUrl = '';

  /** Error message shown if upload fails or URL is empty */
  error = '';

  /** Success message shown after a successful upload */
  success = '';

  /**
   * Lifecycle hook — loads the customer's prescriptions when the page opens.
   */
  ngOnInit() {
    this.loadPrescriptions();
  }

  /**
   * Fetches all prescriptions for the current customer.
   * UI effect: prescription cards appear on the page.
   * Each card shows the image, status badge, and rejection reason (if rejected).
   */
  loadPrescriptions() {
    this.loading = true;
    const customerId = this.auth.getUserId();

    this.medicineService.getMyPrescriptions(customerId).subscribe({
      next: (data) => {
        this.prescriptions = data;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Handles broken prescription image URLs.
   * UI effect: replaces the broken image with a grey placeholder.
   * Called by the (error) event on the <img> tag in the template.
   */
  onImgError(event: Event) {
    (event.target as HTMLImageElement).src =
      'https://placehold.co/300x200/1a1a2e/white?text=Prescription';
  }

  /**
   * Uploads a new prescription image URL for admin review.
   *
   * Validation: imageUrl must not be empty.
   *
   * On success:
   *   - Success message is shown
   *   - imageUrl input is cleared
   *   - Prescription list is refreshed (new PENDING prescription appears)
   *
   * On error:
   *   - Error message from backend is shown
   */
  uploadPrescription() {
    if (!this.imageUrl.trim()) {
      this.error = 'Please enter a prescription image URL.';
      return;
    }

    this.uploading = true;
    this.error = '';
    this.success = '';

    const customerId = this.auth.getUserId();
    const email      = this.auth.getUserEmail();

    this.medicineService.uploadPrescription(customerId, this.imageUrl, email).subscribe({
      next: () => {
        this.uploading = false;
        this.success = 'Prescription uploaded successfully! Awaiting admin review.';
        this.imageUrl = ''; // clear the input
        this.loadPrescriptions(); // refresh the list to show the new prescription
      },
      error: (err) => {
        this.uploading = false;
        this.error = err.error?.message || 'Upload failed. Please try again.';
      }
    });
  }

  /**
   * Returns the CSS class for a prescription status badge.
   * UI effect: PENDING = orange, APPROVED = green, REJECTED = red
   */
  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING:  'status-pending',
      APPROVED: 'status-approved',
      REJECTED: 'status-rejected'
    };
    return map[status] || '';
  }

  /**
   * Returns the emoji icon for a prescription status.
   * UI effect: shown next to the status text on each prescription card.
   */
  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      PENDING: '○', APPROVED: '●', REJECTED: '✕'
    };
    return map[status] || '○';
  }
}
