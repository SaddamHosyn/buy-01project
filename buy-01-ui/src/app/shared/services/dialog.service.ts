import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { ConfirmDialog, ConfirmDialogData } from '../components/confirm-dialog/confirm-dialog';

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private readonly dialog = inject(MatDialog);

  /**
   * Open a confirmation dialog
   */
  confirm(data: ConfirmDialogData): Observable<boolean> {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data,
      disableClose: false,
      autoFocus: true
    });

    return dialogRef.afterClosed();
  }

  /**
   * Show delete confirmation dialog
   */
  confirmDelete(itemName: string = 'this item'): Observable<boolean> {
    return this.confirm({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete ${itemName}? This action cannot be undone.`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'delete_forever'
    });
  }

  /**
   * Show discard changes confirmation dialog
   */
  confirmDiscard(): Observable<boolean> {
    return this.confirm({
      title: 'Discard Changes?',
      message: 'You have unsaved changes. Are you sure you want to discard them?',
      confirmText: 'Discard',
      cancelText: 'Keep Editing',
      type: 'warn',
      icon: 'warning'
    });
  }

  /**
   * Show logout confirmation dialog
   */
  confirmLogout(): Observable<boolean> {
    return this.confirm({
      title: 'Confirm Logout',
      message: 'Are you sure you want to log out?',
      confirmText: 'Logout',
      cancelText: 'Cancel',
      type: 'info',
      icon: 'logout'
    });
  }

  /**
   * Show generic warning dialog
   */
  confirmWarning(message: string, title: string = 'Warning'): Observable<boolean> {
    return this.confirm({
      title,
      message,
      confirmText: 'Continue',
      cancelText: 'Cancel',
      type: 'warn',
      icon: 'warning'
    });
  }
}
