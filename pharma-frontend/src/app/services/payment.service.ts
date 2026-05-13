/**
 * PaymentService — HTTP calls to the payment-service backend.
 *
 * HOW PAYMENT WORKS IN THIS APP:
 *   1. Customer selects a payment method (UPI / Card / Net Banking / COD)
 *   2. For CARD: Razorpay-style modal collects card details (UI only, not sent to backend)
 *   3. For UPI: QR code is shown for scanning
 *   4. For COD: no payment processing — order is just confirmed
 *   5. processPayment() is called → backend generates a transaction ID and returns SUCCESS
 *   6. Frontend shows the processing animation (5 seconds) then success screen
 *   7. markOrderAsPaid() is called to update the order status to PAID
 *
 * NOTE ON SIMULATION:
 *   The backend always returns SUCCESS. In a real app, this would integrate
 *   with Razorpay/Stripe APIs and handle real card charges.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PaymentRequest, PaymentResponse } from '../models/payment.models';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  // Base URL for payment-service endpoints
  private readonly API = `${environment.apiUrl}/api/payments`;

  constructor(private http: HttpClient) {}

  /**
   * Processes a payment for an order.
   * UI effect: triggers the processing overlay animation, then shows success/failure screen.
   *
   * The backend:
   *   1. Creates a Payment record with a generated TXN-XXXXXXXX transaction ID
   *   2. Sets status to SUCCESS
   *   3. Returns the payment details
   *
   * @param request — orderId, customerId, amount, paymentMethod (UPI/CARD/NET_BANKING/CASH)
   * @returns PaymentResponse — contains transactionId, status, amount, orderId
   */
  processPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.API}/process`, request);
  }

  /**
   * Fetches the payment record for a given order.
   * UI effect: could be used to show payment receipt details.
   * Currently used to verify payment status on the orders page.
   *
   * @param orderId — the order whose payment record to fetch
   */
  getPaymentByOrderId(orderId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.API}/order/${orderId}`);
  }
}
