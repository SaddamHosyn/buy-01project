import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, forkJoin } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Media {
  id: string;
  url: string;
  originalFilename: string;
  size: number;
  contentType: string;
  userId?: string;
  productId?: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.mediaUrl;
  private readonly mediaSignal = signal<Media[]>([]);
  readonly media = this.mediaSignal.asReadonly();
  private readonly MAX_FILE_SIZE = 2 * 1024 * 1024;
  private readonly ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  private readonly ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp'];

  validateFile(file: File): { valid: boolean; error?: string } {
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      return { valid: false, error: `Invalid file type: ${file.type}` };
    }
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!this.ALLOWED_EXTENSIONS.includes(extension)) {
      return { valid: false, error: 'Invalid file extension' };
    }
    if (file.size > this.MAX_FILE_SIZE) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
      return { valid: false, error: `File size (${sizeMB}MB) exceeds 2MB limit` };
    }
    return { valid: true };
  }

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

  uploadFile(file: File): Observable<Media> {
    const validation = this.validateFile(file);
    if (!validation.valid) {
      throw new Error(validation.error);
    }
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Media>(`${this.API_URL}/images`, formData).pipe(
      tap(media => this.mediaSignal.update(mediaList => [...mediaList, media]))
    );
  }

  uploadFiles(files: File[]): Observable<Media[]> {
    const { valid, invalid } = this.validateFiles(files);
    if (invalid.length > 0) {
      const errors = invalid.map(i => `${i.file.name}: ${i.error}`).join('; ');
      throw new Error(`Some files are invalid: ${errors}`);
    }
    const uploads = valid.map(file => this.uploadFile(file));
    return forkJoin(uploads);
  }

  getMediaUrl(id: string): string {
    return `${this.API_URL}/images/${id}`;
  }

  /**
   * Get all media files
   */
  getAllMedia(): Observable<Media[]> {
    return this.http.get<Media[]>(`${this.API_URL}/images`).pipe(
      tap(media => this.mediaSignal.set(media))
    );
  }

  deleteMedia(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/images/${id}`).pipe(
      tap(() => this.mediaSignal.update(media => media.filter(m => m.id !== id)))
    );
  }

  /**
   * Delete multiple media files
   */
  deleteMediaFiles(ids: string[]): Observable<void[]> {
    const deletions = ids.map(id => this.deleteMedia(id));
    return forkJoin(deletions);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  isFileTypeAllowed(file: File): boolean {
    return this.ALLOWED_TYPES.includes(file.type);
  }

  isFileSizeValid(file: File): boolean {
    return file.size <= this.MAX_FILE_SIZE;
  }

  getMaxFileSize(): number {
    return this.MAX_FILE_SIZE;
  }

  getAllowedTypes(): string[] {
    return [...this.ALLOWED_TYPES];
  }

  getAllowedExtensions(): string[] {
    return [...this.ALLOWED_EXTENSIONS];
  }
}
