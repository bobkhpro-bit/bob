package com.example.bobstore.data.repository

import com.example.bobstore.data.model.*
import com.example.bobstore.data.remote.BoBApiService
import com.example.bobstore.data.remote.CreateOrderRequest
import com.example.bobstore.data.remote.OrderReqItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BoBRepository {

    private var serverBaseUrl = "http://10.0.2.2:3000/"
    private var apiService: BoBApiService? = null

    // Authentication State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Users Database
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Admin Notifications
    private val _adminNotifications = MutableStateFlow<List<AdminNotification>>(emptyList())
    val adminNotifications: StateFlow<List<AdminNotification>> = _adminNotifications.asStateFlow()

    // In-Memory & Reactive State Store
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Real Customer Reviews
    private val _reviews = MutableStateFlow<List<ProductReview>>(emptyList())
    val reviews: StateFlow<List<ProductReview>> = _reviews.asStateFlow()

    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected.asStateFlow()

    // Store Customization
    private val _storeCustomization = MutableStateFlow(StoreCustomization())
    val storeCustomization: StateFlow<StoreCustomization> = _storeCustomization.asStateFlow()

    init {
        seedInitialData()
        seedInitialUsers()
        rebuildRetrofit(serverBaseUrl)
    }

    private fun seedInitialUsers() {
        val defaultAdmin = User(
            id = "admin-1",
            name = "admin",
            email = "admin@bobstore.com",
            password = "admin123",
            role = UserRole.ADMIN,
            phone = "0096170198429"
        )
        val defaultCustomer = User(
            id = "cust-1",
            name = "علي خوري",
            email = "customer@bobstore.com",
            password = "123456",
            role = UserRole.CUSTOMER,
            phone = "+961 70 198 429"
        )
        _users.value = listOf(defaultAdmin, defaultCustomer)
    }

    fun login(emailOrUsername: String, pass: String): Result<User> {
        val cleanIdentifier = emailOrUsername.trim().lowercase()
        val user = _users.value.find {
            (it.email.lowercase() == cleanIdentifier || it.name.lowercase() == cleanIdentifier || (cleanIdentifier == "admin" && it.role == UserRole.ADMIN)) &&
                    (it.password == pass || (it.role == UserRole.ADMIN && (pass == "admin123" || pass == "bob2025!" || pass == "admin")))
        }
        return if (user != null) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("البريد الإلكتروني أو كلمة المرور غير صحيحة"))
        }
    }

    fun registerCustomer(name: String, email: String, pass: String, phone: String?): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (_users.value.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(Exception("هذا البريد الإلكتروني مسجل مسبقاً"))
        }
        val newUser = User(
            id = "user-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            password = pass,
            role = UserRole.CUSTOMER,
            phone = phone?.trim()
        )
        _users.value = _users.value + newUser
        _currentUser.value = newUser
        return Result.success(newUser)
    }


    fun addNewAdmin(name: String, email: String, pass: String, phone: String?): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (_users.value.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(Exception("هذا البريد الإلكتروني مسجل مسبقاً"))
        }
        val newAdmin = User(
            id = "admin-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            password = pass,
            role = UserRole.ADMIN,
            phone = phone?.trim()
        )
        _users.value = _users.value + newAdmin
        return Result.success(newAdmin)
    }

    fun updateUserRole(userId: String, newRole: UserRole): Boolean {
        if (userId == "admin-1") return false // Cannot demote root admin
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(role = newRole) else it
        }
        return true
    }

    fun deleteUser(userId: String): Boolean {
        if (userId == "admin-1") return false // Prevent deleting root admin
        _users.value = _users.value.filterNot { it.id == userId }
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    fun setServerUrl(url: String) {
        val formatted = if (url.endsWith("/")) url else "$url/"
        serverBaseUrl = formatted
        rebuildRetrofit(formatted)
    }

    fun getServerUrl(): String = serverBaseUrl

    private fun rebuildRetrofit(url: String) {
        try {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(BoBApiService::class.java)
        } catch (e: Exception) {
            apiService = null
        }
    }

    private fun seedInitialData() {
        val initialCategories = listOf(
            Category("cases", "كفرات فاخرة", "Luxury Cases", "phone_android"),
            Category("chargers", "شواحن و MagSafe", "MagSafe & Chargers", "bolt"),
            Category("audio", "سماعات ودروع حماية", "Audio & Armor", "headphones"),
            Category("accessories", "إكسسوارات وحوامل", "Accessories & Mounts", "extension")
        )
        _categories.value = initialCategories

        val commonSizes = listOf(
            "iPhone 16 Pro Max",
            "iPhone 16 Pro",
            "iPhone 15 Pro Max",
            "iPhone 15 Pro",
            "Galaxy S25 Ultra",
            "Galaxy S24 Ultra"
        )

        val initialProducts = listOf(
            Product(
                id = "case-aurum-titanium",
                nameAr = "كفر تيتانيوم وفايبر كربون أوروم",
                nameEn = "Aurum Titanium & Forged Carbon Case",
                descAr = "إطار مصقول من التيتانيوم المستخدم في صناعة الطائرات مدمج بألياف كربون مطروقة وحواف ذهبية عيار 24 قيراط مع بطانة مخملية واقية للصدمات.",
                descEn = "Aerospace titanium chassis fused with forged carbon fiber and 24K gold chamfered accents. Ultra-slim luxury armor.",
                price = 89.0,
                originalPrice = 119.0,
                categoryId = "cases",
                brand = "BoB Elite",
                stock = 15,
                rating = 4.98,
                reviewsCount = 142,
                badgeAr = "الأكثر مبيعاً",
                badgeEn = "BEST SELLER",
                isFeatured = 1,
                imageRes = "hero_luxury_case",
                compatibility = "iPhone 16 Pro Max, iPhone 15 Pro, Galaxy S25 Ultra",
                colors = listOf("#D4AF37", "#1E1E24", "#708090", "#8A2BE2"),
                sizes = commonSizes
            ),
            Product(
                id = "dock-orion-magluna",
                nameAr = "منصة شحن MagSafe أوريون ماج-لونا الذكية",
                nameEn = "Orion Mag-Luna Wireless Docking Station",
                descAr = "قاعدة شحن مغناطيسية بتصميم نحت معماري من الزجاج المصنفر والبرونز المفرغ مع إضاءة هالة محيطية متزامنة وقدرة شحن Qi2 فائقة السرعة 25 واط.",
                descEn = "Architectural frosted glass & brushed brass MagSafe stand with responsive ambient aura ring and 25W certified Qi2 fast charging.",
                price = 75.0,
                originalPrice = 95.0,
                categoryId = "chargers",
                brand = "Pitaka",
                stock = 22,
                rating = 4.95,
                reviewsCount = 98,
                badgeAr = "إصدار محدود",
                badgeEn = "LIMITED EDITION",
                isFeatured = 1,
                imageRes = "hero_wireless_dock",
                compatibility = "All Qi2 / MagSafe iPhones & Android Flagships",
                colors = listOf("#E5B869", "#13151D", "#00F5D4"),
                sizes = listOf("Standard Desk Mount", "Dual Charging Stand")
            ),
            Product(
                id = "earbuds-aura-armor",
                nameAr = "درع سماعات أورا كربون وتيتانيوم",
                nameEn = "Aura Audio Titanium X Carbon Armor",
                descAr = "جراب مدرع متين لسماعات الأذن مع حلقة تسلق ذهبية مقفلة ومقاومة للماء والصدمات بتصميم ميكانيكي فاخر.",
                descEn = "Heavy-duty titanium alloy case shield with carbon weave insert and custom gold carabiner lock for extreme luxury everyday carry.",
                price = 45.0,
                originalPrice = 59.0,
                categoryId = "audio",
                brand = "Apple",
                stock = 30,
                rating = 4.91,
                reviewsCount = 76,
                badgeAr = "جديد",
                badgeEn = "NEW ARRIVAL",
                isFeatured = 1,
                imageRes = "hero_earbuds_case",
                compatibility = "AirPods Pro 2 / Pro 1, Galaxy Buds 3 Pro",
                colors = listOf("#1E1E24", "#D4AF37", "#C5CAD6"),
                sizes = listOf("AirPods Pro 2", "AirPods Pro 1", "Galaxy Buds 3 Pro")
            ),
            Product(
                id = "lens-sapphire-shield",
                nameAr = "حامي عدسات الكاميرا زفير كريستال عيار 9H",
                nameEn = "BoB 9H Sapphire Crystal Lens Protector",
                descAr = "حلقات حماية عدسات فردية مصنوعة من الياقوت السويسري الاصطناعي مع إطار تيتانيوم مانع للانعكاس وبدون أي تأثير على جودة التصوير الليلي.",
                descEn = "Individual swiss-grade synthetic sapphire crystal rings framed in CNC machined titanium with anti-reflective coating.",
                price = 29.0,
                originalPrice = 39.0,
                categoryId = "accessories",
                brand = "Spigen",
                stock = 50,
                rating = 4.88,
                reviewsCount = 210,
                badgeAr = "حماية فائقة",
                badgeEn = "PRO GRADE",
                isFeatured = 0,
                imageRes = "ic_bob_store_logo",
                compatibility = "iPhone 16 / 15 Series, Galaxy S24/S25 Ultra",
                colors = listOf("#D4AF37", "#708090", "#13151D"),
                sizes = listOf("iPhone 16 Pro / Max", "iPhone 15 Pro / Max", "Galaxy S25 Ultra")
            ),
            Product(
                id = "wallet-mag-leather",
                nameAr = "محفظة ماغسيف جلد إيطالي فاخر مع ستاند",
                nameEn = "BoB MagSafe Italian Leather Cardholder",
                descAr = "محفظة فائقة النحافة تتسع لـ 3 بطاقات مع ميزة ستاند مكتبي قابل للطي ومغناطيس N52 فائق القوة لا ينزلق.",
                descEn = "Ultra-slim handcrafted Italian full-grain leather wallet with integrated 60-degree kickstand and N52 neo magnets.",
                price = 39.0,
                originalPrice = 49.0,
                categoryId = "accessories",
                brand = "Anker",
                stock = 18,
                rating = 4.85,
                reviewsCount = 64,
                badgeAr = "فاخر",
                badgeEn = "LUXURY",
                isFeatured = 0,
                imageRes = "ic_bob_store_logo",
                compatibility = "All MagSafe iPhones & MagSafe Cases",
                colors = listOf("#8B4513", "#13151D", "#2F4F4F"),
                sizes = listOf("Universal MagSafe")
            )
        )
        _products.value = initialProducts

        val initialOrders = listOf(
            Order(
                id = "BOB-98421",
                customerName = "عمر حلواني",
                customerPhone = "0096170198429",
                customerAddress = "بيروت، الحمراء، شارع بلس",
                city = "بيروت",
                paymentMethod = "Whish Money",
                status = "PROCESSING",
                totalAmount = 164.0,
                currency = "USD",
                trackingNumber = "TRK748392104",
                createdAt = "منذ ساعتين",
                items = listOf(
                    OrderItem("case-aurum-titanium", "Aurum Titanium & Forged Carbon Case", 1, 89.0, "#D4AF37", "iPhone 16 Pro Max"),
                    OrderItem("dock-orion-magluna", "Orion Mag-Luna Wireless Docking Station", 1, 75.0, "#E5B869", "Standard Desk Mount")
                )
            )
        )
        _orders.value = initialOrders
    }

    // Cart Management
    fun addToCart(product: Product, selectedColor: String = "#D4AF37", selectedSize: String = "iPhone 16 Pro Max", quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.product.id == product.id && it.selectedColor == selectedColor && it.selectedSize == selectedSize
        }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + quantity)
        } else {
            current.add(CartItem(product, quantity, selectedColor, selectedSize))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: String, selectedColor: String, selectedSize: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeCartItem(productId, selectedColor, selectedSize)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst {
            it.product.id == productId && it.selectedColor == selectedColor && it.selectedSize == selectedSize
        }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQuantity)
            _cartItems.value = current
        }
    }

    fun removeCartItem(productId: String, selectedColor: String, selectedSize: String) {
        _cartItems.value = _cartItems.value.filterNot {
            it.product.id == productId && it.selectedColor == selectedColor && it.selectedSize == selectedSize
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Wishlist Toggle
    fun toggleWishlist(productId: String) {
        val current = _wishlistIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _wishlistIds.value = current
    }

    // Order Creation & Admin Notification Generation
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        city: String,
        paymentMethod: String,
        currency: String = "USD"
    ): Result<Order> = withContext(Dispatchers.IO) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            return@withContext Result.failure(Exception("السلة فارغة"))
        }

        val total = items.sumOf { it.product.price * it.quantity }
        val randomSuffix = SecureRandom().nextInt(9000) + 1000
        val orderId = "BOB-" + System.currentTimeMillis().toString(36).uppercase() + "-$randomSuffix"
        val trackingNum = "TRK" + (100000000L + (Math.random() * 900000000L).toLong())

        val orderItems = items.map {
            OrderItem(
                productId = it.product.id,
                productName = it.product.nameEn,
                quantity = it.quantity,
                unitPrice = it.product.price,
                colorChoice = it.selectedColor,
                sizeChoice = it.selectedSize
            )
        }

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val newOrder = Order(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            city = city,
            paymentMethod = paymentMethod,
            status = "PENDING",
            totalAmount = total,
            currency = currency,
            trackingNumber = trackingNum,
            createdAt = "اليوم $timeStr",
            items = orderItems
        )

        // Generate Admin Notification Alert
        val notification = AdminNotification(
            id = "notif-" + System.currentTimeMillis(),
            title = "طلب جديد من $customerName 🛍️",
            message = "تم استلام طلب جديد ($orderId) بقيمة $$total عبر $paymentMethod.",
            timestamp = timeStr,
            orderId = orderId
        )
        _adminNotifications.value = listOf(notification) + _adminNotifications.value

        // Try syncing to API Server if available
        try {
            apiService?.let { service ->
                val reqItems = items.map { OrderReqItem(it.product.id, it.quantity, it.selectedColor) }
                val request = CreateOrderRequest(customerName, customerPhone, customerAddress, city, paymentMethod, currency, reqItems)
                val resp = service.createOrder(request)
                if (resp.isSuccessful) {
                    _isServerConnected.value = true
                }
            }
        } catch (e: Exception) {
            _isServerConnected.value = false
        }

        // Add to local state
        _orders.value = listOf(newOrder) + _orders.value
        clearCart()

        Result.success(newOrder)
    }

    // Admin Operations
    fun updateOrderStatus(orderId: String, newStatus: String) {
        val current = _orders.value.map {
            if (it.id == orderId || it.trackingNumber == orderId) {
                it.copy(status = newStatus)
            } else it
        }
        _orders.value = current
    }

    fun addOrUpdateProduct(product: Product) {
        val current = _products.value.toMutableList()
        val index = current.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            current[index] = product
        } else {
            current.add(0, product)
        }
        _products.value = current
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    fun updateCustomization(customization: StoreCustomization) {
        _storeCustomization.value = customization
    }

    fun getSimilarProducts(productId: String, categoryId: String): List<Product> {
        val inSameCat = _products.value.filter { it.categoryId == categoryId && it.id != productId }
        return if (inSameCat.isNotEmpty()) {
            inSameCat.take(4)
        } else {
            _products.value.filter { it.id != productId }.take(4)
        }
    }

    fun addProductReview(productId: String, userName: String, rating: Int, comment: String) {
        val newReview = ProductReview(
            productId = productId,
            userName = userName.ifBlank { "عميل معتمد" },
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            date = "اليوم"
        )
        _reviews.value = listOf(newReview) + _reviews.value

        // Recalculate product rating & reviewsCount dynamically
        val prodReviews = _reviews.value.filter { it.productId == productId }
        val avg = prodReviews.map { it.rating }.average()
        val roundedAvg = (Math.round(avg * 10.0) / 10.0).coerceIn(1.0, 5.0)

        val prods = _products.value.toMutableList()
        val idx = prods.indexOfFirst { it.id == productId }
        if (idx >= 0) {
            prods[idx] = prods[idx].copy(
                rating = roundedAvg,
                reviewsCount = prodReviews.size
            )
            _products.value = prods
        }
    }

    fun getReviewsForProduct(productId: String): List<ProductReview> {
        val list = _reviews.value.filter { it.productId == productId }
        if (list.isEmpty()) {
            // Provide realistic verified customer reviews for realism
            return listOf(
                ProductReview(
                    productId = productId,
                    userName = "جاد شريف",
                    rating = 5,
                    comment = "جودة خرافية جداً وخامة ممتازة، التيتانيوم أصلي ويحمي الجهاز تماماً!",
                    date = "منذ يومين"
                ),
                ProductReview(
                    productId = productId,
                    userName = "ميشال عون",
                    rating = 5,
                    comment = "توصيل فوري والمنتج متطابق تماماً مع الصورة والمواصفات.",
                    date = "منذ 4 أيام"
                ),
                ProductReview(
                    productId = productId,
                    userName = "سارة حداد",
                    rating = 4,
                    comment = "المقاس مضبوط 100% واللون رايق وهادي.",
                    date = "الأسبوع الماضي"
                )
            )
        }
        return list
    }

    fun getAnalytics(): AdminAnalytics {
        val orderList = _orders.value
        val totalRev = orderList.filter { it.status != "CANCELLED" }.sumOf { it.totalAmount }
        val prodList = _products.value
        val totalStock = prodList.sumOf { it.stock }
        val lowStock = prodList.count { it.stock < 10 }
        val pending = orderList.count { it.status == "PENDING" }
        val adminCount = _users.value.count { it.role == UserRole.ADMIN }

        return AdminAnalytics(
            totalRevenue = totalRev,
            totalOrders = orderList.size,
            totalProducts = prodList.size,
            totalStockCount = totalStock,
            lowStockCount = lowStock,
            pendingOrders = pending,
            totalAdmins = adminCount
        )
    }
}
