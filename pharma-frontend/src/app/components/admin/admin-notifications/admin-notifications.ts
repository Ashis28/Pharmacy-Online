/**
 * AdminNotificationsComponent — Manual notification sender (/admin/notifications).
 *
 * WHAT IT SHOWS:
 *   A form to send a custom notification to any user by email.
 *   Fields: Recipient Email, Notification Type (dropdown), Subject, Message.
 *   A "Sent Log" showing the last 10 notifications sent in this session.
 *
 * HOW SENDING WORKS:
 *   Admin fills in the form and clicks "Send Notification".
 *   send() calls adminService.sendNotification() → POST /api/notifications/send
 *   The notification-service receives it and:
 *   - Logs it (always)
 *   - Sends an email (only if MAIL_ENABLED=true in environment)
 *
 * NOTIFICATION TYPES:
 *   A dropdown with predefined types: ORDER_PLACED, ORDER_SHIPPED, etc.
 *   The type is metadata — it doesn't change the email content,
 *   but helps categorize notifications for future filtering.
 *
 * SENT LOG:
 *   A client-side log of notifications sent in this browser session.
 *   Stored in `sentLog` array (not persisted — clears on page refresh).
 *   Shows the last 10 entries (older ones are removed with .pop()).
 *   Useful for the admin to see what they've sent without checking the backend.
 *
 * FORM RESET:
 *   After a successful send, the form is reset to blank values.
 *   The success message shows for a few seconds then the admin can send another.
 */

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

/** Represents one entry in the client-side sent log */
interface SentLogEntry {
  to:      string; // recipient email
  subject: string; // notification subject
  type:    string; // notification type
  time:    Date;   // when it was sent (client-side timestamp)
}

@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-notifications.html',
  styleUrls: ['./admin-notifications.css']
})
export class AdminNotificationsComponent {
  adminService = inject(AdminService);

  /**
   * The notification form data, two-way bound to the template inputs.
   * Reset to blank after each successful send.
   */
  form = {
    recipientEmail: '',
    subject:        '',
    message:        '',
    type:           'ORDER_UPDATE'
  };

  /** All available notification types shown in the type dropdown */
  notificationTypes = [
    'ORDER_PLACED', 'ORDER_PAID', 'ORDER_PACKED',
    'ORDER_SHIPPED', 'ORDER_DELIVERED', 'ORDER_CANCELLED',
    'PRESCRIPTION_APPROVED', 'PRESCRIPTION_REJECTED',
    'LOW_STOCK_ALERT', 'GENERAL'
  ];

  /** Controls the loading spinner on the Send button */
  loading = false;

  /** Success message shown after a notification is sent */
  success = '';

  /** Error message shown if sending fails */
  error = '';

  /**
   * Client-side log of sent notifications (last 10).
   * Not persisted — clears when the page is refreshed.
   * New entries are added to the front (unshift), old ones removed from the back (pop).
   */
  sentLog: SentLogEntry[] = [];

  /**
   * Returns the emoji icon for a notification type.
   * UI effect: shown next to each entry in the sent log.
   */
  getTypeIcon(type: string): string {
    const map: Record<string, string> = {
      ORDER_PLACED:            '→',
      ORDER_PAID:              '✓',
      ORDER_PACKED:            '◉',
      ORDER_SHIPPED:           '→',
      ORDER_DELIVERED:         '✓',
      ORDER_CANCELLED:         '✕',
      PRESCRIPTION_APPROVED:   '✓',
      PRESCRIPTION_REJECTED:   '✕',
      LOW_STOCK_ALERT:         '!',
      GENERAL:                 '·',
      ORDER_UPDATE:            '↻'
    };
    return map[type] || '·';
  }

  /**
   * Sends the notification to the notification-service.
   *
   * Validation: all 3 fields (email, subject, message) must be filled.
   *
   * On success:
   *   - Success message is shown
   *   - Entry is added to the sent log
   *   - Form is reset to blank
   *
   * On error:
   *   - Error message from backend is shown
   */
  send() {
    // Validate all required fields
    if (!this.form.recipientEmail || !this.form.subject || !this.form.message) {
      this.error = 'All fields are required.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.adminService.sendNotification(this.form).subscribe({
      next: () => {
        this.loading = false;
        this.success = `Notification queued for ${this.form.recipientEmail}. It will be processed by the notification service.`;

        // Add to the client-side sent log (newest first)
        this.sentLog.unshift({
          to:      this.form.recipientEmail,
          subject: this.form.subject,
          type:    this.form.type,
          time:    new Date()
        });
        // Keep only the last 10 entries
        if (this.sentLog.length > 10) this.sentLog.pop();

        // Reset the form for the next notification
        this.form = { recipientEmail: '', subject: '', message: '', type: 'ORDER_UPDATE' };
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Failed to send notification.';
      }
    });
  }
}
