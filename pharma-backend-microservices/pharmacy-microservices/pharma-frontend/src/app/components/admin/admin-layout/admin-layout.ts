/**
 * AdminLayoutComponent — The shell/wrapper for all admin pages (/admin/*).
 *
 * WHAT IT SHOWS:
 *   A two-column layout:
 *   Left:  Sidebar with navigation links (Dashboard, Medicines, Orders, Prescriptions, Notifications)
 *   Right: The current admin page content (rendered via <router-outlet>)
 *
 * HOW NESTED ROUTING WORKS:
 *   In app.routes.ts, all /admin/* routes are children of this component.
 *   AdminLayoutComponent provides the sidebar shell.
 *   The <router-outlet> inside it renders the child component:
 *   - /admin/dashboard     → DashboardComponent
 *   - /admin/medicines     → AdminMedicinesComponent
 *   - /admin/orders        → AdminOrdersComponent
 *   - /admin/prescriptions → AdminPrescriptionsComponent
 *   - /admin/notifications → AdminNotificationsComponent
 *
 * WHY A SEPARATE LAYOUT?
 *   Admin pages need a sidebar navigation, not the top navbar.
 *   By wrapping all admin routes in this layout component, we get:
 *   - Consistent sidebar on all admin pages
 *   - No main navbar/footer (app.ts hides them for /admin/* URLs)
 *   - Clean separation between customer UI and admin UI
 *
 * SIDEBAR TOGGLE:
 *   `sidebarOpen` controls whether the sidebar is expanded or collapsed.
 *   On mobile, the sidebar can be toggled to save screen space.
 *
 * NAV ITEMS:
 *   The sidebar links are defined as an array of { path, icon, label }.
 *   The template renders them with @for, using RouterLinkActive to highlight
 *   the currently active link.
 */

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-layout.html',
  styleUrls: ['./admin-layout.scss']
})
export class AdminLayoutComponent {
  auth = inject(AuthService); // used to show admin name and logout button in sidebar

  /**
   * Controls whether the sidebar is expanded (true) or collapsed (false).
   * UI effect: sidebar shows full labels when open, just icons when collapsed.
   * Toggled by the collapse button at the top of the sidebar.
   */
  sidebarOpen = true;

  /**
   * The navigation items shown in the sidebar.
   * Each item has a route path, an emoji icon, and a text label.
   * Rendered with @for in the template.
   * RouterLinkActive automatically adds an 'active' CSS class to the current page's link.
   */
  navItems = [
    { path: '/admin/dashboard',     icon: '📊', label: 'Dashboard' },
    { path: '/admin/medicines',     icon: '💊', label: 'Medicines' },
    { path: '/admin/orders',        icon: '📦', label: 'Orders' },
    { path: '/admin/prescriptions', icon: '📋', label: 'Prescriptions' },
    { path: '/admin/notifications', icon: '🔔', label: 'Notifications' }
  ];
}
