/**
 * PaymentComponent — The payment page (/payment/:orderId).
 *
 * WHAT IT SHOWS:
 *   Left side: Order summary (items, total, delivery address)
 *   Right side: Payment method selector + method-specific UI
 *
 * PAYMENT METHODS AND THEIR UI:
 *   UPI       → Shows a scannable QR code + UPI ID + app chips (GPay, PhonePe, etc.)
 *   CARD      → Opens a Razorpay-style modal with live card preview + form fields
 *   NET_BANKING → Shows bank selection grid inside the Razorpay modal
 *   CASH      → Skips payment entirely, shows "Order Placed" confirmation
 *
 * PAYMENT STAGES (the `stage` variable controls what's shown):
 *   'idle'       → Normal payment form is visible
 *   'razorpay'   → Razorpay-style modal is open (for CARD/NET_BANKING)
 *   'processing' → Full-screen overlay with animated steps (5 seconds)
 *   'success'    → Payment successful screen with transaction details
 *   'cod_placed' → Order placed screen for Cash on Delivery (no payment)
 *   'failed'     → Error message shown, user can retry
 *
 * PROCESSING ANIMATION:
 *   4 steps animate one by one every 1250ms (~5 seconds total):
 *   🔐 Securing → 📡 Contacting gateway → ✅ Verifying → 🎉 Finalising
 *   stepTimer drives this animation with setInterval.
 *   successTimer adds a 800ms delay before showing the success screen.
 *
 * UPI QR CODE:
 *   Built from a UPI deep-link string: upi://pay?pa=...&am=...
 *   Rendered as an image via Google Charts API (no npm package needed).
 *   The QR has an animated red scan line sweeping across it.
 *
 * RAZORPAY MODAL:
 *   Rendered OUTSIDE .payment-page div so position:fixed works correctly.
 *   (Angular components with transforms break fixed positioning.)
 *   Card number is auto-formatted with spaces every 4 digits.
 *   Card brand (Visa/Mastercard/Amex) is detected from the first digits.
 *
 * CLEANUP (ngOnDestroy):
 *   Clears the setInterval and setTimeout to prevent memory leaks
 *   if the user navigates away before payment completes.
 */

import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { Order } from '../../models/order.models';
import { PaymentResponse } from '../../models/payment.models';

/** All possible states of the payment UI */
export type PaymentStage = 'idle' | 'razorpay' | 'processing' | 'success' | 'cod_placed' | 'failed';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './payment.html',
  styleUrls: ['./payment.scss']
})
export class PaymentComponent implements OnInit, OnDestroy {
  paymentService = inject(PaymentService);
  orderService   = inject(OrderService);
  auth           = inject(AuthService);
  route          = inject(ActivatedRoute); // reads :orderId from the URL
  router         = inject(Router);

  /** The order ID extracted from the URL (/payment/42 → orderId = 42) */
  orderId: number = 0;

  /** The full order object fetched from the backend */
  order: Order | null = null;

  /** The payment result returned by the backend after processing */
  paymentResult: PaymentResponse | null = null;

  /** Currently selected payment method — defaults to UPI */
  selectedMethod: 'CARD' | 'UPI' | 'CASH' | 'NET_BANKING' = 'UPI';

  /** Controls the loading spinner while fetching the order */
  loadingOrder = false;

  /** Error message shown when payment fails */
  error = '';

  /**
   * Controls which UI state is shown.
   * This single variable drives the entire payment flow.
   */
  stage: PaymentStage = 'idle';

  /**
   * Index of the currently highlighted processing step (0-3).
   * Advances every 1250ms via stepTimer.
   */
  processingStep = 0;

  /** setInterval handle — advances processingStep every 1250ms */
  private stepTimer: any;

  /** setTimeout handle — delays showing success screen by 800ms after API returns */
  private successTimer: any;

  /** The 4 animated steps shown during payment processing */
  readonly processingSteps = [
    { icon: '🔐', label: 'Securing connection...' },
    { icon: '📡', label: 'Contacting payment gateway...' },
    { icon: '✅', label: 'Verifying transaction...' },
    { icon: '🎉', label: 'Finalising order...' },
  ];

  /** Payment method options shown as radio buttons */
  paymentMethods = [
    { value: 'UPI',         label: 'UPI',              icon: '📱', desc: 'Pay via UPI apps' },
    { value: 'CARD',        label: 'Card',             icon: '💳', desc: 'Credit / Debit card via Razorpay' },
    { value: 'NET_BANKING', label: 'Net Banking',      icon: '🏦', desc: 'Internet banking' },
    { value: 'CASH',        label: 'Cash on Delivery', icon: '💵', desc: 'Pay on delivery' },
  ];

  // ── Razorpay modal fields ────────────────────────────────────────────────
  rzCardNumber = '';  // formatted as "1234 5678 9012 3456"
  rzCardName   = '';  // cardholder name
  rzExpiry     = '';  // formatted as "MM/YY"
  rzCvv        = '';  // 3-4 digit security code
  rzCardError  = '';  // validation error shown inside the modal
  rzActiveTab: 'card' | 'upi' | 'netbanking' = 'card'; // active tab in Razorpay modal

  // ── UPI QR Code ──────────────────────────────────────────────────────────

  /**
   * Builds the UPI deep-link string for the QR code.
   * Format: upi://pay?pa=<upi-id>&pn=<name>&am=<amount>&cu=INR&tn=<note>
   * When scanned by GPay/PhonePe/Paytm, it pre-fills the payment details.
   */
  get upiString(): string {
    const amount = this.order?.totalAmount ?? 0;
    return `upi://pay?pa=sagarpakar777@okaxis&pn=PharmaOnline&am=${amount}&cu=INR&tn=Order%20${this.orderId}`;
  }

  /**
   * Generates the QR code image URL using Google Charts API.
   * The QR encodes the UPI deep-link string above.
   * UI effect: an <img> tag with this src shows the scannable QR code.
   */
  get qrImageUrl(): string {
    const encoded = encodeURIComponent(this.upiString);
    return `https://chart.googleapis.com/chart?cht=qr&chs=220x220&chl=${encoded}&choe=UTF-8`;
  }

  // ── Card formatting ──────────────────────────────────────────────────────

  /**
   * Formats the card number input as "XXXX XXXX XXXX XXXX".
   * UI effect: spaces are automatically inserted every 4 digits as the user types.
   * Non-numeric characters are stripped. Max 16 digits.
   */
  formatCardNumber(event: Event) {
    const input = event.target as HTMLInputElement;
    let val = input.value.replace(/\D/g, '').slice(0, 16); // digits only, max 16
    this.rzCardNumber = val.replace(/(.{4})/g, '$1 ').trim(); // add space every 4
  }

  /**
   * Formats the expiry input as "MM/YY".
   * UI effect: the slash is automatically inserted after the month digits.
   */
  formatExpiry(event: Event) {
    const input = event.target as HTMLInputElement;
    let val = input.value.replace(/\D/g, '').slice(0, 4);
    if (val.length >= 3) val = val.slice(0, 2) + '/' + val.slice(2);
    this.rzExpiry = val;
  }

  /**
   * Detects the card brand from the first digits.
   * UI effect: shows the correct logo (VISA/Mastercard/AMEX) on the card preview.
   * Visa starts with 4, Mastercard with 51-55 or 22-27, Amex with 34 or 37.
   */
  get cardBrand(): 'visa' | 'mastercard' | 'amex' | 'unknown' {
    const n = this.rzCardNumber.replace(/\s/g, '');
    if (n.startsWith('4'))                        return 'visa';
    if (/^5[1-5]/.test(n) || /^2[2-7]/.test(n)) return 'mastercard';
    if (/^3[47]/.test(n))                         return 'amex';
    return 'unknown';
  }

  // ── Lifecycle ────────────────────────────────────────────────────────────

  /**
   * Reads the orderId from the URL and fetches the order details.
   * URL: /payment/42 → orderId = 42
   */
  ngOnInit() {
    this.orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    if (this.orderId) this.loadOrder();
  }

  /**
   * Cleans up timers when the component is destroyed.
   * Prevents memory leaks if the user navigates away mid-payment.
   */
  ngOnDestroy() {
    clearInterval(this.stepTimer);
    clearTimeout(this.successTimer);
  }

  /**
   * Fetches the order details from the backend.
   * UI effect: populates the Order Summary card on the left side.
   */
  loadOrder() {
    this.loadingOrder = true;
    this.orderService.getOrderById(this.orderId).subscribe({
      next: (order) => { this.order = order; this.loadingOrder = false; },
      error: () => { this.loadingOrder = false; }
    });
  }

  // ── Payment flow ─────────────────────────────────────────────────────────

  /**
   * Handles the Pay button click — routes to the correct flow based on method.
   *
   * CASH → immediately shows "Order Placed" (no payment processing)
   * CARD → opens the Razorpay-style modal for card details
   * UPI/NET_BANKING → goes directly to processPayment()
   */
  onPayClick() {
    if (this.selectedMethod === 'CASH') {
      // COD: no payment needed — just confirm the order
      this.stage = 'cod_placed';
      return;
    }
    if (this.selectedMethod === 'CARD') {
      // Open Razorpay modal to collect card details
      this.rzCardError = '';
      this.stage = 'razorpay';
    } else {
      // UPI or Net Banking — go straight to processing
      this.processPayment();
    }
  }

  /**
   * Closes the Razorpay modal without processing payment.
   * UI effect: modal disappears, user is back on the payment form.
   */
  closeRazorpay() {
    this.stage = 'idle';
    this.rzCardError = '';
  }

  /**
   * Validates card details and submits the Razorpay modal.
   * UI effect: if validation passes, modal closes and processing overlay appears.
   * If validation fails, error message shows inside the modal.
   */
  submitRazorpay() {
    const digits = this.rzCardNumber.replace(/\s/g, '');
    if (digits.length < 16)       { this.rzCardError = 'Enter a valid 16-digit card number.'; return; }
    if (!this.rzCardName.trim())  { this.rzCardError = 'Enter the cardholder name.'; return; }
    if (this.rzExpiry.length < 5) { this.rzCardError = 'Enter a valid expiry (MM/YY).'; return; }
    if (this.rzCvv.length < 3)    { this.rzCardError = 'Enter a valid CVV.'; return; }

    this.rzCardError = '';
    this.stage = 'idle'; // close modal before showing processing overlay
    this.processPayment();
  }

  /**
   * Calls the payment API and drives the processing animation.
   *
   * Flow:
   *   1. Sets stage to 'processing' → shows the full-screen overlay
   *   2. Starts stepTimer → advances the step animation every 1250ms
   *   3. Calls paymentService.processPayment() → backend creates payment record
   *   4. On SUCCESS:
   *      - Stops the timer
   *      - Jumps to last step visually
   *      - After 800ms: sets stage to 'success', marks order as PAID
   *   5. On FAILURE:
   *      - Stops the timer
   *      - Sets stage to 'failed', shows error message
   */
  processPayment() {
    if (!this.order) return;
    this.error = '';
    this.stage = 'processing'; // show the processing overlay
    this.processingStep = 0;

    // Advance the step indicator every 1250ms (~5 seconds for 4 steps)
    this.stepTimer = setInterval(() => {
      if (this.processingStep < this.processingSteps.length - 1) {
        this.processingStep++;
      }
    }, 1250);

    const customerId = this.auth.getUserId();

    this.paymentService.processPayment({
      orderId: this.orderId,
      customerId,
      amount: this.order.totalAmount!,
      paymentMethod: this.selectedMethod
    }).subscribe({
      next: (result) => {
        this.paymentResult = result;
        clearInterval(this.stepTimer); // stop the step animation

        if (result.status === 'SUCCESS') {
          // Jump to last step visually, then show success after 800ms
          this.processingStep = this.processingSteps.length - 1;
          this.successTimer = setTimeout(() => {
            this.stage = 'success';
            // Update order status to PAID in the backend
            this.orderService.markOrderAsPaid(this.orderId).subscribe();
          }, 800);
        } else {
          this.stage = 'failed';
          this.error = 'Payment failed. Please try again.';
        }
      },
      error: (err) => {
        clearInterval(this.stepTimer);
        this.stage = 'failed';
        this.error = err.error?.message || 'Payment processing failed.';
      }
    });
  }

  /**
   * Resets the payment form so the user can try again after a failure.
   * UI effect: processing overlay disappears, payment form is shown again.
   */
  retryPayment() {
    this.stage = 'idle';
    this.error = '';
    this.processingStep = 0;
  }
}
