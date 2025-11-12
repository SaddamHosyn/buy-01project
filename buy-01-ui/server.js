const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();
const port = 3000;

// Middleware to simulate authentication
server.use(jsonServer.bodyParser);

// CORS configuration
server.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  next();
});

// Login endpoint
server.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  const db = router.db; // Access to lowdb instance
  
  const user = db.get('users')
    .find({ email: email, password: password })
    .value();
  
  if (user) {
    // Return user without password and a fake JWT token
    const { password: _, ...userWithoutPassword } = user;
    res.json({
      user: userWithoutPassword,
      token: `fake-jwt-token-${user.id}-${Date.now()}`
    });
  } else {
    res.status(401).json({ message: 'Invalid email or password' });
  }
});

// Register endpoint
server.post('/api/auth/register', (req, res) => {
  const { email, password, name, role, avatar } = req.body;
  const db = router.db;
  
  // Check if user already exists
  const existingUser = db.get('users')
    .find({ email: email })
    .value();
  
  if (existingUser) {
    return res.status(400).json({ message: 'User already exists' });
  }
  
  // Create new user
  const newUser = {
    id: String(Date.now()),
    email,
    password,
    name,
    role,
    avatar: avatar || null,
    createdAt: new Date().toISOString()
  };
  
  db.get('users')
    .push(newUser)
    .write();
  
  const { password: _, ...userWithoutPassword } = newUser;
  res.status(201).json({
    user: userWithoutPassword,
    token: `fake-jwt-token-${newUser.id}-${Date.now()}`
  });
});

// Get seller's products
server.get('/api/products/seller/me', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  // Extract seller ID from fake token (fake-jwt-token-{sellerId}-{timestamp})
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const sellerProducts = db.get('products')
    .filter({ sellerId: sellerId })
    .value();
  
  res.json(sellerProducts);
});

// Get all products (public)
server.get('/api/products', (req, res) => {
  const db = router.db;
  const products = db.get('products').value();
  res.json(products);
});

// Get product by ID
server.get('/api/products/:id', (req, res) => {
  const db = router.db;
  const product = db.get('products')
    .find({ id: req.params.id })
    .value();
  
  if (product) {
    res.json(product);
  } else {
    res.status(404).json({ message: 'Product not found' });
  }
});

// Create product (sellers only)
server.post('/api/products', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const newProduct = {
    id: String(Date.now()),
    ...req.body,
    sellerId: sellerId,
    imageUrls: req.body.imageUrls || [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  
  db.get('products')
    .push(newProduct)
    .write();
  
  res.status(201).json(newProduct);
});

// Update product (own products only)
server.put('/api/products/:id', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const product = db.get('products')
    .find({ id: req.params.id })
    .value();
  
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  
  if (product.sellerId !== sellerId) {
    return res.status(403).json({ message: 'Forbidden: You can only edit your own products' });
  }
  
  const updatedProduct = {
    ...product,
    ...req.body,
    id: product.id,
    sellerId: product.sellerId,
    updatedAt: new Date().toISOString()
  };
  
  db.get('products')
    .find({ id: req.params.id })
    .assign(updatedProduct)
    .write();
  
  res.json(updatedProduct);
});

// Delete product (own products only)
server.delete('/api/products/:id', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const product = db.get('products')
    .find({ id: req.params.id })
    .value();
  
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  
  if (product.sellerId !== sellerId) {
    return res.status(403).json({ message: 'Forbidden: You can only delete your own products' });
  }
  
  db.get('products')
    .remove({ id: req.params.id })
    .write();
  
  res.status(204).send();
});

// Add images to product
server.post('/api/products/:id/images', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const product = db.get('products')
    .find({ id: req.params.id })
    .value();
  
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  
  if (product.sellerId !== sellerId) {
    return res.status(403).json({ message: 'Forbidden' });
  }
  
  // Simulate image upload - in reality, you'd save files
  const newImageUrls = req.body.imageUrls || [];
  const updatedImageUrls = [...(product.imageUrls || []), ...newImageUrls];
  
  db.get('products')
    .find({ id: req.params.id })
    .assign({ imageUrls: updatedImageUrls, updatedAt: new Date().toISOString() })
    .write();
  
  res.json(newImageUrls);
});

// Delete product image
server.delete('/api/products/:id/images', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const { imageUrl } = req.body;
  const db = router.db;
  
  const product = db.get('products')
    .find({ id: req.params.id })
    .value();
  
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  
  if (product.sellerId !== sellerId) {
    return res.status(403).json({ message: 'Forbidden' });
  }
  
  const updatedImageUrls = product.imageUrls.filter(url => url !== imageUrl);
  
  db.get('products')
    .find({ id: req.params.id })
    .assign({ imageUrls: updatedImageUrls, updatedAt: new Date().toISOString() })
    .write();
  
  res.status(204).send();
});

// Media endpoints
server.get('/api/media/seller/me', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  const db = router.db;
  
  const sellerMedia = db.get('media')
    .filter({ sellerId: sellerId })
    .value();
  
  res.json(sellerMedia);
});

// Upload media (simulated)
server.post('/api/media/upload', (req, res) => {
  const authHeader = req.headers.authorization;
  
  if (!authHeader) {
    return res.status(401).json({ message: 'Unauthorized' });
  }
  
  const sellerId = authHeader.split('-')[3];
  
  // Simulate file upload
  res.json({
    url: `https://images.unsplash.com/photo-${Date.now()}?w=800`,
    fileName: req.body.fileName || `image-${Date.now()}.jpg`,
    size: Math.floor(Math.random() * 2000000), // Random size < 2MB
    contentType: 'image/jpeg'
  });
});

// Use default middleware
server.use(middlewares);

// Use the router
server.use(router);

// Start server
server.listen(port, () => {
  console.log(`\n🚀 JSON Server is running!`);
  console.log(`📍 API: http://localhost:${port}/api`);
  console.log(`📦 Resources: http://localhost:${port}/db`);
  console.log(`\n📚 Available endpoints:`);
  console.log(`   POST   http://localhost:${port}/api/auth/login`);
  console.log(`   POST   http://localhost:${port}/api/auth/register`);
  console.log(`   GET    http://localhost:${port}/api/products`);
  console.log(`   GET    http://localhost:${port}/api/products/:id`);
  console.log(`   GET    http://localhost:${port}/api/products/seller/me`);
  console.log(`   POST   http://localhost:${port}/api/products`);
  console.log(`   PUT    http://localhost:${port}/api/products/:id`);
  console.log(`   DELETE http://localhost:${port}/api/products/:id`);
  console.log(`   POST   http://localhost:${port}/api/media/upload`);
  console.log(`\n💡 Test credentials:`);
  console.log(`   Seller: seller@test.com / password123`);
  console.log(`   Client: client@test.com / password123\n`);
});
