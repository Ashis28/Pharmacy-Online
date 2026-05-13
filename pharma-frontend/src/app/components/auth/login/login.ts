/**
 * LoginComponent — The login page (/login).
 *
 * WHAT IT SHOWS:
 *   A form with email, password (with show/hide toggle), and a "Sign In" button.
 *   Error messages for wrong credentials or server issues.
 *   Links to Register and Register as Admin pages.
 *
 * HOW LOGIN WORKS:
 *   1. User fills in email + password and clicks "Sign In"
 *   2. onSubmit() validates the fields are not empty
 *   3. Calls auth.login() → POST /api/auth/login
 *   4. On success:
 *      - AuthService stores the JWT token in localStorage
 *      - AuthService updates the currentUser signal
 *      - Navbar immediately shows logged-in links
 *      - ADMIN users → redirected to /admin/dashboard
 *      - CUSTOMER users → redirected to /home
 *   5. On error:
 *      - 401 → "Invalid email or password"
 *      - 0 (network error) → "Cannot connect to server"
 *      - Other → "Login failed. Please try again."
 *
 * GUEST GUARD:
 *   This page has canActivate: [guestGuard] in app.routes.ts.
 *   If a user is already logged in and tries to visit /login,
 *   they are automatically redirected to /home or /admin/dashboard.
 */

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  auth   = inject(AuthService);
  router = inject(Router);

  /** Two-way bound to the email input field */
  email = '';

  /** Two-way bound to the password input field */
  password = '';

  /** Shows the loading spinner on the Sign In button while the API call is in progress */
  loading = false;

  /** Error message shown below the form on login failure */
  error = '';

  /**
   * Controls whether the password is shown as plain text or dots.
   * Toggled by the 👁️/🙈 button next to the password field.
   * UI effect: input type switches between 'text' and 'password'.
   */
  showPassword = false;

  /**
   * Handles the login form submission.
   *
   * Validation: both email and password must be filled in.
   *
   * On success:
   *   - Admin users → /admin/dashboard
   *   - Customer users → /home
   *
   * On error:
   *   - status 0 → backend is not running
   *   - status 401 → wrong email or password
   *   - other → generic error message
   */
  onSubmit() {
    // Basic validation — both fields required
    if (!this.email || !this.password) {
      this.error = 'Please fill in all fields.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        // Redirect based on role — admins go to dashboard, customers go home
        if (res.roles.includes('ADMIN')) {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.error = 'Cannot connect to server. Make sure the backend is running on port 8888.';
        } else if (err.status === 401) {
          this.error = 'Invalid email or password.';
        } else {
          this.error = 'Login failed. Please try again.';
        }
      }
    });
  }
}
