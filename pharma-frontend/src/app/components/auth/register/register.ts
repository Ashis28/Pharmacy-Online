import { Component, inject, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

export interface PasswordStrength {
  score: number;          // 0–4
  label: 'Too Short' | 'Weak' | 'Fair' | 'Strong' | 'Very Strong';
  color: string;
  width: string;
  tips: string[];
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent implements OnInit {
  @Input() isAdmin = false;

  auth = inject(AuthService);
  router = inject(Router);
  route = inject(ActivatedRoute);

  name = '';
  email = '';
  password = '';
  confirmPassword = '';
  adminToken = '';          // only used on admin registration
  loading = false;
  error = '';
  success = '';
  showPassword = false;

  ngOnInit() {
    // Check route data for admin flag
    const data = this.route.snapshot.data;
    if (data['isAdmin']) {
      this.isAdmin = true;
    }
  }

  get role(): string {
    return this.isAdmin ? 'ADMIN' : 'CUSTOMER';
  }

  /** Safely extract a human-readable message from an HTTP error response. */
  private extractMessage(err: any): string {
    try {
      // err.error may be a string (raw JSON) or already parsed object
      const body = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
      return body?.message || body?.error || 'An error occurred.';
    } catch {
      return err.message || 'An error occurred.';
    }
  }

  /** Evaluate password strength and return a descriptor object */
  get passwordStrength(): PasswordStrength | null {
    if (!this.password) return null;

    const tips: string[] = [];
    let score = 0;

    if (this.password.length < 6) {
      return { score: 0, label: 'Too Short', color: '#e94560', width: '15%', tips: ['Use at least 6 characters'] };
    }

    if (this.password.length >= 8) score++;
    else tips.push('Use at least 8 characters');

    if (/[A-Z]/.test(this.password)) score++;
    else tips.push('Add an uppercase letter');

    if (/[0-9]/.test(this.password)) score++;
    else tips.push('Add a number');

    if (/[^A-Za-z0-9]/.test(this.password)) score++;
    else tips.push('Add a special character (!@#$...)');

    const levels: PasswordStrength[] = [
      { score: 1, label: 'Weak',       color: '#e94560', width: '25%',  tips },
      { score: 2, label: 'Fair',       color: '#f5a623', width: '50%',  tips },
      { score: 3, label: 'Strong',     color: '#2ed573', width: '75%',  tips },
      { score: 4, label: 'Very Strong',color: '#1dd1a1', width: '100%', tips },
    ];

    return levels[score - 1] ?? levels[0];
  }

  onSubmit() {
    if (!this.name || !this.email || !this.password) {
      this.error = 'Please fill in all fields.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }
    if (this.password.length < 6) {
      this.error = 'Password must be at least 6 characters.';
      return;
    }

    // Admin token validation — just check it's not empty; backend does the real check
    if (this.isAdmin) {
      if (!this.adminToken.trim()) {
        this.error = 'Admin registration token is required.';
        return;
      }
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.auth.signup({
      name: this.name,
      email: this.email,
      password: this.password,
      roles: [this.role],
      adminToken: this.isAdmin ? this.adminToken : undefined
    }).subscribe({
      next: () => {
        this.loading = false;
        this.success = `Account created successfully! Redirecting to login...`;
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.error = 'Cannot connect to server. Make sure the backend is running on port 8888.';
        } else if (err.status === 409) {
          this.error = 'Email already registered. Please login.';
        } else if (err.status === 400) {
          // Parse the message cleanly — never show raw JSON
          const msg = this.extractMessage(err);
          this.error = msg.toLowerCase().includes('token') ? 'Invalid token.' : msg;
        } else if (typeof err.error === 'string' && err.error.includes('already')) {
          this.error = 'Email already registered. Please login.';
        } else {
          this.error = this.extractMessage(err) || `Registration failed. Please try again.`;
        }
      }
    });
  }
}
