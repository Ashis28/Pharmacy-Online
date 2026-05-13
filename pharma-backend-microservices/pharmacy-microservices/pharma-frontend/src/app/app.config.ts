/**
 * app.config.ts — Bootstraps and configures the entire Angular application.
 *
 * WHAT IS THIS FILE?
 *   This is the application configuration file. It tells Angular what
 *   features and services to set up when the app starts. Think of it as
 *   the "startup settings" for the whole application.
 *
 * WHAT EACH PROVIDER DOES:
 *
 *   provideZoneChangeDetection({ eventCoalescing: true })
 *     → Sets up Angular's change detection (how Angular knows when to update the UI).
 *     → eventCoalescing: true = batches multiple events together for better performance.
 *     → Without this, Angular would re-check the entire UI after every tiny event.
 *
 *   provideRouter(routes)
 *     → Registers all the routes defined in app.routes.ts.
 *     → This is what makes /home show HomeComponent, /login show LoginComponent, etc.
 *     → Without this, navigation wouldn't work at all.
 *
 *   provideHttpClient(withInterceptors([authInterceptor]))
 *     → Sets up Angular's HTTP client for making API calls.
 *     → withInterceptors([authInterceptor]) registers our auth interceptor.
 *     → The interceptor automatically adds "Authorization: Bearer <token>"
 *       to every HTTP request made anywhere in the app.
 *     → Without this, every service would need to manually add the token.
 *
 * WHERE IS THIS USED?
 *   main.ts calls bootstrapApplication(App, appConfig) which starts the app
 *   using this configuration.
 */

import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Change detection — how Angular knows when to update the UI
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Router — maps URLs to components (uses routes from app.routes.ts)
    provideRouter(routes),

    // HTTP client — enables making API calls, with auth interceptor attached
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
