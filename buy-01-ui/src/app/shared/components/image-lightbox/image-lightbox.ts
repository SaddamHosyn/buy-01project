import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface LightboxData {
  images: string[];
  initialIndex: number;
  title?: string;
}

@Component({
  selector: 'app-image-lightbox',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="lightbox-container">
      <!-- Close Button -->
      <button 
        mat-icon-button 
        class="close-button" 
        (click)="close()"
        matTooltip="Close">
        <mat-icon>close</mat-icon>
      </button>

      <!-- Title -->
      @if (data.title) {
        <div class="lightbox-title">
          <h2>{{ data.title }}</h2>
        </div>
      }

      <!-- Image Display -->
      <div class="image-container">
        <!-- Previous Button -->
        @if (data.images.length > 1) {
          <button 
            mat-icon-button 
            class="nav-button prev"
            (click)="previousImage()"
            [disabled]="currentIndex() === 0"
            matTooltip="Previous">
            <mat-icon>chevron_left</mat-icon>
          </button>
        }

        <!-- Main Image -->
        <div class="main-image-wrapper">
          <img 
            [src]="currentImage()" 
            [alt]="'Image ' + (currentIndex() + 1)"
            class="main-image"
            (click)="$event.stopPropagation()" />
        </div>

        <!-- Next Button -->
        @if (data.images.length > 1) {
          <button 
            mat-icon-button 
            class="nav-button next"
            (click)="nextImage()"
            [disabled]="currentIndex() === data.images.length - 1"
            matTooltip="Next">
            <mat-icon>chevron_right</mat-icon>
          </button>
        }
      </div>

      <!-- Image Counter & Thumbnails -->
      @if (data.images.length > 1) {
        <div class="lightbox-footer">
          <!-- Counter -->
          <div class="image-counter">
            {{ currentIndex() + 1 }} / {{ data.images.length }}
          </div>

          <!-- Thumbnails -->
          <div class="thumbnails-container">
            @for (image of data.images; track image; let i = $index) {
              <div 
                class="thumbnail"
                [class.active]="i === currentIndex()"
                (click)="selectImage(i)">
                <img [src]="image" [alt]="'Thumbnail ' + (i + 1)" />
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .lightbox-container {
      position: relative;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      background: #000;
      color: #fff;
    }

    .close-button {
      position: absolute;
      top: 16px;
      right: 16px;
      z-index: 1000;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      
      &:hover {
        background: rgba(0, 0, 0, 0.8);
      }
    }

    .lightbox-title {
      position: absolute;
      top: 16px;
      left: 16px;
      z-index: 999;
      
      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
      }
    }

    .image-container {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      padding: 60px 80px 20px;
      min-height: 400px;
    }

    .main-image-wrapper {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      max-height: 100%;
    }

    .main-image {
      max-width: 100%;
      max-height: 70vh;
      object-fit: contain;
      cursor: default;
      user-select: none;
      -webkit-user-drag: none;
    }

    .nav-button {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      width: 56px;
      height: 56px;
      
      &:hover:not(:disabled) {
        background: rgba(0, 0, 0, 0.8);
      }
      
      &:disabled {
        opacity: 0.3;
        cursor: not-allowed;
      }
      
      &.prev {
        left: 16px;
      }
      
      &.next {
        right: 16px;
      }
      
      mat-icon {
        font-size: 36px;
        width: 36px;
        height: 36px;
      }
    }

    .lightbox-footer {
      padding: 16px;
      background: rgba(0, 0, 0, 0.8);
      display: flex;
      flex-direction: column;
      gap: 12px;
      align-items: center;
    }

    .image-counter {
      font-size: 14px;
      font-weight: 500;
      opacity: 0.9;
    }

    .thumbnails-container {
      display: flex;
      gap: 8px;
      overflow-x: auto;
      max-width: 100%;
      padding: 4px;
      
      &::-webkit-scrollbar {
        height: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.3);
        border-radius: 3px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.5);
        }
      }
    }

    .thumbnail {
      flex-shrink: 0;
      width: 60px;
      height: 60px;
      cursor: pointer;
      border: 2px solid transparent;
      border-radius: 4px;
      overflow: hidden;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: rgba(255, 255, 255, 0.5);
        transform: scale(1.05);
      }
      
      &.active {
        border-color: #fff;
        box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    /* Responsive */
    @media (max-width: 768px) {
      .image-container {
        padding: 60px 16px 20px;
      }
      
      .nav-button {
        width: 40px;
        height: 40px;
        
        mat-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
        }
        
        &.prev {
          left: 8px;
        }
        
        &.next {
          right: 8px;
        }
      }
      
      .thumbnail {
        width: 50px;
        height: 50px;
      }
    }
  `]
})
export class ImageLightbox {
  readonly data = inject<LightboxData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ImageLightbox>);

  readonly currentIndex = signal<number>(this.data.initialIndex);
  readonly currentImage = signal<string>(this.data.images[this.data.initialIndex]);

  previousImage(): void {
    if (this.currentIndex() > 0) {
      const newIndex = this.currentIndex() - 1;
      this.currentIndex.set(newIndex);
      this.currentImage.set(this.data.images[newIndex]);
    }
  }

  nextImage(): void {
    if (this.currentIndex() < this.data.images.length - 1) {
      const newIndex = this.currentIndex() + 1;
      this.currentIndex.set(newIndex);
      this.currentImage.set(this.data.images[newIndex]);
    }
  }

  selectImage(index: number): void {
    this.currentIndex.set(index);
    this.currentImage.set(this.data.images[index]);
  }

  close(): void {
    this.dialogRef.close();
  }
}
