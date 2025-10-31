import { ErrorHandler, Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from '../services/notification.service';

/**
 * Global Error Handler
 * Catches all unhandled errors and provides user-friendly messages
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private readonly notificationService = inject(NotificationService);
  
  handleError(error: Error | HttpErrorResponse): void {
    console.error('Global Error Handler:', error);
    
    if (error instanceof HttpErrorResponse) {
      // HTTP errors
      this.handleHttpError(error);
    } else {
      // Client-side errors
      this.handleClientError(error);
    }
  }
  
  /**
   * Handle HTTP errors
   */
  private handleHttpError(error: HttpErrorResponse): void {
    let message = 'An unexpected error occurred';
    
    switch (error.status) {
      case 0:
        // Network error
        message = 'Unable to connect to the server. Please check your internet connection.';
        break;
      
      case 400:
        message = error.error?.message || 'Invalid request. Please check your input.';
        break;
      
      case 401:
        message = 'You are not authenticated. Please login to continue.';
        break;
      
      case 403:
        message = 'You do not have permission to access this resource.';
        break;
      
      case 404:
        message = error.error?.message || 'The requested resource was not found.';
        break;
      
      case 409:
        message = error.error?.message || 'A conflict occurred. This resource may already exist.';
        break;
      
      case 422:
        message = error.error?.message || 'Validation failed. Please check your input.';
        break;
      
      case 500:
        message = 'Internal server error. Please try again later.';
        break;
      
      case 503:
        message = 'Service temporarily unavailable. Please try again later.';
        break;
      
      default:
        if (error.status >= 500) {
          message = 'Server error. Please try again later.';
        } else if (error.status >= 400) {
          message = error.error?.message || 'An error occurred while processing your request.';
        }
    }
    
    this.notificationService.error(message);
  }
  
  /**
   * Handle client-side errors
   */
  private handleClientError(error: Error): void {
    let message = 'An unexpected error occurred';
    
    // Check for specific error types
    if (error.message.includes('Network')) {
      message = 'Network error. Please check your connection.';
    } else if (error.message.includes('timeout')) {
      message = 'Request timed out. Please try again.';
    } else if (error.message.includes('quota')) {
      message = 'Storage quota exceeded. Please clear some space.';
    } else if (error.message.includes('permission')) {
      message = 'Permission denied. Please check your settings.';
    } else {
      // Generic error
      message = 'Something went wrong. Please try again.';
    }
    
    this.notificationService.error(message);
  }
}
