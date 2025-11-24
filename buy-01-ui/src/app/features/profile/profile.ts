import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Auth } from '../../core/services/auth';
import { NotificationService } from '../../core/services/notification.service';
import { validateFile, ValidationPresets } from '../../core/validators/file-upload.validator';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatTabsModule, MatDividerModule, MatTooltipModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly notification = inject(NotificationService);

  readonly isLoading = signal(false);
  readonly selectedFile = signal<File | null>(null);
  readonly imagePreview = signal<string | null>(null);
  readonly uploadError = signal('');
  readonly currentUser = this.authService.currentUser;
  
  readonly showPasswordFields = signal(false);

  // --- Forms ---

   readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: [{value: '', disabled: true}] 
  });

  readonly passwordForm: FormGroup = this.fb.group({
    currentPassword: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  }, { validators: this.passwordMatchValidator });

  readonly displayAvatar = computed(() => {
    return this.imagePreview() || this.currentUser()?.avatarUrl || null;
  });

  ngOnInit(): void {
    const user = this.currentUser();
    if (user) {
      this.profileForm.patchValue({ name: user.name, email: user.email });
      if (user.avatarUrl) {
        this.imagePreview.set(user.avatarUrl);
      }
    } else {
      this.router.navigate(['/auth/login']);
    }
  }

  goBack(): void {
    this.location.back();
  }

  togglePasswordFields(): void {
    this.showPasswordFields.update(v => !v);
    if (!this.showPasswordFields()) this.passwordForm.reset();
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword && confirmPassword && newPassword !== confirmPassword 
      ? { passwordMismatch: true } 
      : null;
  }

  // --- Actions ---

   saveProfile(): void {
    if (this.profileForm.invalid) {
      // The form is invalid, so just return and let the HTML show the error.
      return;
    }
    if (!this.profileForm.dirty) {
      this.notification.info('No changes to save.');
      return;
    }

    this.isLoading.set(true);
    const newName = this.profileForm.get('name')?.value;

    this.authService.updateName(newName).subscribe({
      next: (updatedUser) => {
        this.isLoading.set(false);
        this.notification.success('Name updated successfully!');
        this.profileForm.markAsPristine();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.notification.error('Failed to update name. ' + (err.error?.message || ''));
        this.profileForm.patchValue({ name: this.currentUser()?.name });
      }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const { currentPassword, newPassword } = this.passwordForm.value;

    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.notification.success('Password changed successfully!');
        this.passwordForm.reset();
        this.showPasswordFields.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        const errorMessage = err.error?.message || '';
        if (errorMessage.includes('Incorrect current password')) {
          this.passwordForm.get('currentPassword')?.setErrors({ incorrect: true });
          this.notification.error('Incorrect current password.');
        } else {
          this.notification.error('Failed to change password. Please try again.');
        }
      }
    });
  }

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    const validation = validateFile(file, ValidationPresets.AVATAR);

    if (!validation.valid) {
      const errorMsg = (validation.errors && validation.errors.length > 0) ? validation.errors[0] : 'Invalid file';
      this.notification.error(errorMsg);
      input.value = '';
      return;
    }

    this.selectedFile.set(file);
    const reader = new FileReader();
    reader.onload = (e) => this.imagePreview.set(e.target?.result as string);
    reader.readAsDataURL(file);

    this.uploadAvatar();
  }

  uploadAvatar(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.isLoading.set(true);
    this.authService.uploadAvatar(file).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.notification.success('Avatar updated successfully!');
        this.selectedFile.set(null);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.notification.error('Failed to upload avatar. ' + (err.message || ''));
        this.imagePreview.set(this.currentUser()?.avatarUrl || null);
      }
    });
  }

   /* FIXED: This method dynamically gets the required length.
   */
  getErrorMessage(controlName: string, form?: FormGroup): string {
    // If no form is passed (e.g. from template), try to auto-detect
    let targetForm = form;
    
    if (!targetForm) {
       if (this.profileForm.contains(controlName)) {
         targetForm = this.profileForm;
       } else if (this.passwordForm.contains(controlName)) {
         targetForm = this.passwordForm;
       }
    }

    if (!targetForm) return ''; // Safety check if form not found

    const control = targetForm.get(controlName);
    if (!control) return '';

    if (control.hasError('required')) {
        return `${this.getFieldLabel(controlName)} is required`;
    }
    if (control.hasError('minlength')) {
        const requiredLength = control.errors?.['minlength']?.requiredLength;
        return `Must be at least ${requiredLength} characters`;
    }
    if (control.hasError('passwordMismatch')) {
        return 'Passwords do not match';
    }
    if (control.hasError('incorrect')) {
        return 'Incorrect password';
    }
    
    return '';
  }


  private getFieldLabel(controlName: string): string {
    const nameMap: { [key: string]: string } = {
      name: 'Full Name',
      email: 'Email',
      currentPassword: 'Current password',
      newPassword: 'New password',
      confirmPassword: 'Confirm password',
    };
    return nameMap[controlName] || controlName;
  }
}
