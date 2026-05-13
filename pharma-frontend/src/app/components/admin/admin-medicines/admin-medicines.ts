/**
 * AdminMedicinesComponent — Medicine catalog management page (/admin/medicines).
 *
 * WHAT IT SHOWS:
 *   A table of all medicines with columns: ID, Name, Price, Stock, Type (Rx/OTC), Expiry, Actions.
 *   Low-stock rows (stock ≤ 10) are highlighted in amber.
 *   Out-of-stock rows (stock = 0) are highlighted in red.
 *   "+ Add Medicine" button opens the add/edit modal.
 *   Each row has ✏️ Edit and 🗑️ Delete buttons.
 *
 * ADD/EDIT MODAL:
 *   A modal overlay with a form for medicine details.
 *   `showForm` controls visibility. `editingId` is null for add, set for edit.
 *   The same form and modal are reused for both add and edit operations.
 *   On submit, save() decides whether to call addMedicine() or updateMedicine().
 *
 * DELETE CONFIRMATION:
 *   Clicking 🗑️ sets `deleteConfirm` to the medicine ID.
 *   A confirmation modal appears asking "Are you sure?".
 *   Confirming calls deleteMedicine(). Cancelling clears deleteConfirm.
 *   This prevents accidental deletions.
 *
 * FORM PAYLOAD BUILDING:
 *   The save() method carefully builds the payload to match the backend DTO:
 *   - Converts price and stockQuantity to numbers (form inputs return strings)
 *   - Sets expiryDate to null if empty (backend expects LocalDate or null)
 *   - Sets categoryId to null if empty or invalid (optional field)
 *
 * SUCCESS MESSAGES:
 *   After add/edit/delete, a green success banner appears for 3-4 seconds
 *   then automatically disappears (setTimeout clears it).
 */

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { MedicineService } from '../../../services/medicine.service';
import { Medicine } from '../../../models/medicine.models';

@Component({
  selector: 'app-admin-medicines',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-medicines.html',
  styleUrls: ['./admin-medicines.css']
})
export class AdminMedicinesComponent implements OnInit {
  adminService    = inject(AdminService);
  medicineService = inject(MedicineService);

  /** All medicines loaded from the backend */
  medicines: Medicine[] = [];

  /** Controls the loading spinner while fetching medicines */
  loading = false;

  /** Error message shown if the medicines list fails to load */
  loadError = '';

  /** Controls the add/edit modal visibility */
  showForm = false;

  /**
   * The ID of the medicine being edited.
   * null = we're adding a new medicine.
   * number = we're editing an existing medicine with this ID.
   */
  editingId: number | null = null;

  /** Validation error shown inside the modal form */
  formError = '';

  /** Success banner message (auto-clears after 3-4 seconds) */
  success = '';

  /** Controls the saving spinner on the modal's Save button */
  saving = false;

  /**
   * The ID of the medicine pending deletion.
   * null = no delete confirmation is showing.
   * number = the delete confirmation modal is showing for this medicine ID.
   */
  deleteConfirm: number | null = null;

  /** The form data bound to the add/edit modal inputs */
  form: Medicine = this.emptyForm();

  /**
   * Lifecycle hook — loads medicines when the page opens.
   */
  ngOnInit() {
    this.loadMedicines();
  }

  /**
   * Returns a blank medicine object for the "Add Medicine" form.
   * Called when opening the add modal to reset any previous values.
   */
  emptyForm(): Medicine {
    return {
      name: '',
      description: '',
      price: 0,
      stockQuantity: 0,
      requiresPrescription: false,
      expiryDate: '',
      categoryId: null
    };
  }

  /**
   * Fetches all medicines from the catalog.
   * UI effect: the medicines table populates with rows.
   * Low-stock rows are highlighted by the template using [class.row-low].
   */
  loadMedicines() {
    this.loading = true;
    this.loadError = '';
    this.medicineService.getAllMedicines().subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.loadError = 'Cannot connect to backend. Make sure all services are running.';
        } else {
          this.loadError = `Failed to load medicines (${err.status}). ${err.error?.message || ''}`;
        }
      }
    });
  }

  /**
   * Opens the modal in "Add" mode with a blank form.
   * UI effect: modal slides in with empty fields.
   */
  openAdd() {
    this.form = this.emptyForm(); // reset form to blank
    this.editingId = null;        // null = add mode
    this.formError = '';
    this.showForm = true;
  }

  /**
   * Opens the modal in "Edit" mode pre-filled with the medicine's current values.
   * UI effect: modal slides in with the medicine's existing data in the fields.
   *
   * @param medicine — the medicine row that was clicked for editing
   */
  openEdit(medicine: Medicine) {
    // Pre-fill the form with the medicine's current values
    this.form = {
      name: medicine.name,
      description: medicine.description || '',
      price: medicine.price,
      stockQuantity: medicine.stockQuantity,
      requiresPrescription: medicine.requiresPrescription,
      expiryDate: medicine.expiryDate || '',
      categoryId: medicine.categoryId ?? null
    };
    this.editingId = medicine.id!; // set to the medicine's ID = edit mode
    this.formError = '';
    this.showForm = true;
  }

  /**
   * Closes the add/edit modal without saving.
   * UI effect: modal disappears, form is reset.
   */
  closeForm() {
    this.showForm = false;
    this.editingId = null;
    this.formError = '';
  }

  /**
   * Validates and saves the medicine (add or update).
   *
   * Validation:
   *   - Name must not be empty
   *   - Price must be > 0
   *   - Stock quantity must be ≥ 0
   *
   * Payload building:
   *   - Converts form values to correct types (string inputs → numbers)
   *   - Sets expiryDate to null if empty
   *   - Sets categoryId to null if empty or invalid
   *
   * On success:
   *   - Modal closes
   *   - Success banner appears for 4 seconds
   *   - Medicine list refreshes
   *
   * On error:
   *   - Error message shown inside the modal
   */
  save() {
    // Validation
    if (!this.form.name?.trim()) {
      this.formError = 'Medicine name is required.';
      return;
    }
    if (!this.form.price || this.form.price <= 0) {
      this.formError = 'Price must be greater than 0.';
      return;
    }
    if (this.form.stockQuantity < 0) {
      this.formError = 'Stock quantity cannot be negative.';
      return;
    }

    this.saving = true;
    this.formError = '';

    // Build the payload matching the backend MedicineRequest DTO exactly
    const payload: any = {
      name:                 this.form.name.trim(),
      description:          this.form.description?.trim() || '',
      price:                Number(this.form.price),        // ensure it's a number
      stockQuantity:        Number(this.form.stockQuantity), // ensure it's a number
      requiresPrescription: Boolean(this.form.requiresPrescription)
    };

    // Backend expects LocalDate (yyyy-MM-dd) or null — not an empty string
    if (this.form.expiryDate && this.form.expiryDate.trim()) {
      payload.expiryDate = this.form.expiryDate.trim();
    } else {
      payload.expiryDate = null;
    }

    // categoryId is optional — only include if it's a valid positive number
    const catRaw = this.form.categoryId;
    const catId  = catRaw ? Number(catRaw) : NaN;
    payload.categoryId = (catRaw && !isNaN(catId) && catId > 0) ? catId : null;

    // Decide: add new medicine or update existing one
    const obs = this.editingId
      ? this.adminService.updateMedicine(this.editingId, payload)
      : this.adminService.addMedicine(payload);

    obs.subscribe({
      next: () => {
        this.saving = false;
        this.success = this.editingId
          ? '✅ Medicine updated successfully!'
          : '✅ Medicine added successfully!';
        this.closeForm();
        this.loadMedicines(); // refresh the table
        setTimeout(() => this.success = '', 4000); // auto-clear success message
      },
      error: (err) => {
        this.saving = false;
        if (err.status === 0) {
          this.formError = 'Cannot connect to backend. Check if services are running.';
        } else if (err.status === 403) {
          this.formError = 'Access denied. Admin role required.';
        } else {
          this.formError = err.error?.message || err.error || `Failed to save medicine (${err.status}).`;
        }
      }
    });
  }

  /**
   * Shows the delete confirmation modal for a medicine.
   * UI effect: a "Are you sure?" dialog appears.
   * @param id — the medicine ID to potentially delete
   */
  confirmDelete(id: number) {
    this.deleteConfirm = id; // triggers the confirmation modal in the template
  }

  /**
   * Dismisses the delete confirmation without deleting.
   * UI effect: the confirmation modal disappears.
   */
  cancelDelete() {
    this.deleteConfirm = null;
  }

  /**
   * Permanently deletes a medicine after confirmation.
   * UI effect: medicine row disappears from the table, success banner appears.
   */
  deleteMedicine(id: number) {
    this.adminService.deleteMedicine(id).subscribe({
      next: () => {
        this.deleteConfirm = null;
        this.success = '✅ Medicine deleted.';
        this.loadMedicines(); // refresh the table
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.deleteConfirm = null;
        this.loadError = err.error?.message || 'Failed to delete medicine.';
      }
    });
  }
}
