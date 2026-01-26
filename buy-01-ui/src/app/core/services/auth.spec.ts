import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { Auth, User, LoginRequest, RegisterRequest, AuthResponse, RegisterResponse } from './auth';
import { environment } from '../../../environments/environment';

describe('Auth Service', () => {
  let service: Auth;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockUser: User = {
    id: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    role: 'CLIENT',
    avatarUrl: null
  };

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    id: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    role: 'CLIENT',
    avatarUrl: null
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    
    // Clear localStorage before each test
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        Auth,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(Auth);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('Initial State', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should have null currentUser initially', () => {
      expect(service.currentUser()).toBeNull();
    });

    it('should have null token initially', () => {
      expect(service.token()).toBeNull();
    });

    it('should not be authenticated initially', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });

    it('should not be loading initially', () => {
      expect(service.isLoading()).toBeFalse();
    });
  });

  describe('login', () => {
    const loginRequest: LoginRequest = {
      email: 'test@example.com',
      password: 'password123'
    };

    it('should login successfully and store user data', fakeAsync(() => {
      service.login(loginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      req.flush(mockAuthResponse);

      tick();

      expect(service.currentUser()).toEqual(mockUser);
      expect(service.token()).toBe('mock-jwt-token');
      expect(service.isAuthenticated()).toBeTrue();
      expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
    }));

    it('should set loading state during login', fakeAsync(() => {
      service.login(loginRequest).subscribe();
      
      expect(service.isLoading()).toBeTrue();

      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      req.flush(mockAuthResponse);

      tick();

      expect(service.isLoading()).toBeFalse();
    }));

    it('should handle login error', fakeAsync(() => {
      let errorOccurred = false;

      service.login(loginRequest).subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(401);
        }
      });

      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

      tick();

      expect(errorOccurred).toBeTrue();
      expect(service.isLoading()).toBeFalse();
      expect(service.isAuthenticated()).toBeFalse();
    }));
  });

  describe('register', () => {
    const registerRequest: RegisterRequest = {
      email: 'new@example.com',
      password: 'password123',
      name: 'New User',
      role: 'CLIENT'
    };

    const registerResponse: RegisterResponse = {
      id: 'user-456',
      email: 'new@example.com',
      name: 'New User',
      role: 'CLIENT',
      avatarUrl: null
    };

    it('should register successfully', fakeAsync(() => {
      service.register(registerRequest).subscribe(response => {
        expect(response).toEqual(registerResponse);
      });

      const req = httpMock.expectOne(`${environment.authUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerRequest);
      req.flush(registerResponse);

      tick();

      expect(service.isLoading()).toBeFalse();
    }));

    it('should handle registration error', fakeAsync(() => {
      let errorOccurred = false;

      service.register(registerRequest).subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(`${environment.authUrl}/register`);
      req.flush({ message: 'Email already exists' }, { status: 400, statusText: 'Bad Request' });

      tick();

      expect(errorOccurred).toBeTrue();
      expect(service.isLoading()).toBeFalse();
    }));
  });

  describe('logout', () => {
    it('should clear auth data and navigate to login', fakeAsync(() => {
      // First, simulate a logged-in state
      localStorage.setItem('auth_token', 'mock-token');
      localStorage.setItem('current_user', JSON.stringify(mockUser));

      // Re-create service to pick up localStorage
      service = TestBed.inject(Auth);

      service.logout();

      expect(service.currentUser()).toBeNull();
      expect(service.token()).toBeNull();
      expect(service.isAuthenticated()).toBeFalse();
      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('current_user')).toBeNull();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
    }));
  });

  describe('Role Checks', () => {
    beforeEach(fakeAsync(() => {
      const loginRequest: LoginRequest = { email: 'test@example.com', password: 'pass' };
      service.login(loginRequest).subscribe();
      
      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      req.flush(mockAuthResponse);
      tick();
    }));

    it('should correctly identify CLIENT role', () => {
      expect(service.isClient()).toBeTrue();
      expect(service.isSeller()).toBeFalse();
      expect(service.isAdmin()).toBeFalse();
    });

    it('should check role with hasRole method', () => {
      expect(service.hasRole('CLIENT')).toBeTrue();
      expect(service.hasRole('SELLER')).toBeFalse();
      expect(service.hasRole('ADMIN')).toBeFalse();
    });
  });

  describe('Load User from Storage', () => {
    it('should restore user from localStorage on init', () => {
      // Set localStorage before creating the service
      localStorage.setItem('auth_token', 'stored-token');
      localStorage.setItem('current_user', JSON.stringify(mockUser));

      // Reset TestBed to create a fresh instance with pre-set localStorage
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [
          Auth,
          { provide: Router, useValue: routerSpy }
        ]
      });

      // Create new service which should load from localStorage in constructor
      const newService = TestBed.inject(Auth);
      
      // The constructor should load from storage
      expect(newService.token()).toBe('stored-token');
      expect(newService.currentUser()?.email).toBe('test@example.com');
    });

    it('should handle corrupted localStorage gracefully', () => {
      localStorage.setItem('auth_token', 'valid-token');
      localStorage.setItem('current_user', 'invalid-json');

      // Should not throw - service should clear invalid data
      expect(() => TestBed.inject(Auth)).not.toThrow();
    });
  });

  describe('getToken', () => {
    it('should return current token', fakeAsync(() => {
      const loginRequest: LoginRequest = { email: 'test@example.com', password: 'pass' };
      service.login(loginRequest).subscribe();
      
      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      req.flush(mockAuthResponse);
      tick();

      expect(service.getToken()).toBe('mock-jwt-token');
    }));

    it('should return null when not authenticated', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  describe('updateUser', () => {
    it('should update user data in signal and localStorage', fakeAsync(() => {
      const loginRequest: LoginRequest = { email: 'test@example.com', password: 'pass' };
      service.login(loginRequest).subscribe();
      
      const req = httpMock.expectOne(`${environment.authUrl}/login`);
      req.flush(mockAuthResponse);
      tick();

      service.updateUser({ name: 'Updated Name' });

      expect(service.currentUser()?.name).toBe('Updated Name');
      const storedUser = JSON.parse(localStorage.getItem('current_user') || '{}');
      expect(storedUser.name).toBe('Updated Name');
    }));

    it('should not update if no user is logged in', () => {
      service.updateUser({ name: 'Updated Name' });
      expect(service.currentUser()).toBeNull();
    });
  });
});
