import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

console.log('🚀 Starting Buy-01 Application...');
console.log('📦 App Config:', appConfig);

bootstrapApplication(App, appConfig)
  .then(() => {
    console.log('✅ Application bootstrapped successfully!');
  })
  .catch((err) => {
    console.error('❌ Bootstrap Error:', err);
  });
