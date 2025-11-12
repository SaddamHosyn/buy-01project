/**
 * Production Environment Configuration
 * Used when building for production (ng build)
 */
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api', // API Gateway URL
  apiGatewayUrl: 'http://localhost:8080',
  
  // Service endpoints (routed through API Gateway)
  authUrl: 'http://localhost:8080/api/auth',
  usersUrl: 'http://localhost:8080/api/users',
  productsUrl: 'http://localhost:8080/api/products',
  mediaUrl: 'http://localhost:8080/api/media',
  
  // Feature flags
  enableMockData: false,
  enableDebugLogging: false
};
