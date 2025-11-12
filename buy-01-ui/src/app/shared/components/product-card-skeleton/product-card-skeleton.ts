import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonLoader } from '../skeleton/skeleton';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-product-card-skeleton',
  standalone: true,
  imports: [CommonModule, SkeletonLoader, MatCardModule],
  template: `
    <mat-card class="product-skeleton-card">
      <!-- Image Skeleton -->
      <app-skeleton type="rect" height="200px"></app-skeleton>
      
      <mat-card-content>
        <!-- Title Skeleton -->
        <app-skeleton type="title" width="80%" style="margin-top: 12px"></app-skeleton>
        
        <!-- Description Skeletons -->
        <app-skeleton type="text" width="100%" style="margin-top: 8px"></app-skeleton>
        <app-skeleton type="text" width="90%" style="margin-top: 4px"></app-skeleton>
        
        <!-- Price Skeleton -->
        <app-skeleton type="title" width="30%" style="margin-top: 16px"></app-skeleton>
        
        <!-- Button Skeleton -->
        <app-skeleton type="rect" height="36px" style="margin-top: 12px"></app-skeleton>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .product-skeleton-card {
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    mat-card-content {
      flex: 1;
      padding: 16px;
      display: flex;
      flex-direction: column;
    }
  `]
})
export class ProductCardSkeleton {}
