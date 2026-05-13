import { OrderService } from './order.service';
import { of } from 'rxjs';

const mockHttp = { post: jest.fn(), get: jest.fn(), put: jest.fn() } as any;

function makeService(): OrderService {
  return new OrderService(mockHttp);
}

describe('OrderService', () => {

  beforeEach(() => jest.clearAllMocks());

  it('placeOrder() calls POST /api/orders with X-Auth-User header', () => {
    mockHttp.post.mockReturnValue(of({ id: 1 }));
    const req = { customerId: 1, deliveryAddress: '123 St', items: [] };
    makeService().placeOrder(req as any, 'user@test.com').subscribe();
    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/orders'),
      req,
      expect.objectContaining({ headers: expect.objectContaining({ 'X-Auth-User': 'user@test.com' }) })
    );
  });

  it('getOrderById() calls GET /api/orders/{id}', () => {
    mockHttp.get.mockReturnValue(of({ id: 10 }));
    makeService().getOrderById(10).subscribe(o => expect(o.id).toBe(10));
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/orders/10'));
  });

  it('getOrdersByCustomer() calls GET /api/orders/customer/{id}', () => {
    mockHttp.get.mockReturnValue(of([]));
    makeService().getOrdersByCustomer(5).subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/orders/customer/5'));
  });

  it('getAllOrders() calls GET /api/orders', () => {
    mockHttp.get.mockReturnValue(of([]));
    makeService().getAllOrders().subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/orders'));
  });

  it('markOrderAsPaid() calls PUT /api/orders/{id}/pay', () => {
    mockHttp.put.mockReturnValue(of({ id: 3, status: 'PAID' }));
    makeService().markOrderAsPaid(3).subscribe(o => expect(o.status).toBe('PAID'));
    expect(mockHttp.put).toHaveBeenCalledWith(
      expect.stringContaining('/api/orders/3/pay'), {}
    );
  });

  it('updateOrderStatus() calls PUT /api/orders/{id}/status', () => {
    mockHttp.put.mockReturnValue(of({ id: 4, status: 'SHIPPED' }));
    makeService().updateOrderStatus(4, 'SHIPPED').subscribe();
    expect(mockHttp.put).toHaveBeenCalledWith(
      expect.stringContaining('/api/orders/4/status'),
      { status: 'SHIPPED' }
    );
  });

  it('getOrderCount() calls GET /api/orders/count', () => {
    mockHttp.get.mockReturnValue(of(42));
    makeService().getOrderCount().subscribe(c => expect(c).toBe(42));
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/orders/count'));
  });

  it('getRevenue() calls GET with from/to params', () => {
    mockHttp.get.mockReturnValue(of({ revenue: 5000 }));
    makeService().getRevenue('2026-01-01', '2026-01-31').subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(
      expect.stringContaining('from=2026-01-01')
    );
  });
});
