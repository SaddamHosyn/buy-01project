/**
 * Production Environment Configuration
 * Used when building for production (ng build)
 */
export const environment = {
  production: true,
  apiUrl: 'https://localhost:8443/api', // API Gateway URL
  apiGatewayUrl: 'https://localhost:8443',
  
  // Service endpoints (routed through API Gateway)
  authUrl: 'https://localhost:8443/api/auth',
  usersUrl: 'https://localhost:8443/api/users',
  productsUrl: 'https://localhost:8443/api/products',
  mediaUrl: 'https://localhost:8443/api/media',
  
  // Feature flags
  enableMockData: false,
  enableDebugLogging: false
};
