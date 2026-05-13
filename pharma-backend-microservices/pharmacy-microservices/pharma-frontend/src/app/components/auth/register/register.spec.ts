import { RegisterComponent } from './register';
import { of, throwError } from 'rxjs';

// ── Stubs ──────────────────────────────────────────────────────────────────
const mockAuth   = { signup: jest.fn() } as any;
const mockRouter = { navigate: jest.fn() } as any;
const mockRoute  = { snapshot: { data: {} } } as any;

function makeComponent(isAdmin = false): RegisterComponent {
  const comp = new RegisterComponent();
  (comp as any).auth   = mockAuth;
  (comp as any).router = mockRouter;
  (comp as any).route  = mockRoute;
  comp.isAdmin = isAdmin;
  comp.ngOnInit();
  return comp;
}

// ── Tests ──────────────────────────────────────────────────────────────────
describe('RegisterComponent', () => {

  beforeEach(() => jest.clearAllMocks());

  // ── passwordStrength getter ──────────────────────────────────────────────
  describe('passwordStrength getter', () => {
    it('returns null for empty password', () => {
      const comp = makeComponent();
      comp.password = '';
      expect(comp.passwordStrength).toBeNull();
    });

    it('returns Too Short for password < 6 chars', () => {
      const comp = makeComponent();
      comp.password = 'abc';
      expect(comp.passwordStrength?.label).toBe('Too Short');
    });

    it('returns Weak for 6-char lowercase only password', () => {
      const comp = makeComponent();
      comp.password = 'abcdef';
      expect(comp.passwordStrength?.label).toBe('Weak');
    });

    it('returns Fair for password with length + uppercase', () => {
      const comp = makeComponent();
      comp.password = 'Abcdefgh';
      expect(comp.passwordStrength?.label).toBe('Fair');
    });

    it('returns Strong for password with length + uppercase + number', () => {
      const comp = makeComponent();
      comp.password = 'Abcdefg1';
      expect(comp.passwordStrength?.label).toBe('Strong');
    });

    it('returns Very Strong for password with all 4 criteria', () => {
      const comp = makeComponent();
      comp.password = 'Abcdefg1!';
      expect(comp.passwordStrength?.label).toBe('Very Strong');
    });

    it('includes tips for missing criteria', () => {
      const comp = makeComponent();
      comp.password = 'abcdefgh'; // no uppercase, no number, no special
      const tips = comp.passwordStrength?.tips ?? [];
      expect(tips).toContain('Add an uppercase letter');
      expect(tips).toContain('Add a number');
    });
  });

  // ── onSubmit validation ──────────────────────────────────────────────────
  describe('onSubmit() validation', () => {
    it('sets error when name is empty', () => {
      const comp = makeComponent();
      comp.name = '';
      comp.email = 'a@a.com';
      comp.password = 'pass123';
      comp.onSubmit();
      expect(comp.error).toBe('Please fill in all fields.');
      expect(mockAuth.signup).not.toHaveBeenCalled();
    });

    it('sets error when passwords do not match', () => {
      const comp = makeComponent();
      comp.name = 'Test';
      comp.email = 'a@a.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'different';
      comp.onSubmit();
      expect(comp.error).toBe('Passwords do not match.');
    });

    it('sets error when password is too short', () => {
      const comp = makeComponent();
      comp.name = 'Test';
      comp.email = 'a@a.com';
      comp.password = 'abc';
      comp.confirmPassword = 'abc';
      comp.onSubmit();
      expect(comp.error).toBe('Password must be at least 6 characters.');
    });

    it('sets error when admin token is empty on admin registration', () => {
      const comp = makeComponent(true);
      comp.name = 'Admin';
      comp.email = 'a@a.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'pass123';
      comp.adminToken = '';
      comp.onSubmit();
      expect(comp.error).toBe('Admin registration token is required.');
      expect(mockAuth.signup).not.toHaveBeenCalled();
    });

    it('does NOT require admin token for customer registration', () => {
      const comp = makeComponent(false);
      comp.name = 'Cust';
      comp.email = 'c@c.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(of('User registered successfully'));
      comp.onSubmit();
      expect(mockAuth.signup).toHaveBeenCalled();
    });
  });

  // ── onSubmit success ─────────────────────────────────────────────────────
  describe('onSubmit() success', () => {
    it('sets success message and navigates to login', (done) => {
      jest.useFakeTimers();
      const comp = makeComponent();
      comp.name = 'Test';
      comp.email = 't@t.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(of('User registered successfully'));

      comp.onSubmit();
      expect(comp.success).toContain('Account created successfully');
      expect(comp.loading).toBe(false);

      jest.runAllTimers();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
      jest.useRealTimers();
      done();
    });

    it('sends adminToken in payload for admin registration', () => {
      const comp = makeComponent(true);
      comp.name = 'Admin';
      comp.email = 'a@a.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'pass123';
      comp.adminToken = 'PHARMA-ADMIN-2026';
      mockAuth.signup.mockReturnValue(of('User registered successfully'));

      comp.onSubmit();
      expect(mockAuth.signup).toHaveBeenCalledWith(
        expect.objectContaining({ adminToken: 'PHARMA-ADMIN-2026', roles: ['ADMIN'] })
      );
    });

    it('does NOT send adminToken for customer registration', () => {
      const comp = makeComponent(false);
      comp.name = 'Cust';
      comp.email = 'c@c.com';
      comp.password = 'pass123';
      comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(of('User registered successfully'));

      comp.onSubmit();
      const call = mockAuth.signup.mock.calls[0][0];
      expect(call.adminToken).toBeUndefined();
      expect(call.roles).toEqual(['CUSTOMER']);
    });
  });

  // ── onSubmit error handling ──────────────────────────────────────────────
  describe('onSubmit() error handling', () => {
    it('shows connection error on status 0', () => {
      const comp = makeComponent();
      comp.name = 'T'; comp.email = 't@t.com';
      comp.password = 'pass123'; comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(throwError(() => ({ status: 0 })));
      comp.onSubmit();
      expect(comp.error).toContain('Cannot connect to server');
    });

    it('shows duplicate email error on status 409', () => {
      const comp = makeComponent();
      comp.name = 'T'; comp.email = 't@t.com';
      comp.password = 'pass123'; comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(throwError(() => ({ status: 409 })));
      comp.onSubmit();
      expect(comp.error).toContain('Email already registered');
    });

    it('shows "Invalid token." for token-related 400 error', () => {
      const comp = makeComponent(true);
      comp.name = 'A'; comp.email = 'a@a.com';
      comp.password = 'pass123'; comp.confirmPassword = 'pass123';
      comp.adminToken = 'wrong';
      mockAuth.signup.mockReturnValue(throwError(() => ({
        status: 400,
        error: JSON.stringify({ message: 'Invalid token.', status: 400 })
      })));
      comp.onSubmit();
      expect(comp.error).toBe('Invalid token.');
    });

    it('sets loading to false after error', () => {
      const comp = makeComponent();
      comp.name = 'T'; comp.email = 't@t.com';
      comp.password = 'pass123'; comp.confirmPassword = 'pass123';
      mockAuth.signup.mockReturnValue(throwError(() => ({ status: 500, error: {} })));
      comp.onSubmit();
      expect(comp.loading).toBe(false);
    });
  });

  // ── role getter ──────────────────────────────────────────────────────────
  describe('role getter', () => {
    it('returns ADMIN when isAdmin is true', () => {
      const comp = makeComponent(true);
      expect(comp.role).toBe('ADMIN');
    });

    it('returns CUSTOMER when isAdmin is false', () => {
      const comp = makeComponent(false);
      expect(comp.role).toBe('CUSTOMER');
    });
  });
});
