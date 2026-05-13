/**
 * authInterceptor — Automatically attaches the JWT token to every HTTP request.
 *
 * WHAT IS AN INTERCEPTOR?
 *   An interceptor sits between your code and the HTTP layer.
 *   Every time your app makes an HTTP request (GET, POST, PUT, DELETE),
 *   the interceptor runs first and can modify the request before it's sent.
 *
 * WHY DO WE NEED THIS?
 *   Without this interceptor, every service method would need to manually
 *   add the Authorization header:
 *     this.http.get(url, { headers: { Authorization: `Bearer ${token}` } })
 *
 *   With the interceptor, you just write:
 *     this.http.get(url)
 *   ...and the token is added automatically. Much cleaner!
 *
 * HOW IT WORKS:
 *   1. Request comes in (e.g. GET /api/orders)
 *   2. Interceptor reads the JWT token from AuthService
 *   3. If token exists: clones the request and adds "Authorization: Bearer <token>"
 *   4. If no token: sends the request as-is (for public endpoints like login)
 *   5. Passes the (possibly modified) request to the next handler
 *   6. If the response is 401 Unauthorized: logs the user out and redirects to /login
 *
 * WHERE IT'S REGISTERED:
 *   app.config.ts → provideHttpClient(withInterceptors([authInterceptor]))
 *   This registers it globally — it runs for ALL HTTP requests in the app.
 *
 * WHAT IS req.clone()?
 *   HTTP requests are immutable in Angular. You can't modify them directly.
 *   clone() creates a copy with the changes you specify.
 */

import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  // Get the current JWT token (null if not logged in)
  const token = authService.getToken();

  // Clone the request and add the Authorization header if we have a token
  // If no token (guest user), send the original request unchanged
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  // Pass the request to the next handler (the actual HTTP call)
  // and catch any errors in the response
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // 401 = Unauthorized — token is expired or invalid
        // Log the user out and send them to the login page
        // This handles the case where a token expires mid-session
        authService.logout();
        router.navigate(['/login']);
      }
      // Re-throw the error so the component's error handler can also process it
      return throwError(() => error);
    })
  );
};
