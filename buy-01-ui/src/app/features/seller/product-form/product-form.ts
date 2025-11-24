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
import {
  validateFile,
  validateFiles,
  ValidationPresets,
} from '../../../core/validators/file-upload.validator';
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
    MatTooltipModule,
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

  // Reactive form
  productForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    price: [0, [Validators.required, priceValidator(0.01, 999999.99, 2)]],
  });

  ngOnInit(): void {
    // Check if we're in edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productId.set(id);
      this.isEditMode.set(true);
      this.loadProduct(id);
    }
  }

  /**
   * Load product for editing
   */
  loadProduct(id: string): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        // Populate form
        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
        });

        // Set existing images
        if (product.imageUrls && product.imageUrls.length > 0) {
          this.existingImageUrls.set(product.imageUrls);
        }

        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.errorMessage.set('Failed to load product');
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Handle multiple image selection
   */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;

    if (!files || files.length === 0) {
      return;
    }

    this.uploadError.set('');
    const validFiles: File[] = [];
    const previews: string[] = [];

    // Convert FileList to Array
    const filesArray = Array.from(files);

    // Validate all files using ValidationPresets
    const validationResults = validateFiles(filesArray, ValidationPresets.PRODUCT_IMAGE);

    // Check for any validation errors
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

    // All files are valid, generate previews
    for (const file of filesArray) {
      validFiles.push(file);

      // Generate preview
      const reader = new FileReader();
      reader.onload = (e) => {
        previews.push(e.target?.result as string);
        if (previews.length === validFiles.length) {
          this.imagePreviews.set([...this.imagePreviews(), ...previews]);
        }
      };
      reader.readAsDataURL(file);
    }

    // Add valid files
    this.selectedImages.set([...this.selectedImages(), ...validFiles]);
  }

  /**
   * Remove new image preview
   */
  removeNewImage(index: number): void {
    this.selectedImages.update((images) => images.filter((_, i) => i !== index));
    this.imagePreviews.update((previews) => previews.filter((_, i) => i !== index));
  }

  /**
   * Remove existing image URL
   */
  removeExistingImage(index: number): void {
    this.existingImageUrls.update((urls) => urls.filter((_, i) => i !== index));
  }

  /**
   * Submit form (create or update)
   */
  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const formData = this.productForm.value;

    // Prepare product data
    const productData: Partial<Product> = {
      name: formData.name,
      description: formData.description,
      price: Number(formData.price),
      sellerId: this.authService.currentUser()?.id || '',
      imageUrls: [
        ...this.existingImageUrls(),
        ...this.imagePreviews(), // In dev, use base64 previews
      ],
    };

    if (this.isEditMode()) {
      // Update existing product
      this.updateProduct(productData);
    } else {
      // Create new product
      this.createProduct(productData);
    }
  }

  /**
   * Create new product
   */
  private createProduct(productData: Partial<Product>): void {
    // First, upload any new images to the media service
    const selectedFiles = this.selectedImages();

    if (selectedFiles.length > 0) {
      // Upload files first, then create product with media IDs
      this.mediaService.uploadFiles(selectedFiles).subscribe({
        next: (mediaList) => {
          // Get media IDs from uploaded files
          const mediaIds = mediaList.map((m) => m.id);

          // Create product with media IDs
          this.createProductWithMedia(productData, mediaIds);
        },
        error: (error) => {
          console.error('Error uploading images:', error);
          this.errorMessage.set('Failed to upload images. Please try again.');
          this.isSaving.set(false);
        },
      });
    } else {
      // No images to upload, create product directly
      this.createProductWithMedia(productData, []);
    }
  }

  /**
   * Create product with media IDs
   */
  private createProductWithMedia(productData: Partial<Product>, mediaIds: string[]): void {
    const productRequest: ProductRequest = {
      name: productData.name!,
      description: productData.description!,
      price: productData.price!,
      quantity: 10, // Default quantity
    };

    this.productService.createProduct(productRequest).subscribe({
      next: (product) => {
        // Associate media with the product if there are any
        if (mediaIds.length > 0) {
          this.associateMediaWithProduct(product.id, mediaIds);
        } else {
          this.successMessage.set('Product created successfully!');
          this.isSaving.set(false);

          // Redirect to dashboard after 1 second
          setTimeout(() => {
            this.router.navigate(['/seller/dashboard']);
          }, 1000);
        }
      },
      error: (error) => {
        console.error('Error creating product:', error);
        this.errorMessage.set('Failed to create product. Please try again.');
        this.isSaving.set(false);
      },
    });
  }

  /**
   * Associate media files with product
   */
  private associateMediaWithProduct(productId: string, mediaIds: string[]): void {
    // Associate each media with the product
    const associations = mediaIds.map((mediaId) =>
      this.productService.associateMedia(productId, mediaId)
    );

    // Wait for all associations to complete
    forkJoin(associations).subscribe({
      next: () => {
        this.successMessage.set('Product created successfully with images!');
        this.isSaving.set(false);

        // Redirect to dashboard after 1 second
        setTimeout(() => {
          this.router.navigate(['/seller/dashboard']);
        }, 1000);
      },
      error: (error) => {
        console.error('Error associating media:', error);
        this.errorMessage.set('Product created but failed to associate images.');
        this.isSaving.set(false);
      },
    });
  }

  /**
   * Update existing product
   */
  private updateProduct(productData: Partial<Product>): void {
    const id = this.productId();
    if (!id) return;

    // Prepare update request with ONLY the fields backend expects
    const updateRequest = {
      name: productData.name,
      description: productData.description,
      price: productData.price,
      quantity: productData.stock || 0,
    };

    this.productService.updateProduct(id, updateRequest).subscribe({
      next: (product) => {
        this.successMessage.set('Product updated successfully!');
        this.isSaving.set(false);

        // Redirect to dashboard after 1 second
        setTimeout(() => {
          this.router.navigate(['/seller/dashboard']);
        }, 1000);
      },
      error: (error) => {
        console.error('Error updating product:', error);
        this.errorMessage.set('Failed to update product. Please try again.');
        this.isSaving.set(false);
      },
    });
  }

  /**
   * Cancel and go back to dashboard
   */
  cancel(): void {
    if (this.productForm.dirty) {
      this.dialogService.confirmDiscard().subscribe((confirmed) => {
        if (confirmed) {
          this.router.navigate(['/seller/dashboard']);
        }
      });
    } else {
      this.router.navigate(['/seller/dashboard']);
    }
  }

  /**
   * Get form control error message
   */
  getErrorMessage(controlName: string): string {
    const control = this.productForm.get(controlName);

    if (!control || !control.errors) {
      return '';
    }

    // Use the centralized error message helper
    return getValidationMessage(control.errors, controlName);
  }
}
