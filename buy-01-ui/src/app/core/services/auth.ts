import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, BehaviorSubject } from 'rxjs';

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

export interface AuthResponse {
  user: User;
  token: string;
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
  
  // API URL - JSON Server for development, Spring Boot for production
  private readonly API_URL = 'http://localhost:3000/api/auth';
  // When backend is ready, change to: 'http://localhost:8080/api/auth'
  
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
   * DEVELOPMENT ONLY: Using localStorage simulation
   * TODO: Replace with real API call when backend is ready
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    this.loadingSignal.set(true);
    
    // DEVELOPMENT ONLY: Simulate login without backend
    return new Observable<AuthResponse>(observer => {
      setTimeout(() => {
        // Get registered users from localStorage
        const registeredUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
        
        // Find user by email (we're not checking password in dev mode for simplicity)
        const user = registeredUsers.find((u: User) => u.email === credentials.email);
        
        if (!user) {
          this.loadingSignal.set(false);
          observer.error({ error: { message: 'Invalid email or password' } });
          return;
        }
        
        // Create fake token
        const fakeToken = `fake_token_${Date.now()}`;
        
        const response: AuthResponse = {
          user: user,
          token: fakeToken
        };
        
        this.setAuth(response.user, response.token);
        this.loadingSignal.set(false);
        
        observer.next(response);
        observer.complete();
      }, 500); // Simulate network delay
    });
    
    // PRODUCTION: Use real backend API
    // return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
    //   tap(response => {
    //     this.setAuth(response.user, response.token);
    //     this.loadingSignal.set(false);
    //   }),
    //   catchError(error => {
    //     this.loadingSignal.set(false);
    //     throw error;
    //   })
    // );
  }
  
  /**
   * Register new user
   * DEVELOPMENT ONLY: Using localStorage simulation
   * TODO: Replace with real API call when backend is ready
   */
  register(data: RegisterRequest): Observable<AuthResponse> {
    this.loadingSignal.set(true);
    
    // DEVELOPMENT ONLY: Simulate registration without backend
    return new Observable<AuthResponse>(observer => {
      setTimeout(() => {
        // Check if email already exists (basic validation)
        const existingUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
        const emailExists = existingUsers.some((u: User) => u.email === data.email);
        
        if (emailExists) {
          this.loadingSignal.set(false);
          observer.error({ error: { message: 'Email already registered' } });
          return;
        }
        
        // Create new user
        const newUser: User = {
          id: Date.now().toString(), // Simple ID generation
          email: data.email,
          name: data.name,
          role: data.role,
          avatarUrl: data.avatarUrl || null
        };
        
        // Store in "database" (localStorage)
        existingUsers.push(newUser);
        localStorage.setItem('registered_users', JSON.stringify(existingUsers));
        
        // Create fake token
        const fakeToken = `fake_token_${Date.now()}`;
        
        const response: AuthResponse = {
          user: newUser,
          token: fakeToken
        };
        
        // DON'T automatically log in - let user login manually
        // this.setAuth(response.user, response.token);
        this.loadingSignal.set(false);
        
        observer.next(response);
        observer.complete();
      }, 500); // Simulate network delay
    });
    
    // PRODUCTION: Use real backend API
    // return this.http.post<AuthResponse>(`${this.API_URL}/register`, data).pipe(
    //   tap(response => {
    //     this.setAuth(response.user, response.token);
    //     this.loadingSignal.set(false);
    //   }),
    //   catchError(error => {
    //     this.loadingSignal.set(false);
    //     throw error;
    //   })
    // );
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

