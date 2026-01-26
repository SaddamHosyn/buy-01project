import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService, Product, ProductRequest } from './product.service';
import { Auth } from './auth';
import { environment } from '../../../environments/environment';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<Auth>;

  const mockProducts: Product[] = [
    {
      id: 'prod-1',
      name: 'Product 1',
      description: 'Description 1',
      price: 99.99,
      stock: 10,
      sellerId: 'seller-123',
      mediaIds: ['media-1'],
      imageUrls: ['http://example.com/img1.jpg']
    },
    {
      id: 'prod-2',
      name: 'Product 2',
      description: 'Description 2',
      price: 149.99,
      stock: 5,
      sellerId: 'seller-456',
      mediaIds: [],
      imageUrls: []
    },
    {
      id: 'prod-3',
      name: 'Product 3',
      description: 'Description 3',
      price: 199.99,
      stock: 15,
      sellerId: 'seller-123',
      mediaIds: ['media-2'],
      imageUrls: ['http://example.com/img2.jpg']
    }
  ];

  const mockCurrentUser = () => ({ id: 'seller-123', email: 'seller@test.com', name: 'Seller', role: 'SELLER' as const });

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('Auth', [], {
      currentUser: mockCurrentUser
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProductService,
        { provide: Auth, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Service Creation', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should have empty products initially', () => {
      expect(service.products()).toEqual([]);
    });
  });

  describe('getAllProducts', () => {
    it('should fetch all products', fakeAsync(() => {
      service.getAllProducts().subscribe(products => {
        expect(products).toEqual(mockProducts);
        expect(products.length).toBe(3);
      });

      const req = httpMock.expectOne(environment.productsUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockProducts);

      tick();

      // Verify signal is updated
      expect(service.products()).toEqual(mockProducts);
    }));

    it('should handle empty product list', fakeAsync(() => {
      service.getAllProducts().subscribe(products => {
        expect(products).toEqual([]);
      });

      const req = httpMock.expectOne(environment.productsUrl);
      req.flush([]);

      tick();

      expect(service.products()).toEqual([]);
    }));

    it('should handle HTTP error', fakeAsync(() => {
      let errorOccurred = false;

      service.getAllProducts().subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(environment.productsUrl);
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));
  });

  describe('getProductById', () => {
    it('should fetch a single product by ID', fakeAsync(() => {
      const productId = 'prod-1';

      service.getProductById(productId).subscribe(product => {
        expect(product).toEqual(mockProducts[0]);
        expect(product.id).toBe(productId);
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/${productId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProducts[0]);

      tick();
    }));

    it('should handle product not found', fakeAsync(() => {
      let errorOccurred = false;

      service.getProductById('non-existent').subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/non-existent`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));
  });

  describe('getSellerProducts', () => {
    it('should filter products by current seller ID', fakeAsync(() => {
      service.getSellerProducts().subscribe(products => {
        expect(products.length).toBe(2);
        expect(products.every(p => p.sellerId === 'seller-123')).toBeTrue();
      });

      const req = httpMock.expectOne(environment.productsUrl);
      req.flush(mockProducts);

      tick();
    }));

    it('should return empty array if seller has no products', fakeAsync(() => {
      // Flush products that don't match the current user's sellerId
      // Current user is seller-123, but we'll return products with different sellerIds
      const productsWithDifferentSellers: Product[] = [
        { id: 'prod-1', name: 'Product 1', description: 'Desc', price: 10, sellerId: 'seller-999' },
        { id: 'prod-2', name: 'Product 2', description: 'Desc', price: 20, sellerId: 'seller-888' }
      ];

      service.getSellerProducts().subscribe(products => {
        expect(products.length).toBe(0);
      });

      const req = httpMock.expectOne(environment.productsUrl);
      req.flush(productsWithDifferentSellers);

      tick();
    }));

    it('should throw error if user not authenticated', () => {
      // Create a new spy that returns null for currentUser
      const nullAuthSpy = jasmine.createSpyObj('Auth', [], {
        currentUser: () => null
      });

      // Reset TestBed with null auth user
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [
          ProductService,
          { provide: Auth, useValue: nullAuthSpy }
        ]
      });

      const unauthService = TestBed.inject(ProductService);
      expect(() => unauthService.getSellerProducts()).toThrowError('User not authenticated');
    });
  });

  describe('createProduct', () => {
    const newProductRequest: ProductRequest = {
      name: 'New Product',
      description: 'New Description',
      price: 299.99,
      quantity: 20
    };

    const createdProduct: Product = {
      id: 'prod-new',
      name: 'New Product',
      description: 'New Description',
      price: 299.99,
      stock: 20,
      sellerId: 'seller-123'
    };

    it('should create a new product', fakeAsync(() => {
      service.createProduct(newProductRequest).subscribe(product => {
        expect(product).toEqual(createdProduct);
        expect(product.name).toBe('New Product');
      });

      const req = httpMock.expectOne(environment.productsUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newProductRequest);
      req.flush(createdProduct);

      tick();
    }));

    it('should handle validation error on create', fakeAsync(() => {
      let errorOccurred = false;

      service.createProduct({ name: '', description: '', price: -1, quantity: 0 }).subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(environment.productsUrl);
      req.flush({ message: 'Validation failed' }, { status: 400, statusText: 'Bad Request' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));
  });

  describe('updateProduct', () => {
    const updateRequest: Partial<ProductRequest> = {
      name: 'Updated Product Name',
      price: 399.99
    };

    const updatedProduct: Product = {
      ...mockProducts[0],
      name: 'Updated Product Name',
      price: 399.99
    };

    it('should update an existing product', fakeAsync(() => {
      service.updateProduct('prod-1', updateRequest).subscribe(product => {
        expect(product.name).toBe('Updated Product Name');
        expect(product.price).toBe(399.99);
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/prod-1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(updatedProduct);

      tick();
    }));

    it('should handle unauthorized update', fakeAsync(() => {
      let errorOccurred = false;

      service.updateProduct('prod-2', updateRequest).subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(403);
        }
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/prod-2`);
      req.flush({ message: 'Not authorized' }, { status: 403, statusText: 'Forbidden' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));
  });

  describe('deleteProduct', () => {
    it('should delete a product', fakeAsync(() => {
      let deleteCompleted = false;

      service.deleteProduct('prod-1').subscribe({
        next: () => {
          deleteCompleted = true;
        }
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/prod-1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);

      tick();

      expect(deleteCompleted).toBeTrue();
    }));

    it('should handle delete of non-existent product', fakeAsync(() => {
      let errorOccurred = false;

      service.deleteProduct('non-existent').subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/non-existent`);
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));

    it('should handle unauthorized delete', fakeAsync(() => {
      let errorOccurred = false;

      service.deleteProduct('prod-2').subscribe({
        error: (error) => {
          errorOccurred = true;
          expect(error.status).toBe(403);
        }
      });

      const req = httpMock.expectOne(`${environment.productsUrl}/prod-2`);
      req.flush({ message: 'Cannot delete another seller\'s product' }, { status: 403, statusText: 'Forbidden' });

      tick();

      expect(errorOccurred).toBeTrue();
    }));
  });
});
