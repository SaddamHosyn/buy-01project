import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Auth } from '../services/auth';

/**
 * Modern functional route guard for authentication
 * Protects routes that require user to be logged in
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);
  
  if (authService.isAuthenticated()) {
    return true;
  }
  
  // Store the attempted URL for redirecting after login
  const returnUrl = state.url;
  
  // Navigate to login page
  router.navigate(['/auth/login'], { 
    queryParams: { returnUrl } 
  });
  
  return false;
};

