import { Component, inject } from '@angular/core';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'warn' | 'danger' | 'info';
  icon?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>
      @if (data.icon) {
        <mat-icon [class]="'dialog-icon ' + data.type">{{ data.icon }}</mat-icon>
      }
      {{ data.title }}
    </h2>
    
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        {{ data.cancelText || 'Cancel' }}
      </button>
      <button 
        mat-raised-button 
        [color]="data.type === 'danger' ? 'warn' : 'primary'"
        (click)="onConfirm()"
        cdkFocusInitial>
        {{ data.confirmText || 'Confirm' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 0;
    }

    .dialog-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      
      &.warn {
        color: #ff9800;
      }
      
      &.danger {
        color: #f44336;
      }
      
      &.info {
        color: #2196f3;
      }
    }

    mat-dialog-content {
      padding: 20px 0;
      min-width: 300px;
      
      p {
        margin: 0;
        color: rgba(0, 0, 0, 0.7);
        line-height: 1.5;
      }
    }

    mat-dialog-actions {
      padding: 12px 0 0 0;
      gap: 8px;
    }
  `]
})
export class ConfirmDialog {
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialog>);

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
