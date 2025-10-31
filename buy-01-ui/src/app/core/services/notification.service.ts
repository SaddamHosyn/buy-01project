import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

/**
 * Centralized Notification Service
 * Provides consistent toast messages across the application
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);
  
  // Default configuration
  private readonly defaultConfig: MatSnackBarConfig = {
    duration: 3000,
    horizontalPosition: 'end',
    verticalPosition: 'top'
  };
  
  /**
   * Show success message (green)
   */
  success(message: string, duration?: number): void {
    this.snackBar.open(message, 'Close', {
      ...this.defaultConfig,
      duration: duration || this.defaultConfig.duration,
      panelClass: ['success-snackbar']
    });
  }
  
  /**
   * Show error message (red)
   */
  error(message: string, duration?: number): void {
    this.snackBar.open(message, 'Close', {
      ...this.defaultConfig,
      duration: duration || 5000, // Errors stay longer
      panelClass: ['error-snackbar']
    });
  }
  
  /**
   * Show warning message (orange)
   */
  warning(message: string, duration?: number): void {
    this.snackBar.open(message, 'Close', {
      ...this.defaultConfig,
      duration: duration || 4000,
      panelClass: ['warning-snackbar']
    });
  }
  
  /**
   * Show info message (blue)
   */
  info(message: string, duration?: number): void {
    this.snackBar.open(message, 'Close', {
      ...this.defaultConfig,
      duration: duration || this.defaultConfig.duration,
      panelClass: ['info-snackbar']
    });
  }
  
  /**
   * Show custom message with action
   */
  custom(message: string, action: string, config?: MatSnackBarConfig): void {
    this.snackBar.open(message, action, {
      ...this.defaultConfig,
      ...config
    });
  }
  
  /**
   * Dismiss all notifications
   */
  dismiss(): void {
    this.snackBar.dismiss();
  }
  
  /**
   * Show file upload validation error
   */
  fileUploadError(fileName: string, reason: string): void {
    this.error(`${fileName}: ${reason}`);
  }
  
  /**
   * Show file upload success
   */
  fileUploadSuccess(count: number, failedCount?: number): void {
    if (failedCount && failedCount > 0) {
      this.warning(
        `Successfully uploaded ${count} file(s), ${failedCount} failed`
      );
    } else {
      this.success(`Successfully uploaded ${count} file(s)`);
    }
  }
  
  /**
   * Show network error message
   */
  networkError(): void {
    this.error('Network error. Please check your connection and try again.');
  }
  
  /**
   * Show authentication error
   */
  authError(): void {
    this.error('Authentication required. Please login to continue.');
  }
  
  /**
   * Show permission error
   */
  permissionError(): void {
    this.error('You do not have permission to perform this action.');
  }
  
  /**
   * Show validation error
   */
  validationError(message: string): void {
    this.warning(message);
  }
}
