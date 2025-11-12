import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

// Modern interface definitions
export interface User {
  id: string;
  email: string;
  name: string;
  role: 'SELLER' | 'CLIENT' | 'ADMIN';
  avatarUrl?: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: 'SELLER' | 'CLIENT';
  avatarUrl?: string | null;
}

// Backend response for login
export interface AuthResponse {
  token: string;
  id: string;
  email: string;
  name: string;
  role: 'SELLER' | 'CLIENT' | 'ADMIN';
  avatarUrl?: string | null;
}

// Backend response for registration
export interface RegisterResponse {
  id: string;
  email: string;
  name: string;
  role: 'SELLER' | 'CLIENT' | 'ADMIN';
  avatarUrl?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  
  // Modern Signals for reactive state management
  private readonly currentUserSignal = signal<User | null>(null);
  private readonly tokenSignal = signal<string | null>(null);
  private readonly loadingSignal = signal<boolean>(false);
  
  // Public readonly signals
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly token = this.tokenSignal.asReadonly();
  readonly isLoading = this.loadingSignal.asReadonly();
  
  // Computed signals for derived state
  readonly isAuthenticated = computed(() => !!this.currentUserSignal());
  readonly isSeller = computed(() => this.currentUserSignal()?.role === 'SELLER');
  readonly isClient = computed(() => this.currentUserSignal()?.role === 'CLIENT');
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');
  
  // API URL from environment configuration
  private readonly API_URL = environment.authUrl;
  
  constructor() {
    this.loadUserFromStorage();
  }
  
  /**
   * Load user from localStorage on app initialization
   */
  private loadUserFromStorage(): void {
    try {
      const token = localStorage.getItem('auth_token');
      const userJson = localStorage.getItem('current_user');
      
      if (token && userJson) {
        const user = JSON.parse(userJson);
        this.tokenSignal.set(token);
        this.currentUserSignal.set(user);
      }
    } catch (error) {
      console.error('Error loading user from storage:', error);
      this.clearAuth();
    }
  }
  
  /**
   * Login with email and password
   * Calls backend API: POST /api/auth/login
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    this.loadingSignal.set(true);
    
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response: AuthResponse) => {
        // Map backend response to User object
        const user: User = {
          id: response.id,
          email: response.email,
          name: response.name,
          role: response.role,
          avatarUrl: response.avatarUrl
        };
        
        this.setAuth(user, response.token);
        this.loadingSignal.set(false);
      }),
      catchError((error: any) => {
        this.loadingSignal.set(false);
        return throwError(() => error);
      })
    );
  }
  
  /**
   * Register new user
   * Calls backend API: POST /api/auth/register
   */
  register(data: RegisterRequest): Observable<RegisterResponse> {
    this.loadingSignal.set(true);
    
    return this.http.post<RegisterResponse>(`${this.API_URL}/register`, data).pipe(
      tap(() => {
        this.loadingSignal.set(false);
      }),
      catchError((error: any) => {
        this.loadingSignal.set(false);
        return throwError(() => error);
      })
    );
  }
  
  /**
   * Logout user
   */
  logout(): void {
    this.clearAuth();
    this.router.navigate(['/auth/login']);
  }
  
  /**
   * Set authentication data
   */
  private setAuth(user: User, token: string): void {
    this.currentUserSignal.set(user);
    this.tokenSignal.set(token);
    
    // Persist to localStorage
    localStorage.setItem('auth_token', token);
    localStorage.setItem('current_user', JSON.stringify(user));
  }
  
  /**
   * Clear authentication data
   */
  private clearAuth(): void {
    this.currentUserSignal.set(null);
    this.tokenSignal.set(null);
    
    // Clear from localStorage
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
  }
  
  /**
   * Check if user has specific role
   */
  hasRole(role: User['role']): boolean {
    return this.currentUserSignal()?.role === role;
  }
  
  /**
   * Update current user data (for profile updates)
   */
  updateUser(updates: Partial<User>): void {
    const currentUser = this.currentUserSignal();
    if (currentUser) {
      const updatedUser = { ...currentUser, ...updates };
      this.currentUserSignal.set(updatedUser);
      
      // Update localStorage
      localStorage.setItem('current_user', JSON.stringify(updatedUser));
    }
  }
  
  /**
   * Get current token (for interceptor)
   */
  getToken(): string | null {
    return this.tokenSignal();
  }
}

