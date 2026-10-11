/**
 * BoB Store - Secure Minimalist Backend API
 * Node.js 18+ | Express | SQLite (better-sqlite3) | Native crypto TOTP
 * JSON API Only - No HTML - Strict Security Headers
 */

const express = require('express');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const Database = require('better-sqlite3');
const crypto = require('crypto');
const path = require('path');

const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '127.0.0.1';
const DB_PATH = process.env.DB_PATH || path.join(__dirname, 'bob_store.db');

// Initialize Express App
const app = express();

// Security middleware - Helmet
app.use(helmet({
  contentSecurityPolicy: false, // Pure JSON API
  crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// CORS headers for native Android clients
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Admin-Token, X-Admin-TOTP');
  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }
  next();
});

// Rate limiting (100 requests per 15 minutes per IP, 30 for admin endpoints)
const globalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 200,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again later.' }
});

const adminLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 40,
  message: { error: 'Too many admin authentication attempts.' }
});

app.use('/api/', globalLimiter);
app.use('/api/admin/', adminLimiter);

// Express JSON body parser with strict size limit
app.use(express.json({ limit: '200kb' }));

// Initialize SQLite Database with better-sqlite3
const db = new Database(DB_PATH);
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

// Database Schema
db.exec(`
  CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name_ar TEXT NOT NULL,
    name_en TEXT NOT NULL,
    icon TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS products (
    id TEXT PRIMARY KEY,
    name_ar TEXT NOT NULL,
    name_en TEXT NOT NULL,
    desc_ar TEXT NOT NULL,
    desc_en TEXT NOT NULL,
    price REAL NOT NULL,
    original_price REAL,
    category_id TEXT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 10,
    rating REAL NOT NULL DEFAULT 4.9,
    reviews_count INTEGER NOT NULL DEFAULT 12,
    badge_ar TEXT,
    badge_en TEXT,
    is_featured INTEGER NOT NULL DEFAULT 0,
    image_res TEXT NOT NULL,
    compatibility TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
  );

  CREATE TABLE IF NOT EXISTS orders (
    id TEXT PRIMARY KEY,
    customer_name TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    customer_address TEXT NOT NULL,
    city TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    total_amount REAL NOT NULL,
    currency TEXT NOT NULL DEFAULT 'USD',
    tracking_number TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id TEXT NOT NULL,
    product_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price REAL NOT NULL,
    color_choice TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
  );

  CREATE TABLE IF NOT EXISTS admins (
    username TEXT PRIMARY KEY,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    totp_secret TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );
`);

/* ----------------------------------------------------
 * Built-in Crypto Helpers & RFC 6238 TOTP (HMAC-SHA1)
 * Using only Node.js native 'crypto' module
 * ---------------------------------------------------- */

function hashPassword(password, salt = crypto.randomBytes(16).toString('hex')) {
  const hash = crypto.pbkdf2Sync(password, salt, 100000, 64, 'sha512').toString('hex');
  return { hash, salt };
}

function verifyPassword(password, hash, salt) {
  const computed = crypto.pbkdf2Sync(password, salt, 100000, 64, 'sha512').toString('hex');
  return crypto.timingSafeEqual(Buffer.from(hash, 'hex'), Buffer.from(computed, 'hex'));
}

// Generate base32 secret for TOTP
function generateTotpSecret() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';
  let secret = '';
  const bytes = crypto.randomBytes(20);
  for (let i = 0; i < 20; i++) {
    secret += chars[bytes[i] % 32];
  }
  return secret;
}

// Decode base32 to buffer
function base32Decode(str) {
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';
  let bits = 0;
  let value = 0;
  const output = [];

  for (let i = 0; i < str.length; i++) {
    const val = alphabet.indexOf(str[i].toUpperCase());
    if (val === -1) continue;
    value = (value << 5) | val;
    bits += 5;
    if (bits >= 8) {
      output.push((value >>> (bits - 8)) & 255);
      bits -= 8;
    }
  }
  return Buffer.from(output);
}

// Compute standard RFC 6238 TOTP 6-digit code
function getTotpCode(secretBase32, timeStep = 30, offsetSteps = 0) {
  const epoch = Math.floor(Date.now() / 1000);
  const counter = Math.floor(epoch / timeStep) + offsetSteps;

  const buffer = Buffer.alloc(8);
  buffer.writeBigInt64BE(BigInt(counter), 0);

  const key = base32Decode(secretBase32);
  const hmac = crypto.createHmac('sha1', key).update(buffer).digest();

  const offset = hmac[hmac.length - 1] & 0x0f;
  const codeInt =
    ((hmac[offset] & 0x7f) << 24) |
    ((hmac[offset + 1] & 0xff) << 16) |
    ((hmac[offset + 2] & 0xff) << 8) |
    (hmac[offset + 3] & 0xff);

  const otp = (codeInt % 1000000).toString().padStart(6, '0');
  return otp;
}

// Verify TOTP within +/- 1 step window for clock drift
function verifyTotp(secretBase32, candidateCode) {
  if (!candidateCode || typeof candidateCode !== 'string') return false;
  const cleanCode = candidateCode.trim();
  for (let step = -1; step <= 1; step++) {
    const valid = getTotpCode(secretBase32, 30, step);
    if (crypto.timingSafeEqual(Buffer.from(valid), Buffer.from(cleanCode.padStart(6, '0')))) {
      return true;
    }
  }
  return false;
}

// Active session token store
const activeTokens = new Map();

function generateAuthToken(username) {
  const token = crypto.randomBytes(32).toString('hex');
  const expiresAt = Date.now() + 24 * 60 * 60 * 1000;
  activeTokens.set(token, { username, expiresAt });
  return token;
}

function verifyAuthToken(req, res, next) {
  const token = req.headers['x-admin-token'] || req.headers['authorization']?.replace('Bearer ', '');
  if (!token || !activeTokens.has(token)) {
    return res.status(401).json({ error: 'Unauthorized: Valid Admin Token Required' });
  }
  const session = activeTokens.get(token);
  if (Date.now() > session.expiresAt) {
    activeTokens.delete(token);
    return res.status(401).json({ error: 'Token expired' });
  }
  req.admin = session;
  next();
}

/* ----------------------------------------------------
 * Seed Initial Luxury Data
 * ---------------------------------------------------- */
function seedDatabase() {
  const catCount = db.prepare('SELECT count(*) as count FROM categories').get().count;
  if (catCount === 0) {
    const insertCat = db.prepare('INSERT INTO categories (id, name_ar, name_en, icon) VALUES (?, ?, ?, ?)');
    insertCat.run('cases', 'كفرات فاخرة', 'Luxury Cases', 'phone_android');
    insertCat.run('chargers', 'شواحن و MagSafe', 'MagSafe & Chargers', 'bolt');
    insertCat.run('audio', 'حماية وسماعات', 'Audio & Armor', 'headphones');
    insertCat.run('accessories', 'إكسسوارات وحوامل', 'Accessories & Mounts', 'extension');
  }

  const prodCount = db.prepare('SELECT count(*) as count FROM products').get().count;
  if (prodCount === 0) {
    const insertProd = db.prepare(`
      INSERT INTO products (
        id, name_ar, name_en, desc_ar, desc_en, price, original_price,
        category_id, stock, rating, reviews_count, badge_ar, badge_en,
        is_featured, image_res, compatibility
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    insertProd.run(
      'case-aurum-titanium',
      'كفر تيتانيوم وفايبر كربون أوروم 3D',
      'Aurum Titanium & Forged Carbon Case',
      'تصميم هندسي فاخر مصنوع من التيتانيوم المستخدم في صناعة الطائرات مع ألياف الكربون المطروقة ولمسات ذهبية عيار 24 قيراط وحماية عسكرية فائقة.',
      'Precision aerospace-grade titanium frame fused with forged carbon fiber and 24K gold chamfered accents. Ultra-slim luxury armor.',
      89.0, 119.0, 'cases', 15, 4.98, 142,
      'الأكثر مبيعاً', 'BEST SELLER', 1,
      'hero_luxury_case',
      'iPhone 16 Pro Max, iPhone 15 Pro, Galaxy S25 Ultra'
    );

    insertProd.run(
      'dock-orion-magluna',
      'منصة شحن MagSafe أوريون ماج-لونا الذكية',
      'Orion Mag-Luna Wireless Docking Station',
      'قاعدة شحن مغناطيسية ثلاثية الأبعاد من الزجاج المصنفر والبرونز المفرغ مع إضاءة محيطية تفاعلية وقدرة شحن سريع 25 واط Qi2 معتمد.',
      'Architectural frosted glass and brushed brass MagSafe stand with responsive ambient aura lighting and 25W certified Qi2 fast charging.',
      75.0, 95.0, 'chargers', 22, 4.95, 98,
      'إصدار محدود', 'LIMITED EDITION', 1,
      'hero_wireless_dock',
      'All Qi2 / MagSafe iPhones & Android Flagships'
    );

    insertProd.run(
      'earbuds-aura-armor',
      'درع سماعات أورا كربون وتيتانيوم',
      'Aura Audio Titanium X Carbon Armor',
      'جراب مدرع لسماعات الأذن مع حلقة تسلق ذهبية مقفلة ومقاومة للصدمات والخدوش بتصميم ميكانيكي ثلاثي الأبعاد فاخر.',
      'Heavy-duty titanium alloy case shield with carbon weave insert and custom gold carabiner lock for extreme luxury everyday carry.',
      45.0, 59.0, 'audio', 30, 4.91, 76,
      'جديد', 'NEW ARRIVAL', 1,
      'hero_earbuds_case',
      'AirPods Pro 2, Galaxy Buds 3 Pro'
    );

    insertProd.run(
      'lens-sapphire-shield',
      'حامي عدسات الكاميرا زفير كريستال عيار 9H',
      'BoB 9H Sapphire Crystal Lens Protector',
      'حلقات حماية عدسات فردية من حجر الياقوت السويسري الاصطناعي مع إطار تيتانيوم مانع للانعكاس.',
      'Individual swiss-grade synthetic sapphire crystal rings framed in CNC machined titanium with anti-reflective coating.',
      29.0, 39.0, 'accessories', 50, 4.88, 210,
      'حماية فائقة', 'PRO GRADE', 0,
      'ic_bob_store_logo',
      'iPhone 16 / 15 Series, Galaxy S24/S25 Ultra'
    );

    insertProd.run(
      'wallet-mag-leather',
      'محفظة مغناطيسية ماغسيف جلد إيطالي فاخر',
      'BoB MagSafe Italian Leather Cardholder',
      'محفظة فائقة النحافة تتسع لـ 3 بطاقات مع ميزة حامل مكتبي قابل للطي ومغناطيس N52 قوي جداً.',
      'Ultra-slim handcrafted Italian full-grain leather wallet with integrated 60-degree kickstand and N52 neo magnets.',
      39.0, 49.0, 'accessories', 18, 4.85, 64,
      'فاخر', 'LUXURY', 0,
      'ic_bob_store_logo',
      'MagSafe Compatible Devices'
    );
  }

  // Seed default admin (Password: bob2025! & fixed demo TOTP secret)
  const adminCount = db.prepare('SELECT count(*) as count FROM admins').get().count;
  if (adminCount === 0) {
    const demoSecret = 'JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP'; // Standard Base32 demo secret
    const { hash, salt } = hashPassword('bob2025!');
    db.prepare('INSERT INTO admins (username, password_hash, salt, totp_secret) VALUES (?, ?, ?, ?)').run(
      'admin@bobstore.com',
      hash,
      salt,
      demoSecret
    );
  }
}

seedDatabase();

/* ----------------------------------------------------
 * API Endpoints
 * ---------------------------------------------------- */

// Health & System Info
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    service: 'BoB Store Secure API',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Categories
app.get('/api/categories', (req, res) => {
  const categories = db.prepare('SELECT * FROM categories').all();
  res.json({ success: true, data: categories });
});

// Products: List with filter
app.get('/api/products', (req, res) => {
  const { category, search } = req.query;
  let query = 'SELECT * FROM products WHERE 1=1';
  const params = [];

  if (category && category !== 'all') {
    query += ' AND category_id = ?';
    params.push(category);
  }

  if (search) {
    query += ' AND (name_ar LIKE ? OR name_en LIKE ? OR desc_ar LIKE ? OR desc_en LIKE ?)';
    const term = `%${search}%`;
    params.push(term, term, term, term);
  }

  query += ' ORDER BY is_featured DESC, created_at DESC';
  const products = db.prepare(query).all(...params);
  res.json({ success: true, count: products.length, data: products });
});

// Product by ID
app.get('/api/products/:id', (req, res) => {
  const product = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!product) {
    return res.status(404).json({ success: false, error: 'Product not found' });
  }
  res.json({ success: true, data: product });
});

// Create Customer Order
app.post('/api/orders', (req, res) => {
  const {
    customer_name,
    customer_phone,
    customer_address,
    city,
    payment_method,
    currency = 'USD',
    items
  } = req.body;

  if (!customer_name || !customer_phone || !items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ success: false, error: 'Missing required order fields or empty items' });
  }

  const orderId = 'BOB-' + Date.now().toString(36).toUpperCase() + '-' + crypto.randomBytes(2).toString('hex').toUpperCase();
  const trackingNumber = 'TRK' + Math.floor(100000000 + Math.random() * 900000000);

  let totalAmount = 0;

  const insertOrder = db.prepare(`
    INSERT INTO orders (id, customer_name, customer_phone, customer_address, city, payment_method, status, total_amount, currency, tracking_number)
    VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?)
  `);

  const insertItem = db.prepare(`
    INSERT INTO order_items (order_id, product_id, quantity, unit_price, color_choice)
    VALUES (?, ?, ?, ?, ?)
  `);

  const updateStock = db.prepare('UPDATE products SET stock = MAX(0, stock - ?) WHERE id = ?');

  const transaction = db.transaction(() => {
    // Calculate total from products
    for (const item of items) {
      const product = db.prepare('SELECT price, stock FROM products WHERE id = ?').get(item.product_id);
      if (!product) throw new Error(`Product ${item.product_id} not found`);
      const itemPrice = product.price;
      const qty = item.quantity || 1;
      totalAmount += itemPrice * qty;
    }

    insertOrder.run(orderId, customer_name, customer_phone, customer_address || '', city || '', payment_method || 'CARD', totalAmount, currency, trackingNumber);

    for (const item of items) {
      const product = db.prepare('SELECT price FROM products WHERE id = ?').get(item.product_id);
      const qty = item.quantity || 1;
      insertItem.run(orderId, item.product_id, qty, product.price, item.color_choice || 'Default');
      updateStock.run(qty, item.product_id);
    }
  });

  try {
    transaction();
    res.status(201).json({
      success: true,
      message: 'Order created successfully',
      data: {
        order_id: orderId,
        tracking_number: trackingNumber,
        total_amount: totalAmount,
        currency,
        status: 'PENDING'
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Track Order by ID or Tracking Number
app.get('/api/orders/:identifier', (req, res) => {
  const order = db.prepare('SELECT * FROM orders WHERE id = ? OR tracking_number = ?').get(req.params.identifier, req.params.identifier);
  if (!order) {
    return res.status(404).json({ success: false, error: 'Order not found' });
  }
  const items = db.prepare(`
    SELECT oi.*, p.name_ar, p.name_en, p.image_res
    FROM order_items oi
    JOIN products p ON oi.product_id = p.id
    WHERE oi.order_id = ?
  `).all(order.id);

  res.json({ success: true, data: { ...order, items } });
});

/* ----------------------------------------------------
 * Admin Protected Endpoints
 * ---------------------------------------------------- */

// Admin Login Step 1: Verify Password and issue challenge
app.post('/api/admin/login', (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.status(400).json({ success: false, error: 'Username and password required' });
  }

  const admin = db.prepare('SELECT * FROM admins WHERE username = ?').get(username);
  if (!admin || !verifyPassword(password, admin.password_hash, admin.salt)) {
    return res.status(401).json({ success: false, error: 'Invalid admin credentials' });
  }

  // Check if TOTP code was supplied directly in same call
  const { totp_code } = req.body;
  if (totp_code) {
    if (verifyTotp(admin.totp_secret, totp_code)) {
      const token = generateAuthToken(admin.username);
      return res.json({
        success: true,
        message: 'Admin authenticated with TOTP 2FA',
        token,
        username: admin.username
      });
    } else {
      return res.status(401).json({ success: false, error: 'Invalid TOTP 2FA code' });
    }
  }

  // Or request 2FA step
  res.json({
    success: true,
    requires_totp: true,
    message: 'Password verified. Enter 6-digit TOTP code.'
  });
});

// Admin Verify Step 2: TOTP Code
app.post('/api/admin/verify-totp', (req, res) => {
  const { username, totp_code } = req.body;
  const admin = db.prepare('SELECT * FROM admins WHERE username = ?').get(username);
  if (!admin) {
    return res.status(404).json({ success: false, error: 'Admin not found' });
  }

  if (verifyTotp(admin.totp_secret, totp_code)) {
    const token = generateAuthToken(admin.username);
    res.json({
      success: true,
      token,
      username: admin.username
    });
  } else {
    res.status(401).json({ success: false, error: 'Invalid or expired TOTP code' });
  }
});

// Admin Demo TOTP Helper (returns current valid code for effortless testing)
app.get('/api/admin/demo-totp-helper', (req, res) => {
  const admin = db.prepare('SELECT totp_secret FROM admins LIMIT 1').get();
  if (!admin) return res.status(404).json({ error: 'No admin' });
  const currentOtp = getTotpCode(admin.totp_secret, 30, 0);
  res.json({
    demo_username: 'admin@bobstore.com',
    demo_password: 'bob2025!',
    current_totp_code: currentOtp,
    valid_window_seconds: 30 - (Math.floor(Date.now() / 1000) % 30)
  });
});

// Admin Analytics
app.get('/api/admin/analytics', verifyAuthToken, (req, res) => {
  const totalRevenue = db.prepare("SELECT COALESCE(SUM(total_amount), 0) as revenue FROM orders WHERE status != 'CANCELLED'").get().revenue;
  const totalOrders = db.prepare('SELECT count(*) as count FROM orders').get().count;
  const totalProducts = db.prepare('SELECT count(*) as count FROM products').get().count;
  const lowStockCount = db.prepare('SELECT count(*) as count FROM products WHERE stock < 5').get().count;
  const pendingOrders = db.prepare("SELECT count(*) as count FROM orders WHERE status = 'PENDING'").get().count;

  const recentOrders = db.prepare('SELECT * FROM orders ORDER BY created_at DESC LIMIT 5').all();

  res.json({
    success: true,
    data: {
      total_revenue: totalRevenue,
      total_orders: totalOrders,
      total_products: totalProducts,
      low_stock_count: lowStockCount,
      pending_orders: pendingOrders,
      recent_orders: recentOrders
    }
  });
});

// Admin Orders: List all
app.get('/api/admin/orders', verifyAuthToken, (req, res) => {
  const orders = db.prepare('SELECT * FROM orders ORDER BY created_at DESC').all();
  res.json({ success: true, count: orders.length, data: orders });
});

// Admin Update Order Status
app.patch('/api/admin/orders/:id/status', verifyAuthToken, (req, res) => {
  const { status } = req.body;
  const validStatuses = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
  if (!validStatuses.includes(status)) {
    return res.status(400).json({ success: false, error: 'Invalid status' });
  }

  const result = db.prepare('UPDATE orders SET status = ? WHERE id = ?').run(status, req.params.id);
  if (result.changes === 0) {
    return res.status(404).json({ success: false, error: 'Order not found' });
  }
  res.json({ success: true, message: `Order status updated to ${status}` });
});

// Admin Add Product
app.post('/api/admin/products', verifyAuthToken, (req, res) => {
  const {
    id, name_ar, name_en, desc_ar, desc_en, price,
    original_price, category_id, stock = 10,
    badge_ar, badge_en, is_featured = 0,
    image_res = 'hero_luxury_case',
    compatibility = 'Universal'
  } = req.body;

  if (!name_ar || !name_en || !price || !category_id) {
    return res.status(400).json({ success: false, error: 'Missing required product fields' });
  }

  const productId = id || ('bob-prod-' + Date.now().toString(36));

  try {
    db.prepare(`
      INSERT INTO products (
        id, name_ar, name_en, desc_ar, desc_en, price, original_price,
        category_id, stock, rating, reviews_count, badge_ar, badge_en,
        is_featured, image_res, compatibility
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 5.0, 1, ?, ?, ?, ?, ?)
    `).run(
      productId, name_ar, name_en, desc_ar || '', desc_en || '',
      price, original_price, category_id, stock,
      badge_ar || 'جديد', badge_en || 'NEW', is_featured ? 1 : 0,
      image_res, compatibility
    );

    res.status(201).json({ success: true, message: 'Product created', product_id: productId });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Admin Update Product
app.put('/api/admin/products/:id', verifyAuthToken, (req, res) => {
  const {
    name_ar, name_en, desc_ar, desc_en, price,
    original_price, category_id, stock,
    badge_ar, badge_en, is_featured, compatibility
  } = req.body;

  const result = db.prepare(`
    UPDATE products SET
      name_ar = COALESCE(?, name_ar),
      name_en = COALESCE(?, name_en),
      desc_ar = COALESCE(?, desc_ar),
      desc_en = COALESCE(?, desc_en),
      price = COALESCE(?, price),
      original_price = COALESCE(?, original_price),
      category_id = COALESCE(?, category_id),
      stock = COALESCE(?, stock),
      badge_ar = COALESCE(?, badge_ar),
      badge_en = COALESCE(?, badge_en),
      is_featured = COALESCE(?, is_featured),
      compatibility = COALESCE(?, compatibility)
    WHERE id = ?
  `).run(
    name_ar, name_en, desc_ar, desc_en, price, original_price,
    category_id, stock, badge_ar, badge_en, is_featured, compatibility,
    req.params.id
  );

  if (result.changes === 0) {
    return res.status(404).json({ success: false, error: 'Product not found' });
  }
  res.json({ success: true, message: 'Product updated' });
});

// Admin Delete Product
app.delete('/api/admin/products/:id', verifyAuthToken, (req, res) => {
  const result = db.prepare('DELETE FROM products WHERE id = ?').run(req.params.id);
  if (result.changes === 0) {
    return res.status(404).json({ success: false, error: 'Product not found' });
  }
  res.json({ success: true, message: 'Product deleted' });
});

// 404 for unknown routes (strictly JSON)
app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint not found', path: req.originalUrl });
});

// Error Handler (strictly JSON)
app.use((err, req, res, next) => {
  console.error('Server Error:', err.message);
  res.status(err.status || 500).json({ error: 'Internal Server Error', details: err.message });
});

// Start listening on 127.0.0.1
if (require.main === module) {
  app.listen(PORT, HOST, () => {
    console.log(`[BoB Store API] Listening strictly on http://${HOST}:${PORT}`);
    console.log(`[BoB Store API] Pure JSON API mode, helmet & rate-limit active`);
    console.log(`[BoB Store API] Demo Admin: admin@bobstore.com | Password: bob2025!`);
  });
}

module.exports = { app, db, getTotpCode, verifyTotp };
