import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

export interface Media {
  id: string;
  url: string;
  fileName: string;
  size: number;
  contentType: string;
  sellerId?: string;
  productId?: string;
  createdAt: string;
}

export interface MediaUploadResponse {
  id: string;
  url: string;
  fileName: string;
  size: number;
  contentType: string;
}

export interface UploadProgress {
  fileName: string;
  progress: number; // 0-100
  status: 'uploading' | 'completed' | 'error';
  url?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private readonly http = inject(HttpClient);
  
  // JSON Server for development
  private readonly API_URL = 'http://localhost:3000/media';
  // When backend is ready, change to: 'http://localhost:8080/api/media'
  
  // Signals for state management
  private readonly mediaSignal = signal<Media[]>([]);
  private readonly uploadProgressSignal = signal<Map<string, UploadProgress>>(new Map());
  
  readonly media = this.mediaSignal.asReadonly();
  readonly uploadProgress = this.uploadProgressSignal.asReadonly();
  
  // Validation constants
  private readonly MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes
  private readonly ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  private readonly ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp'];
  
  /**
   * Validate file before upload
   */
  validateFile(file: File): { valid: boolean; error?: string } {
    // Check file type
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      return {
        valid: false,
        error: `Invalid file type. Only JPG, PNG, and WebP images are allowed. Got: ${file.type}`
      };
    }
    
    // Check file extension (additional validation)
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!this.ALLOWED_EXTENSIONS.includes(extension)) {
      return {
        valid: false,
        error: `Invalid file extension. Only .jpg, .jpeg, .png, and .webp are allowed.`
      };
    }
    
    // Check file size (2MB max)
    if (file.size > this.MAX_FILE_SIZE) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
      return {
        valid: false,
        error: `File size (${sizeMB}MB) exceeds the maximum limit of 2MB`
      };
    }
    
    return { valid: true };
  }
  
  /**
   * Validate multiple files
   */
  validateFiles(files: File[]): { valid: File[]; invalid: { file: File; error: string }[] } {
    const valid: File[] = [];
    const invalid: { file: File; error: string }[] = [];
    
    files.forEach(file => {
      const validation = this.validateFile(file);
      if (validation.valid) {
        valid.push(file);
      } else {
        invalid.push({ file, error: validation.error || 'Unknown error' });
      }
    });
    
    return { valid, invalid };
  }
  
  /**
   * Upload single file with progress tracking
   * CSR APPROACH: Convert to base64 and store in JSON Server
   */
  uploadFile(file: File, productId?: string): Observable<UploadProgress> {
    // Validate file first
    const validation = this.validateFile(file);
    if (!validation.valid) {
      return new Observable(observer => {
        observer.next({
          fileName: file.name,
          progress: 0,
          status: 'error',
          error: validation.error
        });
        observer.complete();
      });
    }
    
    // Initialize progress tracking
    const progress: UploadProgress = {
      fileName: file.name,
      progress: 0,
      status: 'uploading'
    };
    
    this.updateProgress(file.name, progress);
    
    // CSR: Convert file to base64 (simulating upload)
    return new Observable<UploadProgress>(observer => {
      const reader = new FileReader();
      
      // Simulate upload progress
      let simulatedProgress = 0;
      const progressInterval = setInterval(() => {
        simulatedProgress += 10;
        if (simulatedProgress <= 90) {
          progress.progress = simulatedProgress;
          this.updateProgress(file.name, progress);
          observer.next(progress);
        }
      }, 100);
      
      reader.onload = () => {
        clearInterval(progressInterval);
        
        const base64Url = reader.result as string;
        
        // Create media object
        const mediaData: Omit<Media, 'id'> = {
          url: base64Url,
          fileName: file.name,
          size: file.size,
          contentType: file.type,
          productId: productId,
          createdAt: new Date().toISOString()
        };
        
        // Save to JSON Server
        this.http.post<Media>(this.API_URL, {
          ...mediaData,
          id: Date.now().toString()
        }).subscribe({
          next: (savedMedia) => {
            progress.progress = 100;
            progress.status = 'completed';
            progress.url = savedMedia.url;
            
            this.updateProgress(file.name, progress);
            observer.next(progress);
            observer.complete();
            
            // Update media signal
            this.mediaSignal.update(media => [...media, savedMedia]);
          },
          error: (error) => {
            clearInterval(progressInterval);
            progress.status = 'error';
            progress.error = 'Failed to save media';
            
            this.updateProgress(file.name, progress);
            observer.next(progress);
            observer.complete();
          }
        });
      };
      
      reader.onerror = () => {
        clearInterval(progressInterval);
        progress.status = 'error';
        progress.error = 'Failed to read file';
        
        this.updateProgress(file.name, progress);
        observer.next(progress);
        observer.complete();
      };
      
      reader.readAsDataURL(file);
    });
  }
  
  /**
   * Upload multiple files with progress tracking
   */
  uploadFiles(files: File[], productId?: string): Observable<UploadProgress[]> {
    // Validate all files first
    const { valid, invalid } = this.validateFiles(files);
    
    // Create error progress for invalid files
    const errorProgress: UploadProgress[] = invalid.map(({ file, error }) => ({
      fileName: file.name,
      progress: 0,
      status: 'error',
      error
    }));
    
    return new Observable<UploadProgress[]>(observer => {
      if (valid.length === 0) {
        observer.next(errorProgress);
        observer.complete();
        return;
      }
      
      const allProgress: UploadProgress[] = [...errorProgress];
      let completed = 0;
      
      // Upload each valid file
      valid.forEach(file => {
        this.uploadFile(file, productId).subscribe({
          next: (progress) => {
            // Update progress array
            const index = allProgress.findIndex(p => p.fileName === file.name);
            if (index >= 0) {
              allProgress[index] = progress;
            } else {
              allProgress.push(progress);
            }
            
            observer.next([...allProgress]);
            
            if (progress.status !== 'uploading') {
              completed++;
              if (completed === valid.length) {
                observer.complete();
              }
            }
          }
        });
      });
    });
  }
  
  /**
   * Update upload progress signal
   */
  private updateProgress(fileName: string, progress: UploadProgress): void {
    this.uploadProgressSignal.update(map => {
      const newMap = new Map(map);
      newMap.set(fileName, progress);
      return newMap;
    });
  }
  
  /**
   * Clear upload progress for a file
   */
  clearProgress(fileName: string): void {
    this.uploadProgressSignal.update(map => {
      const newMap = new Map(map);
      newMap.delete(fileName);
      return newMap;
    });
  }
  
  /**
   * Clear all upload progress
   */
  clearAllProgress(): void {
    this.uploadProgressSignal.set(new Map());
  }
  
  /**
   * Get all media (public)
   */
  getAllMedia(): Observable<Media[]> {
    return this.http.get<Media[]>(this.API_URL).pipe(
      tap(media => this.mediaSignal.set(media))
    );
  }
  
  /**
   * Get media by ID
   */
  getMediaById(id: string): Observable<Media> {
    return this.http.get<Media>(`${this.API_URL}/${id}`);
  }
  
  /**
   * Get media by product ID
   * CSR APPROACH: Get all media, filter client-side
   */
  getMediaByProduct(productId: string): Observable<Media[]> {
    return this.http.get<Media[]>(this.API_URL).pipe(
      map(media => media.filter(m => m.productId === productId))
    );
  }
  
  /**
   * Delete media by ID
   */
  deleteMedia(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`).pipe(
      tap(() => {
        // Update signal - remove deleted media
        this.mediaSignal.update(media => media.filter(m => m.id !== id));
      })
    );
  }
  
  /**
   * Delete multiple media items
   */
  deleteMultipleMedia(ids: string[]): Observable<void[]> {
    return new Observable(observer => {
      const deletions = ids.map(id => this.deleteMedia(id));
      
      let completed = 0;
      const results: void[] = [];
      
      deletions.forEach((deletion, index) => {
        deletion.subscribe({
          next: () => {
            results.push();
            completed++;
            
            if (completed === deletions.length) {
              observer.next(results);
              observer.complete();
            }
          },
          error: (error) => {
            console.error(`Failed to delete media ${ids[index]}:`, error);
            completed++;
            
            if (completed === deletions.length) {
              observer.next(results);
              observer.complete();
            }
          }
        });
      });
    });
  }
  
  /**
   * Get file size in human-readable format
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }
  
  /**
   * Check if file type is allowed
   */
  isFileTypeAllowed(file: File): boolean {
    return this.ALLOWED_TYPES.includes(file.type);
  }
  
  /**
   * Check if file size is within limit
   */
  isFileSizeValid(file: File): boolean {
    return file.size <= this.MAX_FILE_SIZE;
  }
  
  /**
   * Get maximum file size
   */
  getMaxFileSize(): number {
    return this.MAX_FILE_SIZE;
  }
  
  /**
   * Get allowed file types
   */
  getAllowedTypes(): string[] {
    return [...this.ALLOWED_TYPES];
  }
  
  /**
   * Get allowed file extensions
   */
  getAllowedExtensions(): string[] {
    return [...this.ALLOWED_EXTENSIONS];
  }
}
