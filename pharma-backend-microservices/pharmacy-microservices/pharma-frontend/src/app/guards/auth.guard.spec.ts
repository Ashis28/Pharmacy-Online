/**
 * Tests for authGuard, adminGuard, guestGuard.
 *
 * The guards use Angular's inject() which we stub via the @angular/core mock.
 * We call the guard functions directly, injecting mock services via
 * the module-level variables that the mock inject() returns.
 */

// Set up mock services BEFORE importing the guards
const mockAuthService = {
  isLoggedIn: jest.fn(),
  isAdmin:    jest.fn(),
};
const mockRouter = { navigate: jest.fn() };

// Override inject() in our @angular/core mock to return the right service
const angularCore = require('@angular/core');
angularCore.inject = (token: any) => {
  const name = (token?.name ?? '') as string;
  if (name === 'AuthService') return mockAuthService;
  if (name === 'Router')      return mockRouter;
  return {};
};

import { authGuard, adminGuard, guestGuard } from './auth.guard';

describe('Route Guards', () => {

  beforeEach(() => jest.clearAllMocks());

  // ── authGuard ────────────────────────────────────────────────────────────
  describe('authGuard', () => {
    it('returns true when user is logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      expect((authGuard as any)()).toBe(true);
    });

    it('redirects to /login and returns false when not logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      expect((authGuard as any)()).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  // ── adminGuard ───────────────────────────────────────────────────────────
  describe('adminGuard', () => {
    it('returns true when logged in AND admin', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.isAdmin.mockReturnValue(true);
      expect((adminGuard as any)()).toBe(true);
    });

    it('redirects to /home when logged in but not admin', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.isAdmin.mockReturnValue(false);
      expect((adminGuard as any)()).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
    });

    it('redirects to /login when not logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      mockAuthService.isAdmin.mockReturnValue(false);
      expect((adminGuard as any)()).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  // ── guestGuard ───────────────────────────────────────────────────────────
  describe('guestGuard', () => {
    it('returns true when user is NOT logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(false);
      expect((guestGuard as any)()).toBe(true);
    });

    it('redirects admin to /admin/dashboard when already logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.isAdmin.mockReturnValue(true);
      expect((guestGuard as any)()).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
    });

    it('redirects customer to /home when already logged in', () => {
      mockAuthService.isLoggedIn.mockReturnValue(true);
      mockAuthService.isAdmin.mockReturnValue(false);
      expect((guestGuard as any)()).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
    });
  });
});
