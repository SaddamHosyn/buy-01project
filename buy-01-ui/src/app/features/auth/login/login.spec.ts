import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { Auth, AuthResponse } from '../../../core/services/auth';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { signal } from '@angular/core';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authServiceSpy: jasmine.SpyObj<Auth>;
  let router: Router;

  const mockAuthResponse: AuthResponse = {
    token: 'mock-token',
    id: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    role: 'CLIENT',
    avatarUrl: null
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('Auth', ['login'], {
      isLoading: signal(false),
      isSeller: () => false,
      isClient: () => true
    });

    await TestBed.configureTestingModule({
      imports: [
        Login,
        ReactiveFormsModule,
        NoopAnimationsModule,
        RouterTestingModule.withRoutes([]),
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule
      ],
      providers: [
        { provide: Auth, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  describe('Component Creation', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with empty form', () => {
      expect(component.loginForm.get('email')?.value).toBe('');
      expect(component.loginForm.get('password')?.value).toBe('');
    });

    it('should have no error message initially', () => {
      expect(component.errorMessage()).toBe('');
    });
  });

  describe('Form Validation', () => {
    it('should require email', () => {
      const emailControl = component.loginForm.get('email');
      emailControl?.setValue('');
      expect(emailControl?.hasError('required')).toBeTrue();
    });

    it('should validate email format', () => {
      const emailControl = component.loginForm.get('email');
      emailControl?.setValue('invalid-email');
      expect(emailControl?.hasError('email')).toBeTrue();

      emailControl?.setValue('valid@email.com');
      expect(emailControl?.hasError('email')).toBeFalse();
    });

    it('should require password', () => {
      const passwordControl = component.loginForm.get('password');
      passwordControl?.setValue('');
      expect(passwordControl?.hasError('required')).toBeTrue();
    });

    it('should require minimum password length', () => {
      const passwordControl = component.loginForm.get('password');
      passwordControl?.setValue('12345');
      expect(passwordControl?.hasError('minlength')).toBeTrue();

      passwordControl?.setValue('123456');
      expect(passwordControl?.hasError('minlength')).toBeFalse();
    });

    it('should be invalid when form is empty', () => {
      expect(component.loginForm.valid).toBeFalse();
    });

    it('should be valid with correct data', () => {
      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'password123'
      });
      expect(component.loginForm.valid).toBeTrue();
    });
  });

  describe('Form Submission', () => {
    it('should not submit if form is invalid', () => {
      component.loginForm.patchValue({ email: '', password: '' });
      component.onSubmit();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should mark all fields as touched on invalid submit', () => {
      component.onSubmit();
      expect(component.loginForm.get('email')?.touched).toBeTrue();
      expect(component.loginForm.get('password')?.touched).toBeTrue();
    });

    it('should call authService.login on valid submit', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      expect(authServiceSpy.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      });
    }));

    it('should clear error message before login attempt', fakeAsync(() => {
      component.errorMessage.set('Previous error');
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      expect(component.errorMessage()).toBe('');
    }));
  });

  describe('Navigation After Login', () => {
    it('should navigate to products for CLIENT role', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      // Should navigate to products since isSeller returns false
      expect(router.navigate).toHaveBeenCalledWith(['/products']);
    }));

    it('should call login with correct credentials', fakeAsync(() => {
      authServiceSpy.login.and.returnValue(of(mockAuthResponse));

      component.loginForm.patchValue({
        email: 'seller@example.com',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      expect(authServiceSpy.login).toHaveBeenCalledWith({
        email: 'seller@example.com',
        password: 'password123'
      });
    }));
  });

  describe('Error Handling', () => {
    it('should display error message on login failure', fakeAsync(() => {
      const errorResponse = {
        error: { message: 'Invalid credentials' },
        status: 401
      };
      authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'wrongpassword'
      });

      component.onSubmit();
      tick();

      expect(component.errorMessage()).toBe('Invalid credentials');
    }));

    it('should show default error message when no message provided', fakeAsync(() => {
      const errorResponse = { status: 500 };
      authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.patchValue({
        email: 'test@example.com',
        password: 'password123'
      });

      component.onSubmit();
      tick();

      expect(component.errorMessage()).toBe('Login failed. Please check your credentials.');
    }));
  });

  describe('getErrorMessage', () => {
    it('should return empty string for valid field', () => {
      component.loginForm.get('email')?.setValue('valid@email.com');
      expect(component.getErrorMessage('email')).toBe('');
    });

    it('should return error message for invalid email', () => {
      component.loginForm.get('email')?.setValue('invalid');
      component.loginForm.get('email')?.markAsTouched();
      const errorMessage = component.getErrorMessage('email');
      expect(errorMessage).toBeTruthy();
    });

    it('should return empty string for non-existent control', () => {
      expect(component.getErrorMessage('nonexistent')).toBe('');
    });
  });

  describe('UI Elements', () => {
    it('should have email input field', () => {
      const emailInput = fixture.nativeElement.querySelector('input[formControlName="email"]');
      expect(emailInput).toBeTruthy();
    });

    it('should have password input field', () => {
      const passwordInput = fixture.nativeElement.querySelector('input[formControlName="password"]');
      expect(passwordInput).toBeTruthy();
    });

    it('should have submit button', () => {
      const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(submitButton).toBeTruthy();
    });

    it('should have link to registration page', () => {
      const registerLink = fixture.nativeElement.querySelector('a[routerLink="/auth/register"]');
      expect(registerLink).toBeTruthy();
    });
  });
});
