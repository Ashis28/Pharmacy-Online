import { AuthService } from './auth.service';
import { of, throwError } from 'rxjs';

// ── Stubs ──────────────────────────────────────────────────────────────────
const mockHttp    = { post: jest.fn(), get: jest.fn() } as any;
const mockRouter  = { navigate: jest.fn() } as any;
const mockCart    = { clearCart: jest.fn() } as any;

function makeService(): AuthService {
  const svc = new AuthService(mockHttp, mockRouter, mockCart);
  return svc;
}

// ── Tests ──────────────────────────────────────────────────────────────────
describe('AuthService', () => {

  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  // ── isLoggedIn ───────────────────────────────────────────────────────────
  describe('isLoggedIn()', () => {
    it('returns false when no token in localStorage', () => {
      const svc = makeService();
      expect(svc.isLoggedIn()).toBe(false);
    });

    it('returns true when token exists in localStorage', () => {
      localStorage.setItem('pharma_token', 'some-token');
      const svc = makeService();
      expect(svc.isLoggedIn()).toBe(true);
    });
  });

  // ── isAdmin / isCustomer ─────────────────────────────────────────────────
  describe('isAdmin() and isCustomer()', () => {
    it('isAdmin returns false when no user', () => {
      const svc = makeService();
      expect(svc.isAdmin()).toBe(false);
    });

    it('isCustomer returns false when no user', () => {
      const svc = makeService();
      expect(svc.isCustomer()).toBe(false);
    });

    it('isAdmin returns true for admin user', () => {
      const user = { token: 't', name: 'Admin', email: 'a@a.com', id: 1, roles: ['ADMIN'] };
      localStorage.setItem('pharma_user', JSON.stringify(user));
      const svc = makeService();
      expect(svc.isAdmin()).toBe(true);
    });

    it('isCustomer returns true for customer user', () => {
      const user = { token: 't', name: 'Cust', email: 'c@c.com', id: 2, roles: ['CUSTOMER'] };
      localStorage.setItem('pharma_user', JSON.stringify(user));
      const svc = makeService();
      expect(svc.isCustomer()).toBe(true);
    });

    it('isAdmin returns false for customer user', () => {
      const user = { token: 't', name: 'Cust', email: 'c@c.com', id: 2, roles: ['CUSTOMER'] };
      localStorage.setItem('pharma_user', JSON.stringify(user));
      const svc = makeService();
      expect(svc.isAdmin()).toBe(false);
    });
  });

  // ── getToken ─────────────────────────────────────────────────────────────
  describe('getToken()', () => {
    it('returns null when no token', () => {
      const svc = makeService();
      expect(svc.getToken()).toBeNull();
    });

    it('returns token from localStorage', () => {
      localStorage.setItem('pharma_token', 'abc123');
      const svc = makeService();
      expect(svc.getToken()).toBe('abc123');
    });
  });

  // ── getUserId ────────────────────────────────────────────────────────────
  describe('getUserId()', () => {
    it('returns 1 as default when no user', () => {
      const svc = makeService();
      expect(svc.getUserId()).toBe(1);
    });

    it('returns user id from stored user', () => {
      const user = { token: 't', name: 'U', email: 'u@u.com', id: 42, roles: ['CUSTOMER'] };
      localStorage.setItem('pharma_user', JSON.stringify(user));
      const svc = makeService();
      expect(svc.getUserId()).toBe(42);
    });
  });

  // ── logout ───────────────────────────────────────────────────────────────
  describe('logout()', () => {
    it('clears localStorage, resets user, clears cart, navigates to login', () => {
      localStorage.setItem('pharma_token', 'tok');
      localStorage.setItem('pharma_user', '{"id":1}');
      const svc = makeService();
      svc.logout();
      expect(localStorage.getItem('pharma_token')).toBeNull();
      expect(localStorage.getItem('pharma_user')).toBeNull();
      expect(svc.currentUser()).toBeNull();
      expect(mockCart.clearCart).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  // ── login ────────────────────────────────────────────────────────────────
  describe('login()', () => {
    it('stores token and user in localStorage on success', (done) => {
      const response = { token: 'jwt-tok', name: 'John', id: 5, roles: ['CUSTOMER'] };
      mockHttp.post.mockReturnValue(of(response));
      const svc = makeService();
      svc.login({ email: 'john@test.com', password: 'pass' }).subscribe(() => {
        expect(localStorage.getItem('pharma_token')).toBe('jwt-tok');
        const stored = JSON.parse(localStorage.getItem('pharma_user')!);
        expect(stored.name).toBe('John');
        expect(stored.id).toBe(5);
        expect(svc.currentUser()?.name).toBe('John');
        done();
      });
    });
  });

  // ── signup ───────────────────────────────────────────────────────────────
  describe('signup()', () => {
    it('calls POST /api/auth/signup with the request', () => {
      mockHttp.post.mockReturnValue(of('User registered successfully'));
      const svc = makeService();
      const req = { name: 'Test', email: 't@t.com', password: 'pass', roles: ['CUSTOMER'] };
      svc.signup(req).subscribe();
      expect(mockHttp.post).toHaveBeenCalledWith(
        expect.stringContaining('/api/auth/signup'),
        req,
        expect.any(Object)
      );
    });
  });

  // ── loadUser resilience ──────────────────────────────────────────────────
  describe('loadUser() resilience', () => {
    it('returns null when localStorage has corrupt JSON', () => {
      localStorage.setItem('pharma_user', 'not-valid-json{{{');
      const svc = makeService();
      expect(svc.currentUser()).toBeNull();
    });
  });
});
