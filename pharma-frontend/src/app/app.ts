/**
 * App — The root component of the entire Angular application.
 *
 * WHAT IS THE ROOT COMPONENT?
 *   Every Angular app has one root component that acts as the container
 *   for everything else. This is it. It's loaded first when the browser
 *   opens the app, and it never gets destroyed while the app is running.
 *
 * WHAT DOES IT DO?
 *   1. Shows the Navbar at the top of every page (except admin pages)
 *   2. Shows the Footer at the bottom of every page (except admin pages)
 *   3. Renders the current page component via <router-outlet>
 *
 * WHY HIDE NAVBAR/FOOTER ON ADMIN PAGES?
 *   The admin section has its own layout (AdminLayoutComponent) with a
 *   sidebar navigation. The main navbar and footer would look out of place
 *   and clutter the admin UI. So we hide them when the URL starts with /admin.
 *
 * HOW DOES ROUTER-OUTLET WORK?
 *   <router-outlet> is a placeholder. Angular's router replaces it with
 *   the component that matches the current URL. When you navigate to /home,
 *   HomeComponent appears there. When you go to /medicines, MedicinesListComponent
 *   appears there. The navbar and footer stay fixed around it.
 *
 * HOW DOES THE ADMIN DETECTION WORK?
 *   We subscribe to router.events and filter for NavigationEnd events.
 *   NavigationEnd fires every time navigation completes (URL changes).
 *   We check if the new URL starts with '/admin' and set showNavbar accordingly.
 */

import { Component, inject } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar';
import { FooterComponent } from './components/footer/footer';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',       // used in index.html as <app-root></app-root>
  standalone: true,           // no NgModule needed — modern Angular approach
  imports: [RouterOutlet, CommonModule, NavbarComponent, FooterComponent],
  template: `
    <!-- Show navbar on all pages EXCEPT admin pages -->
    @if (showNavbar) {
      <app-navbar />
    }

    <!-- This is where the current page component renders -->
    <!-- Angular replaces this with HomeComponent, MedicinesListComponent, etc. -->
    <router-outlet />

    <!-- Show footer on all pages EXCEPT admin pages -->
    @if (showNavbar) {
      <app-footer />
    }
  `
})
export class App {
  private router = inject(Router);

  /**
   * Controls whether the navbar and footer are visible.
   * true  = show navbar + footer (all customer-facing pages)
   * false = hide navbar + footer (admin pages use their own layout)
   */
  showNavbar = true;

  constructor() {
    // Listen to every navigation event in the app
    this.router.events
      .pipe(
        // Only care about NavigationEnd — when navigation is fully complete
        filter(e => e instanceof NavigationEnd)
      )
      .subscribe((e: any) => {
        // Hide navbar/footer when on any admin page (/admin/dashboard, /admin/orders, etc.)
        this.showNavbar = !e.urlAfterRedirects.startsWith('/admin');
      });
  }
}
