/**
 * Development Environment Configuration
 * Used during local development (ng serve)
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api', // API Gateway URL
  apiGatewayUrl: 'http://localhost:8080',
  
  // Service endpoints (routed through API Gateway)
  authUrl: 'http://localhost:8080/api/auth',
  usersUrl: 'http://localhost:8080/api/users',
  productsUrl: 'http://localhost:8080/api/products',
  mediaUrl: 'http://localhost:8080/api/media',
  
  // Feature flags
  enableMockData: false, // Set to true to use JSON server instead
  enableDebugLogging: true
};
