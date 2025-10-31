import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '../services/auth';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

/**
 * Modern functional HTTP interceptor for adding auth token to requests
 * This is the new Angular way (no classes needed!)
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Auth);
  const router = inject(Router);
  const token = authService.getToken();
  
  // Clone request and add authorization header if token exists
  const authReq = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;
  
  // Handle the request and catch auth errors
  return next(authReq).pipe(
    catchError(error => {
      // Handle 401 Unauthorized - redirect to login
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/auth/login']);
      }
      
      return throwError(() => error);
    })
  );
};

