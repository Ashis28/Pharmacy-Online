/**
 * AdminPrescriptionsComponent — Prescription review page (/admin/prescriptions).
 *
 * WHAT IT SHOWS:
 *   Cards for each PENDING prescription, showing:
 *   - The prescription image (or a placeholder if the URL is broken)
 *   - Customer email
 *   - Upload date
 *   - ✅ Approve button and ❌ Reject button
 *
 * NOTE: Only PENDING prescriptions are shown here.
 * Once approved or rejected, they disappear from this list.
 * The customer can see all their prescriptions (including approved/rejected)
 * on their own prescriptions page.
 *
 * APPROVE FLOW:
 *   Admin clicks ✅ Approve → approve() calls adminService.approvePrescription()
 *   → backend changes status to APPROVED
 *   → success banner appears, list refreshes (approved prescription disappears)
 *
 * REJECT FLOW:
 *   Admin clicks ❌ Reject → openReject() opens the rejection reason modal
 *   → Admin types a reason (e.g. "Image too blurry")
 *   → submitReject() calls adminService.rejectPrescription() with the reason
 *   → backend changes status to REJECTED and stores the reason
 *   → success banner appears, list refreshes
 *
 * REJECTION REASON MODAL:
 *   `rejectModal` stores { id, reason } while the modal is open.
 *   null = modal is closed.
 *   The reason is required — submitReject() validates it's not empty.
 *
 * IMAGE ERROR HANDLING:
 *   onImgError() replaces broken image URLs with a grey placeholder.
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { Prescription } from '../../../models/medicine.models';

@Component({
  selector: 'app-admin-prescriptions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-prescriptions.html',
  styleUrls: ['./admin-prescriptions.css']
})
export class AdminPrescriptionsComponent implements OnInit {
  adminService = inject(AdminService);

  /** All PENDING prescriptions awaiting review */
  prescriptions: Prescription[] = [];

  /** Controls the loading spinner */
  loading = false;

  /** Success banner message (auto-clears after 3 seconds) */
  success = '';

  /** Error message shown if approve/reject fails */
  error = '';

  /**
   * Stores the rejection modal state.
   * null = modal is closed.
   * { id, reason } = modal is open for this prescription ID with this reason text.
   */
  rejectModal: { id: number; reason: string } | null = null;

  /**
   * Lifecycle hook — loads pending prescriptions when the page opens.
   */
  ngOnInit() {
    this.loadPrescriptions();
  }

  /**
   * Fetches all PENDING prescriptions from the backend.
   * UI effect: prescription cards appear on the page.
   */
  loadPrescriptions() {
    this.loading = true;
    this.adminService.getPendingPrescriptions().subscribe({
      next: (data) => { this.prescriptions = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  /**
   * Approves a prescription — changes its status to APPROVED.
   * UI effect: the prescription card disappears from the list (it's no longer PENDING).
   * Success banner appears for 3 seconds.
   *
   * @param id — the prescription ID to approve
   */
  approve(id: number) {
    this.adminService.approvePrescription(id).subscribe({
      next: () => {
        this.success = `Prescription #${id} approved!`;
        this.loadPrescriptions(); // refresh — approved prescription disappears
        setTimeout(() => this.success = '', 3000);
      },
      error: () => { this.error = 'Failed to approve.'; }
    });
  }

  /**
   * Opens the rejection reason modal for a prescription.
   * UI effect: a modal appears with a text input for the rejection reason.
   *
   * @param id — the prescription ID to reject
   */
  openReject(id: number) {
    this.rejectModal = { id, reason: '' }; // open modal with empty reason
  }

  /**
   * Handles broken prescription image URLs.
   * UI effect: replaces the broken image with a grey placeholder.
   */
  onImgError(event: Event) {
    (event.target as HTMLImageElement).src =
      'https://placehold.co/300x200/1a1a2e/white?text=Prescription';
  }

  /**
   * Submits the rejection with the typed reason.
   * Validation: reason must not be empty.
   * UI effect: modal closes, prescription disappears from list, success banner appears.
   */
  submitReject() {
    if (!this.rejectModal?.reason.trim()) {
      this.error = 'Please provide a rejection reason.';
      return;
    }
    this.adminService.rejectPrescription(this.rejectModal.id, this.rejectModal.reason).subscribe({
      next: () => {
        this.success = `Prescription #${this.rejectModal!.id} rejected.`;
        this.rejectModal = null; // close the modal
        this.loadPrescriptions(); // refresh — rejected prescription disappears
        setTimeout(() => this.success = '', 3000);
      },
      error: () => { this.error = 'Failed to reject.'; }
    });
  }
}
