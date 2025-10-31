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
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatDividerModule,
    MatTooltipModule
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
  
  // Signals for state management
  readonly isLoading = signal<boolean>(false);
  readonly selectedFile = signal<File | null>(null);
  readonly imagePreview = signal<string | null>(null);
  readonly uploadError = signal<string>('');
  readonly currentUser = this.authService.currentUser;
  readonly showPasswordFields = signal<boolean>(false);
  readonly showEmailFields = signal<boolean>(false);
  
  // Computed - current avatar or preview
  readonly displayAvatar = computed(() => {
    return this.imagePreview() || this.currentUser()?.avatarUrl || null;
  });
  
  // Profile form
  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]]
  });
  
  // Change Password form
  readonly passwordForm: FormGroup = this.fb.group({
    currentPassword: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  }, { validators: this.passwordMatchValidator });
  
  // Change Email form
  readonly emailForm: FormGroup = this.fb.group({
    newEmail: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });
  
  ngOnInit(): void {
    // Load current user data
    const user = this.currentUser();
    if (user) {
      this.profileForm.patchValue({
        name: user.name,
        email: user.email
      });
      
      // Set current avatar if exists
      if (user.avatarUrl) {
        this.imagePreview.set(user.avatarUrl);
      }
    } else {
      // Redirect to login if not authenticated
      this.router.navigate(['/auth/login']);
    }
  }
  
  /**
   * Navigate back to previous page
   */
  goBack(): void {
    this.location.back();
  }
  
  /**
   * Custom validator for password match
   */
  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }
  
  /**
   * Handle file selection with validation
   */
  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    
    // Reset previous state
    this.uploadError.set('');
    this.selectedFile.set(null);
    
    if (!file) {
      return;
    }
    
    // Validate file using validator
    const validation = validateFile(file, ValidationPresets.AVATAR);
    
    if (!validation.valid) {
      this.uploadError.set(validation.errors[0]);
      this.notification.fileUploadError(file.name, validation.errors[0]);
      return;
    }
    
    // Set selected file
    this.selectedFile.set(file);
    
    // Generate preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imagePreview.set(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  }
  
  /**
   * Remove avatar
   */
  removeAvatar(): void {
    this.selectedFile.set(null);
    this.imagePreview.set(null);
    this.uploadError.set('');
    
    // Reset file input
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
  
  /**
   * Save profile changes
   */
  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    
    this.isLoading.set(true);
    
    // Prepare update data
    const updateData = {
      name: this.profileForm.value.name,
      email: this.profileForm.value.email,
      avatarUrl: this.imagePreview()
    };
    
    // Simulate API call
    setTimeout(() => {
      // Update user data using auth service method
      this.authService.updateUser(updateData);
      
      this.isLoading.set(false);
      this.notification.success('Profile updated successfully!');
    }, 1000);
  }
  
  /**
   * Toggle password change section
   */
  togglePasswordFields(): void {
    this.showPasswordFields.update(v => !v);
    if (!this.showPasswordFields()) {
      this.passwordForm.reset();
    }
  }
  
  /**
   * Toggle email change section
   */
  toggleEmailFields(): void {
    this.showEmailFields.update(v => !v);
    if (!this.showEmailFields()) {
      this.emailForm.reset();
    }
  }
  
  /**
   * Change password
   */
  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    
    // Check password match
    if (this.passwordForm.hasError('passwordMismatch')) {
      this.notification.error('Passwords do not match');
      return;
    }
    
    this.isLoading.set(true);
    
    const currentPassword = this.passwordForm.value.currentPassword;
    const newPassword = this.passwordForm.value.newPassword;
    
    // Simulate API call
    setTimeout(() => {
      // In production, verify current password and update
      // For now, just simulate success
      
      this.isLoading.set(false);
      this.notification.success('Password changed successfully!');
      this.passwordForm.reset();
      this.showPasswordFields.set(false);
    }, 1000);
  }
  
  /**
   * Change email
   */
  changeEmail(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }
    
    this.isLoading.set(true);
    
    const newEmail = this.emailForm.value.newEmail;
    const password = this.emailForm.value.password;
    
    // Check if email already exists
    const registeredUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
    const emailExists = registeredUsers.some((u: any) => u.email === newEmail && u.id !== this.currentUser()?.id);
    
    if (emailExists) {
      this.isLoading.set(false);
      this.notification.error('Email already exists');
      return;
    }
    
    // Simulate API call
    setTimeout(() => {
      // Update email
      this.authService.updateUser({ email: newEmail });
      
      // Update profile form
      this.profileForm.patchValue({ email: newEmail });
      
      // Update in registered users list
      const users = JSON.parse(localStorage.getItem('registered_users') || '[]');
      const updatedUsers = users.map((u: any) => 
        u.id === this.currentUser()?.id ? { ...u, email: newEmail } : u
      );
      localStorage.setItem('registered_users', JSON.stringify(updatedUsers));
      
      this.isLoading.set(false);
      this.notification.success('Email changed successfully!');
      this.emailForm.reset();
      this.showEmailFields.set(false);
    }, 1000);
  }
  
  /**
   * Get form control error message
   */
  getErrorMessage(controlName: string): string {
    // Check all forms for the control
    let control = this.profileForm.get(controlName);
    if (!control) control = this.passwordForm.get(controlName);
    if (!control) control = this.emailForm.get(controlName);
    
    if (!control) return '';
    
    if (control.hasError('required')) {
      return `${this.formatControlName(controlName)} is required`;
    }
    if (control.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `${this.formatControlName(controlName)} must be at least ${minLength} characters`;
    }
    if (control.hasError('email')) {
      return 'Please enter a valid email address';
    }
    
    return '';
  }
  
  /**
   * Format control name for display
   */
  private formatControlName(name: string): string {
    const nameMap: { [key: string]: string } = {
      name: 'Name',
      email: 'Email',
      currentPassword: 'Current password',
      newPassword: 'New password',
      confirmPassword: 'Confirm password',
      newEmail: 'New email',
      password: 'Password'
    };
    
    return nameMap[name] || name;
  }
}
