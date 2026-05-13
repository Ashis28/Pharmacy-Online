export interface PaymentRequest {
  orderId: number;
  customerId: number;
  amount: number;
  paymentMethod: 'CARD' | 'UPI' | 'CASH' | 'NET_BANKING';
}

export interface PaymentResponse {
  paymentId: number;
  orderId: number;
  amount: number;
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  transactionId: string;
  createdAt: string;
  paymentMethod?: string;
}
