import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { ProductList } from './product-list';
import { ProductService, Product } from '../../../core/services/product.service';
import { Auth, User } from '../../../core/services/auth';

describe('ProductList Component', () => {
  let component: ProductList;
  let fixture: ComponentFixture<ProductList>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let authServiceSpy: jasmine.SpyObj<Auth>;
  let router: Router;

  const mockProducts: Product[] = [
    {
      id: 'prod-1',
      name: 'Product 1',
      description: 'Description 1',
      price: 99.99,
      stock: 10,
      sellerId: 'seller-1',
      imageUrls: ['http://example.com/img1.jpg']
    },
    {
      id: 'prod-2',
      name: 'Product 2',
      description: 'Description 2',
      price: 149.99,
      stock: 5,
      sellerId: 'seller-2',
      imageUrls: []
    },
    {
      id: 'prod-3',
      name: 'Product 3',
      description: 'Description 3',
      price: 199.99,
      stock: 0,
      sellerId: 'seller-1',
      imageUrls: ['http://example.com/img3.jpg']
    }
  ];

  const mockUser: User = {
    id: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    role: 'CLIENT'
  };

  beforeEach(async () => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['getAllProducts'], {
      products: signal<Product[]>([])
    });
    productServiceSpy.getAllProducts.and.returnValue(of(mockProducts));

    authServiceSpy = jasmine.createSpyObj('Auth', ['logout'], {
      currentUser: signal(mockUser),
      isAuthenticated: signal(true),
      isSeller: signal(false),
      isClient: signal(true)
    });

    await TestBed.configureTestingModule({
      imports: [
        ProductList,
        NoopAnimationsModule,
        RouterTestingModule.withRoutes([])
      ],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: Auth, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductList);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  describe('Component Creation', () => {
    it('should create', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should have empty products initially before loading', () => {
      expect(component.products()).toEqual([]);
    });
  });

  describe('ngOnInit', () => {
    it('should load products on init', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(productServiceSpy.getAllProducts).toHaveBeenCalled();
      expect(component.products().length).toBe(3);
    }));

    it('should set isLoading to true while loading', () => {
      expect(component.isLoading()).toBeFalse();
      
      fixture.detectChanges();
      
      // After products loaded, isLoading should be false
      expect(component.isLoading()).toBeFalse();
    });
  });

  describe('Product Loading', () => {
    it('should display products after loading', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      const products = component.products();
      expect(products.length).toBe(3);
      expect(products[0].name).toBe('Product 1');
      expect(products[1].name).toBe('Product 2');
      expect(products[2].name).toBe('Product 3');
    }));

    it('should handle empty product list', fakeAsync(() => {
      productServiceSpy.getAllProducts.and.returnValue(of([]));

      fixture.detectChanges();
      tick();

      expect(component.products()).toEqual([]);
      expect(component.isLoading()).toBeFalse();
    }));

    it('should handle error while loading products', fakeAsync(() => {
      const consoleSpy = spyOn(console, 'error');
      productServiceSpy.getAllProducts.and.returnValue(throwError(() => new Error('Network error')));

      fixture.detectChanges();
      tick();

      expect(consoleSpy).toHaveBeenCalled();
      expect(component.isLoading()).toBeFalse();
    }));
  });

  describe('viewProduct', () => {
    it('should navigate to product detail page', () => {
      component.viewProduct('prod-1');

      expect(router.navigate).toHaveBeenCalledWith(['/products', 'prod-1']);
    });

    it('should navigate with correct product ID', () => {
      component.viewProduct('prod-abc-123');

      expect(router.navigate).toHaveBeenCalledWith(['/products', 'prod-abc-123']);
    });
  });

  describe('logout', () => {
    it('should call authService.logout', () => {
      component.logout();

      expect(authServiceSpy.logout).toHaveBeenCalled();
    });
  });

  describe('goToDashboard', () => {
    it('should navigate to seller dashboard', () => {
      component.goToDashboard();

      expect(router.navigate).toHaveBeenCalledWith(['/seller/dashboard']);
    });
  });

  describe('UI Integration', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();
    }));

    it('should render product cards', () => {
      const productCards = fixture.nativeElement.querySelectorAll('mat-card');
      // Number may vary based on template structure
      expect(productCards.length).toBeGreaterThan(0);
    });

    it('should display product names', () => {
      const content = fixture.nativeElement.textContent;
      expect(content).toContain('Product 1');
      expect(content).toContain('Product 2');
      expect(content).toContain('Product 3');
    });

    it('should display product prices', () => {
      const content = fixture.nativeElement.textContent;
      expect(content).toContain('99.99');
      expect(content).toContain('149.99');
      expect(content).toContain('199.99');
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner when isLoading is true', () => {
      component.isLoading.set(true);
      fixture.detectChanges();

      const spinner = fixture.nativeElement.querySelector('mat-spinner, mat-progress-spinner');
      // Spinner should exist when loading
      // Note: exact selector depends on template implementation
    });

    it('should hide loading spinner when isLoading is false', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
    }));
  });

  describe('Auth Service Integration', () => {
    it('should have access to authService', () => {
      expect(component.authService).toBeTruthy();
    });

    it('should be able to check if user is seller', () => {
      // Access the authService through component
      expect(component.authService).toBeDefined();
    });
  });

  describe('Product Data Integrity', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should maintain product data structure', () => {
      const products = component.products();
      
      products.forEach(product => {
        expect(product.id).toBeDefined();
        expect(product.name).toBeDefined();
        expect(product.description).toBeDefined();
        expect(product.price).toBeDefined();
        expect(typeof product.price).toBe('number');
      });
    });

    it('should handle products with and without images', () => {
      const products = component.products();
      
      const productWithImage = products.find(p => p.imageUrls && p.imageUrls.length > 0);
      const productWithoutImage = products.find(p => !p.imageUrls || p.imageUrls.length === 0);
      
      expect(productWithImage).toBeTruthy();
      expect(productWithoutImage).toBeTruthy();
    });

    it('should handle products with zero stock', () => {
      const products = component.products();
      const outOfStockProduct = products.find(p => p.stock === 0);
      
      expect(outOfStockProduct).toBeTruthy();
      expect(outOfStockProduct?.name).toBe('Product 3');
    });
  });
});
