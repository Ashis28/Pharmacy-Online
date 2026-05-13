/**
 * FooterComponent — The site footer shown at the bottom of all non-admin pages.
 *
 * WHAT IT SHOWS:
 *   Left:   Brand logo + tagline
 *   Middle: Quick navigation links (Home, Medicines, My Orders, Prescriptions)
 *   Right:  Contact information (name, email, phone, address)
 *   Bottom: Copyright bar with current year
 *
 * HOW CURRENT YEAR WORKS:
 *   `currentYear = new Date().getFullYear()` runs once when the component is created.
 *   It reads the system clock and stores the 4-digit year (e.g. 2026).
 *   The template shows "© 2026 PharmaOnline" — automatically correct every year.
 *
 * WHERE IT'S SHOWN:
 *   app.ts conditionally renders <app-footer /> only when showNavbar is true.
 *   This means it appears on all customer-facing pages but NOT on admin pages.
 *   Admin pages have their own layout (AdminLayoutComponent with a sidebar).
 */

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './footer.html',
  styleUrls: ['./footer.scss']
})
export class FooterComponent {
  /**
   * The current calendar year, used in the copyright notice.
   * UI effect: shows "© 2026 PharmaOnline" (or whatever the current year is).
   * Evaluated once at component creation — no need to update it dynamically.
   */
  currentYear = new Date().getFullYear();
}
