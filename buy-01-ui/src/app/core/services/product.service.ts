import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map } from 'rxjs';
import { Auth } from './auth';

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  sellerId?: string;
  imageUrls?: string[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(Auth);
  
  // JSON Server for development - using direct /products endpoint
  private readonly API_URL = 'http://localhost:3000/products';
  // When backend is ready, change to: 'http://localhost:8080/api/products'
  
  // Signals for state management
  private readonly productsSignal = signal<Product[]>([]);
  readonly products = this.productsSignal.asReadonly();
  
  /**
   * Get all products (public)
   */
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.API_URL).pipe(
      tap(products => this.productsSignal.set(products))
    );
  }
  
  /**
   * Get product by ID
   */
  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }
  
  /**
   * Get seller's products (authenticated)
   * CSR APPROACH: Get all products, filter client-side by sellerId
   */
  getSellerProducts(): Observable<Product[]> {
    const currentUserId = this.authService.currentUser()?.id;
    
    if (!currentUserId) {
      throw new Error('User not authenticated');
    }
    
    // Get all products and filter by sellerId (client-side filtering - CSR!)
    return this.http.get<Product[]>(this.API_URL).pipe(
      map(products => products.filter(p => p.sellerId === currentUserId))
    );
  }
  
  /**
   * Create product (sellers only)
   */
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.API_URL, product);
  }
  
  /**
   * Update product (sellers only - own products)
   */
  updateProduct(id: string, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.API_URL}/${id}`, product);
  }
  
  /**
   * Delete product (sellers only - own products)
   */
  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
  
  /**
   * Add images to product
   * CSR APPROACH: Update product with new imageUrls array
   */
  addProductImages(productId: string, imageUrls: string[]): Observable<string[]> {
    // Get current product first
    return this.getProductById(productId).pipe(
      map(product => {
        const updatedImageUrls = [...(product.imageUrls || []), ...imageUrls];
        
        // Update product with new images
        this.updateProduct(productId, { imageUrls: updatedImageUrls }).subscribe();
        
        return imageUrls;
      })
    );
  }
  
  /**
   * Delete product image
   * CSR APPROACH: Update product with filtered imageUrls array
   */
  deleteProductImage(productId: string, imageUrl: string): Observable<void> {
    // Get current product first
    return this.getProductById(productId).pipe(
      map(product => {
        const updatedImageUrls = (product.imageUrls || []).filter(url => url !== imageUrl);
        
        // Update product with filtered images
        this.updateProduct(productId, { imageUrls: updatedImageUrls }).subscribe();
      })
    );
  }
}
