import { PaymentService } from './payment.service';
import { of } from 'rxjs';

const mockHttp = { post: jest.fn(), get: jest.fn() } as any;

function makeService(): PaymentService {
  return new PaymentService(mockHttp);
}

describe('PaymentService', () => {

  beforeEach(() => jest.clearAllMocks());

  it('processPayment() calls POST /api/payments/process', () => {
    mockHttp.post.mockReturnValue(of({ status: 'SUCCESS', transactionId: 'TXN-001' }));
    const req = { orderId: 1, customerId: 2, amount: 100, paymentMethod: 'UPI' };
    makeService().processPayment(req as any).subscribe(res => {
      expect(res.status).toBe('SUCCESS');
    });
    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/payments/process'), req
    );
  });

  it('getPaymentByOrderId() calls GET /api/payments/order/{id}', () => {
    mockHttp.get.mockReturnValue(of({ orderId: 5, status: 'SUCCESS' }));
    makeService().getPaymentByOrderId(5).subscribe(res => {
      expect(res.orderId).toBe(5);
    });
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/payments/order/5'));
  });

  it('processPayment() passes all payment methods correctly', () => {
    const methods = ['UPI', 'CARD', 'NET_BANKING', 'CASH'];
    methods.forEach(method => {
      mockHttp.post.mockReturnValue(of({ status: 'SUCCESS' }));
      const req = { orderId: 1, customerId: 1, amount: 50, paymentMethod: method };
      makeService().processPayment(req as any).subscribe();
      expect(mockHttp.post).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({ paymentMethod: method })
      );
    });
  });
});
