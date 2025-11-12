import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MediaService, Media, UploadProgress } from '../../../core/services/media.service';
import { Auth } from '../../../core/services/auth';
import { ProductService, Product } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DialogService } from '../../../shared/services/dialog.service';

@Component({
  selector: 'app-media-manager',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDialogModule
  ],
  templateUrl: './media-manager.html',
  styleUrl: './media-manager.css',
})
export class MediaManager implements OnInit {
  private readonly mediaService = inject(MediaService);
  private readonly productService = inject(ProductService);
  private readonly authService = inject(Auth);
  private readonly location = inject(Location);
  private readonly notification = inject(NotificationService);
  private readonly dialogService = inject(DialogService);
  
  // Signals for reactive state
  readonly allMedia = signal<Media[]>([]);
  readonly isLoading = signal<boolean>(true);
  readonly isUploading = signal<boolean>(false);
  readonly selectedMedia = signal<Set<string>>(new Set());
  readonly uploadProgress = signal<UploadProgress[]>([]);
  readonly myProducts = signal<Product[]>([]);
  readonly selectedProductId = signal<string | undefined>(undefined);
  readonly filterByProduct = signal<string>('all');
  
  // Computed signals
  readonly currentUser = this.authService.currentUser;
  readonly hasSelectedMedia = computed(() => this.selectedMedia().size > 0);
  readonly selectedCount = computed(() => this.selectedMedia().size);
  readonly totalSize = computed(() => {
    return this.filteredMedia().reduce((sum, media) => sum + media.size, 0);
  });
  readonly filteredMedia = computed(() => {
    const filter = this.filterByProduct();
    if (filter === 'all') return this.allMedia();
    if (filter === 'unassigned') return this.allMedia().filter(m => !m.productId);
    return this.allMedia().filter(m => m.productId === filter);
  });
  
  // Validation info
  readonly maxFileSize = this.mediaService.getMaxFileSize();
  readonly allowedTypes = this.mediaService.getAllowedTypes();
  readonly allowedExtensions = this.mediaService.getAllowedExtensions();
  
  ngOnInit(): void {
    this.loadMedia();
    this.loadMyProducts();
  }
  
  /**
   * Navigate back to previous page
   */
  goBack(): void {
    this.location.back();
  }
  
  /**
   * Load seller's products
   */
  loadMyProducts(): void {
    this.productService.getSellerProducts().subscribe({
      next: (products) => {
        this.myProducts.set(products);
      },
      error: (error) => {
        console.error('Error loading products:', error);
      }
    });
  }
  
  /**
   * Load all media
   */
  loadMedia(): void {
    this.isLoading.set(true);
    
    this.mediaService.getAllMedia().subscribe({
      next: (media) => {
        this.allMedia.set(media);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading media:', error);
        this.notification.error('Failed to load media');
        this.isLoading.set(false);
      }
    });
  }
  
  /**
   * Handle file selection
   */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    
    if (!files || files.length === 0) {
      return;
    }
    
    const fileArray = Array.from(files);
    this.uploadFiles(fileArray);
    
    // Reset input
    input.value = '';
  }
  
  /**
   * Upload files with progress tracking
   */
  uploadFiles(files: File[]): void {
    this.isUploading.set(true);
    this.uploadProgress.set([]);
    
    const productId = this.selectedProductId();
    
    this.mediaService.uploadFiles(files, productId).subscribe({
      next: (progress) => {
        this.uploadProgress.set(progress);
        
        // Check if all uploads are completed
        const allCompleted = progress.every(p => p.status !== 'uploading');
        if (allCompleted) {
          this.isUploading.set(false);
          
          // Count successful uploads
          const successful = progress.filter(p => p.status === 'completed').length;
          const failed = progress.filter(p => p.status === 'error').length;
          
          if (successful > 0) {
            this.notification.fileUploadSuccess(successful, failed > 0 ? failed : undefined);
            
            // Reload media
            this.loadMedia();
          } else {
            this.notification.error('All uploads failed');
          }
          
          // Clear progress after 3 seconds
          setTimeout(() => {
            this.mediaService.clearAllProgress();
            this.uploadProgress.set([]);
          }, 3000);
        }
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.isUploading.set(false);
        this.notification.error('Upload failed');
      }
    });
  }
  
  /**
   * Toggle media selection
   */
  toggleSelection(mediaId: string): void {
    this.selectedMedia.update(selected => {
      const newSet = new Set(selected);
      if (newSet.has(mediaId)) {
        newSet.delete(mediaId);
      } else {
        newSet.add(mediaId);
      }
      return newSet;
    });
  }
  
  /**
   * Select all media
   */
  selectAll(): void {
    const allIds = this.allMedia().map(m => m.id);
    this.selectedMedia.set(new Set(allIds));
  }
  
  /**
   * Deselect all media
   */
  deselectAll(): void {
    this.selectedMedia.set(new Set());
  }
  
  /**
   * Delete single media
   */
  deleteMedia(mediaId: string): void {
    this.dialogService.confirmDelete('this image').subscribe(confirmed => {
      if (!confirmed) {
        return;
      }
      
      this.mediaService.deleteMedia(mediaId).subscribe({
        next: () => {
          this.notification.success('Image deleted successfully', 2000);
          this.allMedia.update(media => media.filter(m => m.id !== mediaId));
          this.selectedMedia.update(selected => {
            const newSet = new Set(selected);
            newSet.delete(mediaId);
            return newSet;
          });
        },
        error: (error) => {
          console.error('Error deleting media:', error);
          this.notification.error('Failed to delete image');
        }
      });
    });
  }
  
  /**
   * Delete selected media
   */
  deleteSelected(): void {
    const count = this.selectedMedia().size;
    
    this.dialogService.confirm({
      title: 'Delete Multiple Images',
      message: `Are you sure you want to delete ${count} selected image(s)? This action cannot be undone.`,
      confirmText: 'Delete All',
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'delete_forever'
    }).subscribe(confirmed => {
      if (!confirmed) {
        return;
      }
      
      const ids = Array.from(this.selectedMedia());
      
      this.mediaService.deleteMultipleMedia(ids).subscribe({
        next: () => {
          this.notification.success(`${count} image(s) deleted successfully`, 2000);
          this.allMedia.update(media => media.filter(m => !ids.includes(m.id)));
          this.deselectAll();
        },
        error: (error) => {
          console.error('Error deleting media:', error);
          this.notification.error('Failed to delete images');
        }
      });
    });
  }
  
  /**
   * Copy image URL to clipboard
   */
  copyUrl(url: string): void {
    navigator.clipboard.writeText(url).then(() => {
      this.notification.success('URL copied to clipboard', 2000);
    }).catch(() => {
      this.notification.error('Failed to copy URL', 2000);
    });
  }
  
  /**
   * Format file size
   */
  formatSize(bytes: number): string {
    return this.mediaService.formatFileSize(bytes);
  }
  
  /**
   * Format date
   */
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  /**
   * Check if media is selected
   */
  isSelected(mediaId: string): boolean {
    return this.selectedMedia().has(mediaId);
  }
  
  /**
   * Get product name by ID
   */
  getProductName(productId?: string): string {
    if (!productId) return 'Unassigned';
    const product = this.myProducts().find(p => p.id === productId);
    return product?.name || `Product ${productId}`;
  }
  
  /**
   * Filter media by product
   */
  filterMedia(filter: string): void {
    this.filterByProduct.set(filter);
  }
}

