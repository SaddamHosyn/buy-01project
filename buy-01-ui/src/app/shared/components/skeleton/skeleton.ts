import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="skeleton" 
      [class]="'skeleton-' + type"
      [ngStyle]="{
        width: width,
        height: height,
        borderRadius: borderRadius
      }">
    </div>
  `,
  styles: [`
    .skeleton {
      background: linear-gradient(
        90deg,
        #f0f0f0 0%,
        #e0e0e0 50%,
        #f0f0f0 100%
      );
      background-size: 200% 100%;
      animation: loading 1.5s infinite ease-in-out;
    }

    @keyframes loading {
      0% {
        background-position: 200% 0;
      }
      100% {
        background-position: -200% 0;
      }
    }

    .skeleton-text {
      height: 16px;
      border-radius: 4px;
    }

    .skeleton-title {
      height: 24px;
      border-radius: 4px;
    }

    .skeleton-heading {
      height: 32px;
      border-radius: 4px;
    }

    .skeleton-circle {
      border-radius: 50%;
    }

    .skeleton-rect {
      border-radius: 4px;
    }

    .skeleton-card {
      border-radius: 8px;
      height: 200px;
    }

    .skeleton-avatar {
      border-radius: 50%;
      width: 40px;
      height: 40px;
    }

    .skeleton-thumbnail {
      border-radius: 4px;
      width: 100px;
      height: 100px;
    }
  `]
})
export class SkeletonLoader {
  @Input() type: 'text' | 'title' | 'heading' | 'circle' | 'rect' | 'card' | 'avatar' | 'thumbnail' = 'text';
  @Input() width: string = '100%';
  @Input() height: string = 'auto';
  @Input() borderRadius: string = '';
}
