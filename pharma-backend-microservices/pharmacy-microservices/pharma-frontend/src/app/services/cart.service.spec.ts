import { CartService } from './cart.service';
import { Medicine } from '../models/medicine.models';

// ── Minimal stubs ──────────────────────────────────────────────────────────
const mockRouter = { navigate: jest.fn() } as any;

function makeService(): CartService {
  const svc = new CartService();
  (svc as any).router = mockRouter;
  return svc;
}

function makeMedicine(overrides: Partial<Medicine> = {}): Medicine {
  return {
    id: 1,
    name: 'Paracetamol',
    price: 50,
    stockQuantity: 10,
    requiresPrescription: false,
    ...overrides,
  };
}

// ── Tests ──────────────────────────────────────────────────────────────────
describe('CartService', () => {

  beforeEach(() => {
    localStorage.clear();
    mockRouter.navigate.mockClear();
  });

  // ── addToCart ────────────────────────────────────────────────────────────
  describe('addToCart()', () => {
    it('redirects to login and returns false when not logged in', () => {
      const svc = makeService();
      const result = svc.addToCart(makeMedicine());
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        expect.objectContaining({ queryParams: expect.any(Object) })
      );
    });

    it('adds medicine to cart when logged in', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      const result = svc.addToCart(makeMedicine());
      expect(result).toBe(true);
      expect(svc.cartItems()).toHaveLength(1);
      expect(svc.cartItems()[0].quantity).toBe(1);
    });

    it('increments quantity when same medicine added again', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      const med = makeMedicine();
      svc.addToCart(med);
      svc.addToCart(med);
      expect(svc.cartItems()).toHaveLength(1);
      expect(svc.cartItems()[0].quantity).toBe(2);
    });

    it('does not exceed stockQuantity when adding repeatedly', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      const med = makeMedicine({ stockQuantity: 2 });
      svc.addToCart(med);
      svc.addToCart(med);
      svc.addToCart(med); // 3rd add — should be ignored
      expect(svc.cartItems()[0].quantity).toBe(2);
    });

    it('adds different medicines as separate items', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1, name: 'Med A' }));
      svc.addToCart(makeMedicine({ id: 2, name: 'Med B' }));
      expect(svc.cartItems()).toHaveLength(2);
    });
  });

  // ── removeFromCart ───────────────────────────────────────────────────────
  describe('removeFromCart()', () => {
    it('removes the correct item', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1 }));
      svc.addToCart(makeMedicine({ id: 2, name: 'Med B' }));
      svc.removeFromCart(1);
      expect(svc.cartItems()).toHaveLength(1);
      expect(svc.cartItems()[0].medicine.id).toBe(2);
    });

    it('does nothing when item not in cart', () => {
      const svc = makeService();
      expect(() => svc.removeFromCart(99)).not.toThrow();
      expect(svc.cartItems()).toHaveLength(0);
    });
  });

  // ── updateQuantity ───────────────────────────────────────────────────────
  describe('updateQuantity()', () => {
    it('updates quantity correctly', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1, stockQuantity: 20 }));
      svc.updateQuantity(1, 5);
      expect(svc.cartItems()[0].quantity).toBe(5);
    });

    it('removes item when quantity set to 0', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1 }));
      svc.updateQuantity(1, 0);
      expect(svc.cartItems()).toHaveLength(0);
    });

    it('removes item when quantity set to negative', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1 }));
      svc.updateQuantity(1, -1);
      expect(svc.cartItems()).toHaveLength(0);
    });

    it('caps quantity at stockQuantity', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1, stockQuantity: 3 }));
      svc.updateQuantity(1, 99);
      expect(svc.cartItems()[0].quantity).toBe(3);
    });
  });

  // ── clearCart ────────────────────────────────────────────────────────────
  describe('clearCart()', () => {
    it('empties the cart', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1 }));
      svc.addToCart(makeMedicine({ id: 2, name: 'B' }));
      svc.clearCart();
      expect(svc.cartItems()).toHaveLength(0);
    });
  });

  // ── computed signals ─────────────────────────────────────────────────────
  describe('cartCount and cartTotal', () => {
    it('cartCount sums all quantities', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1, stockQuantity: 5 }));
      svc.addToCart(makeMedicine({ id: 1, stockQuantity: 5 })); // qty 2
      svc.addToCart(makeMedicine({ id: 2, name: 'B', stockQuantity: 5 })); // qty 1
      expect(svc.cartCount()).toBe(3);
    });

    it('cartTotal calculates price × quantity', () => {
      localStorage.setItem('pharma_token', 'fake-token');
      const svc = makeService();
      svc.addToCart(makeMedicine({ id: 1, price: 100, stockQuantity: 5 }));
      svc.addToCart(makeMedicine({ id: 1, price: 100, stockQuantity: 5 })); // qty 2
      expect(svc.cartTotal()).toBe(200);
    });

    it('cartCount is 0 for empty cart', () => {
      const svc = makeService();
      expect(svc.cartCount()).toBe(0);
    });

    it('cartTotal is 0 for empty cart', () => {
      const svc = makeService();
      expect(svc.cartTotal()).toBe(0);
    });
  });
});
