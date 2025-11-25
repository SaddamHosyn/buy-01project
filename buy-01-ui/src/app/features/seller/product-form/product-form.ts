import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProductService, Product, ProductRequest } from '../../../core/services/product.service';
import { MediaService, Media } from '../../../core/services/media.service';
import { Auth } from '../../../core/services/auth';
import { priceValidator, getValidationMessage } from '../../../core/validators/form.validators';
import { validateFile, validateFiles, ValidationPresets } from '../../../core/validators/file-upload.validator';
import { DialogService } from '../../../shared/services/dialog.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './product-form.html',
  styleUrl: './product-form.css',
})
export class ProductForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly mediaService = inject(MediaService);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialogService = inject(DialogService);
  
  // Signals for reactive state
  readonly isEditMode = signal<boolean>(false);
  readonly isLoading = signal<boolean>(false);
  readonly isSaving = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly successMessage = signal<string>('');
  readonly productId = signal<string | null>(null);
  
  // Image handling signals
  readonly selectedImages = signal<File[]>([]);
  readonly imagePreviews = signal<string[]>([]);
  readonly existingImageUrls = signal<string[]>([]);
  readonly uploadError = signal<string>('');
  readonly deletedMediaIds = signal<string[]>([]); // NEW: Track deleted media IDs
  
  // Reactive form
  productForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    price: [0, [Validators.required, priceValidator(0.01, 999999.99, 2)]],
    quantity: [1, [Validators.required, Validators.min(0)]]
  });
  
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productId.set(id);
      this.isEditMode.set(true);
      this.loadProduct(id);
    }
  }
  
  loadProduct(id: string): void {
    this.isLoading.set(true);
    this.errorMessage.set('');
    
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          quantity: product.stock || 0
        });
        
        if (product.imageUrls && product.imageUrls.length > 0) {
          this.existingImageUrls.set(product.imageUrls);
        }
        
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.errorMessage.set('Failed to load product');
        this.isLoading.set(false);
      }
    });
  }
  
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    
    if (!files || files.length === 0) {
      return;
    }
    
    this.uploadError.set('');
    const validFiles: File[] = [];
    const previews: string[] = [];
    
    const filesArray = Array.from(files);
    const validationResults = validateFiles(filesArray, ValidationPresets.PRODUCT_IMAGE);
    
    let hasErrors = false;
    validationResults.forEach((result, filename) => {
      if (!result.valid) {
        this.uploadError.set(`${filename}: ${result.errors[0]}`);
        hasErrors = true;
      }
    });
    
    if (hasErrors) {
      return;
    }
    
    for (const file of filesArray) {
      validFiles.push(file);
      
      const reader = new FileReader();
      reader.onload = (e) => {
        previews.push(e.target?.result as string);
        if (previews.length === validFiles.length) {
          this.imagePreviews.set([...this.imagePreviews(), ...previews]);
        }
      };
      reader.readAsDataURL(file);
    }
    
    this.selectedImages.set([...this.selectedImages(), ...validFiles]);
  }
  
  removeNewImage(index: number): void {
    this.selectedImages.update(images => images.filter((_, i) => i !== index));
    this.imagePreviews.update(previews => previews.filter((_, i) => i !== index));
  }
  
 


/**
 * Remove existing image URL and DELETE from backend
 */
/**
 * Remove existing image URL and DELETE from backend
 */
removeExistingImage(index: number): void {
  const urls = this.existingImageUrls();
  const urlToRemove = urls[index];
  
  const urlParts = urlToRemove.split('/');
  const mediaId = urlParts[urlParts.length - 1];
  
  if (mediaId && mediaId.length > 0) {
    this.dialogService.confirm({
      title: 'Delete Image',
      message: 'Are you sure you want to delete this image? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'delete'
    }).subscribe(confirmed => {
      if (!confirmed) {
        return;
      }
      
      // Show loading
      this.isSaving.set(true);
      
      this.mediaService.deleteMedia(mediaId).subscribe({
        next: () => {
          // Remove from UI immediately
          this.existingImageUrls.update(urls => urls.filter((_, i) => i !== index));
          
          // IMPORTANT: Also remove from product's mediaIds in backend
          const productId = this.productId();
          if (productId) {
            this.productService.removeMediaFromProduct(productId, mediaId).subscribe({
              next: () => {
                console.log('Media removed from product successfully');
              },
              error: (err) => {
                console.warn('Could not remove media from product:', err);
              }
            });
          }
          
          this.successMessage.set('Image deleted successfully');
          this.isSaving.set(false);
          
          setTimeout(() => this.successMessage.set(''), 3000);
        },
        error: (error) => {
          console.error('Error deleting image:', error);
          this.errorMessage.set('Failed to delete image from server');
          this.isSaving.set(false);
          
          setTimeout(() => this.errorMessage.set(''), 5000);
        }
      });
    });
  } else {
    this.existingImageUrls.update(urls => urls.filter((_, i) => i !== index));
  }
}















  
 onSubmit(): void {
  if (this.productForm.invalid) {
    this.productForm.markAllAsTouched();
    return;
  }
  
  // Clear any previous error messages
  this.errorMessage.set('');
  this.successMessage.set('');
  this.uploadError.set('');
  
  this.isSaving.set(true);
  
  const formData = this.productForm.value;
  
  const productData: Partial<Product> = {
    name: formData.name,
    description: formData.description,
    price: Number(formData.price),
    stock: Number(formData.quantity),
    sellerId: this.authService.currentUser()?.id || '',
    imageUrls: [
      ...this.existingImageUrls(),
      ...this.imagePreviews()
    ]
  };
  
  if (this.isEditMode()) {
    this.updateProduct(productData);
  } else {
    this.createProduct(productData);
  }
}

  
  private createProduct(productData: Partial<Product>): void {
    const selectedFiles = this.selectedImages();
    
    if (selectedFiles.length > 0) {
      this.mediaService.uploadFiles(selectedFiles).subscribe({
        next: (mediaList) => {
          const mediaIds = mediaList.map(m => m.id);
          this.createProductWithMedia(productData, mediaIds);
        },
        error: (error) => {
          console.error('Error uploading images:', error);
          this.errorMessage.set('Failed to upload images. Please try again.');
          this.isSaving.set(false);
        }
      });
    } else {
      this.createProductWithMedia(productData, []);
    }
  }
  
  private createProductWithMedia(productData: Partial<Product>, mediaIds: string[]): void {
    const productRequest: ProductRequest = {
      name: productData.name!,
      description: productData.description!,
      price: productData.price!,
      quantity: productData.stock || 0
    };
    
    this.productService.createProduct(productRequest).subscribe({
      next: (product) => {
        if (mediaIds.length > 0) {
          this.associateMediaWithProduct(product.id, mediaIds);
        } else {
          this.successMessage.set('Product created successfully!');
          this.isSaving.set(false);
          
          setTimeout(() => {
            this.router.navigate(['/seller/dashboard']);
          }, 1000);
        }
      },
      error: (error) => {
        console.error('Error creating product:', error);
        this.errorMessage.set('Failed to create product. Please try again.');
        this.isSaving.set(false);
      }
    });
  }
  
  private associateMediaWithProduct(productId: string, mediaIds: string[]): void {
    const associations = mediaIds.map(mediaId => 
      this.productService.associateMedia(productId, mediaId)
    );
    
    forkJoin(associations).subscribe({
      next: () => {
        this.successMessage.set('Product created successfully with images!');
        this.isSaving.set(false);
        
        setTimeout(() => {
          this.router.navigate(['/seller/dashboard']);
        }, 1000);
      },
      error: (error) => {
        console.error('Error associating media:', error);
        this.errorMessage.set('Product created but failed to associate images.');
        this.isSaving.set(false);
      }
    });
  }
  
  /**
   * Update existing product (INCLUDING NEW IMAGES AND DELETIONS)
   */
  private updateProduct(productData: Partial<Product>): void {
    const id = this.productId();
    if (!id) return;
    
    const selectedFiles = this.selectedImages();
    const deletedIds = this.deletedMediaIds();
    
    // Step 1: Remove deleted media IDs from product (if any)
    if (deletedIds.length > 0) {
      this.removeDeletedMediaFromProduct(id, deletedIds, () => {
        this.proceedWithUpdate(id, selectedFiles, productData);
      });
    } else {
      this.proceedWithUpdate(id, selectedFiles, productData);
    }
  }
  
  /**
   * Helper method to continue update flow
   */
  private proceedWithUpdate(id: string, selectedFiles: File[], productData: Partial<Product>): void {
    if (selectedFiles.length > 0) {
      this.mediaService.uploadFiles(selectedFiles).subscribe({
        next: (mediaList) => {
          const newMediaIds = mediaList.map(m => m.id);
          
          const associations = newMediaIds.map(mediaId => 
            this.productService.associateMedia(id, mediaId)
          );
          
          forkJoin(associations).subscribe({
            next: () => {
              this.updateProductDetails(id, productData);
            },
            error: (error) => {
              console.error('Error associating new images:', error);
              this.errorMessage.set('Failed to link new images to product');
              this.isSaving.set(false);
            }
          });
        },
        error: (error) => {
          console.error('Error uploading new images:', error);
          this.errorMessage.set('Failed to upload new images. Please try again.');
          this.isSaving.set(false);
        }
      });
    } else {
      this.updateProductDetails(id, productData);
    }
  }
  
  /**
   * Remove deleted media IDs from product's mediaIds array
   */
  private removeDeletedMediaFromProduct(productId: string, deletedIds: string[], callback: () => void): void {
    // Log deleted IDs for now
    // The backend should handle orphaned media IDs gracefully
    console.log('Deleted media IDs:', deletedIds);
    callback();
  }
  
  /**
   * Update product details
   */
  private updateProductDetails(id: string, productData: Partial<Product>): void {
    const updateRequest: Partial<ProductRequest> = {
      name: productData.name,
      description: productData.description,
      price: productData.price,
      quantity: productData.stock
    };
    
    this.productService.updateProduct(id, updateRequest).subscribe({
      next: (product) => {
        this.successMessage.set('Product updated successfully!');
        this.isSaving.set(false);
        
        setTimeout(() => {
          this.router.navigate(['/seller/dashboard']);
        }, 1000);
      },
      error: (error) => {
        console.error('Error updating product:', error);
        this.errorMessage.set('Failed to update product. Please try again.');
        this.isSaving.set(false);
      }
    });
  }
  
  cancel(): void {
    if (this.productForm.dirty) {
      this.dialogService.confirmDiscard().subscribe(confirmed => {
        if (confirmed) {
          this.router.navigate(['/seller/dashboard']);
        }
      });
    } else {
      this.router.navigate(['/seller/dashboard']);
    }
  }
  
  getErrorMessage(controlName: string): string {
    const control = this.productForm.get(controlName);
    
    if (!control || !control.errors) {
      return '';
    }
    
    if (controlName === 'quantity') {
      if (control.hasError('required')) return 'Quantity is required';
      if (control.hasError('min')) return 'Quantity cannot be negative';
    }
    
    return getValidationMessage(control.errors, controlName);
  }
}
