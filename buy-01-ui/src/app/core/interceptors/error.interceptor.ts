import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../services/notification.service';
import { Auth } from '../services/auth';

/**
 * HTTP Error Interceptor
 * Intercepts HTTP errors and handles them appropriately
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(Auth);
  const notificationService = inject(NotificationService);
  
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle specific HTTP error codes
      switch (error.status) {
        case 401:
          // Unauthorized - logout and redirect to login
          authService.logout();
          router.navigate(['/auth/login']);
          notificationService.authError();
          break;
        
        case 403:
          // Forbidden - permission denied
          notificationService.permissionError();
          break;
        
        case 404:
          // Not found - show specific message if available
          if (error.error?.message) {
            notificationService.error(error.error.message);
          }
          break;
        
        case 0:
          // Network error
          notificationService.networkError();
          break;
        
        case 500:
        case 503:
          // Server errors - handled by global error handler
          // Don't show duplicate notifications
          break;
        
        default:
          // Other errors - let global handler deal with it
          break;
      }
      
      // Re-throw the error for further handling
      return throwError(() => error);
    })
  );
};
