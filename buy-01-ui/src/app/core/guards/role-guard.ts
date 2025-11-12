import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Auth, User } from '../services/auth';

/**
 * Modern functional route guard for role-based access control
 * Checks if user has required role to access a route
 * 
 * Usage in routes:
 * canActivate: [roleGuard],
 * data: { roles: ['SELLER', 'ADMIN'] }
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);
  
  const currentUser = authService.currentUser();
  const requiredRoles = route.data['roles'] as User['role'][];
  
  // Check if user is authenticated
  if (!currentUser) {
    router.navigate(['/auth/login']);
    return false;
  }
  
  // Check if user has required role
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = requiredRoles.includes(currentUser.role);
    
    if (!hasRole) {
      // Redirect to unauthorized page or home
      router.navigate(['/products']);
      return false;
    }
  }
  
  return true;
};

