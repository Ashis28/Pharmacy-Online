/**
 * HomeComponent — The landing page of PharmaOnline (/home).
 *
 * WHAT IT SHOWS:
 *   1. Hero section — welcome banner with "Shop Now" and "My Orders" buttons
 *   2. Stats row — "500+ Medicines", "24h Fast Delivery", "100% Authentic", "Secure Payments"
 *   3. Featured Medicines — first 6 medicines from the catalog with "Add to Cart" buttons
 *   4. Loading spinner while medicines are being fetched
 *
 * HOW FEATURED MEDICINES WORK:
 *   On page load (ngOnInit), we call medicineService.getAllMedicines().
 *   We take only the first 6 results with .slice(0, 6) to show as "featured".
 *   This is a simple approach — in a real app you'd have a dedicated "featured" API.
 *
 * ADD TO CART:
 *   When the user clicks "Add to Cart" on a medicine card:
 *   - If not logged in: CartService redirects to /login automatically
 *   - If logged in: medicine is added to the cart, badge count increases
 *
 * STATS ARRAY:
 *   The stats (500+ Medicines, etc.) are hardcoded in the component.
 *   They're displayed using @for in the template.
 */

import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MedicineService } from '../../services/medicine.service';
import { CartService } from '../../services/cart.service';
import { Medicine } from '../../models/medicine.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  auth            = inject(AuthService);
  medicineService = inject(MedicineService);
  cart            = inject(CartService);
  private refreshSub?: Subscription;

  /** First 6 medicines shown as "featured" on the home page */
  featuredMedicines: Medicine[] = [];

  /** Controls the loading spinner visibility */
  loading = false;

  /**
   * Hardcoded stats shown in the stats row.
   * Each stat has an icon, a value, and a label.
   * Displayed with @for in home.html.
   */
  stats = [
    { value: '500+',   label: 'Medicines' },
    { value: '24h',    label: 'Fast Delivery' },
    { value: '100%',   label: 'Authentic' },
    { value: 'Secure', label: 'Payments' }
  ];

  /**
   * Lifecycle hook — runs once when the component is first created.
   * Fetches medicines from the backend to populate the featured section.
   * UI effect: loading spinner shows, then 6 medicine cards appear.
   */
  ngOnInit() {
    this.loadFeatured();
    this.refreshSub = this.medicineService.onMedicinesRefresh().subscribe(() => this.loadFeatured());
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
  }

  private loadFeatured() {
    this.loading = true;
    this.medicineService.getAllMedicines().subscribe({
      next: (medicines) => {
        this.featuredMedicines = medicines.slice(0, 6);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Adds a medicine to the shopping cart.
   * UI effect:
   *   - If not logged in: redirects to /login
   *   - If logged in: cart badge count increases by 1
   *
   * @param medicine — the medicine object from the featured list
   */
  addToCart(medicine: Medicine) {
    this.cart.addToCart(medicine);
  }
}
