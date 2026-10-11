package com.example.bobstore.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bobstore.data.model.*
import com.example.bobstore.data.remote.gemini.GeminiSearchService
import com.example.bobstore.data.remote.gemini.VisualAnalysisResult
import com.example.bobstore.data.repository.BoBRepository
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab {
    DISCOVER,
    WISHLIST,
    ORDERS,
    CART,
    PROFILE,
    ADMIN_DASHBOARD,
    ADMIN_USERS,
    ADMIN_ORDERS,
    ADMIN_CUSTOMIZATION
}

sealed interface VisualSearchUiState {
    object Idle : VisualSearchUiState
    data class Analyzing(val previewBitmap: Bitmap?) : VisualSearchUiState
    data class Success(val result: VisualAnalysisResult, val image: Bitmap) : VisualSearchUiState
    data class Error(val message: String) : VisualSearchUiState
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false
)

private data class FilterBaseBundle(
    val products: List<Product>,
    val cat: String,
    val query: String,
    val aiIds: List<String>?
)

private data class FilterOptionBundle(
    val brand: String?,
    val price: PriceRangeFilter?,
    val sort: ProductSortOption
)

class BoBViewModel(
    val repository: BoBRepository = BoBRepository()
) : ViewModel() {

    private val geminiService = GeminiSearchService()

    // Current User Session
    val currentUser: StateFlow<User?> = repository.currentUser
    val users: StateFlow<List<User>> = repository.users
    val adminNotifications: StateFlow<List<AdminNotification>> = repository.adminNotifications

    // Language & Currency
    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentCurrency = MutableStateFlow(Currency.USD)
    val currentCurrency: StateFlow<Currency> = _currentCurrency.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppNavTab.DISCOVER)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Filters
    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Search & Visual Search States
    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    private val _aiSearchInsight = MutableStateFlow<String?>(null)
    val aiSearchInsight: StateFlow<String?> = _aiSearchInsight.asStateFlow()

    private val _aiMatchedProductIds = MutableStateFlow<List<String>?>(null)
    val aiMatchedProductIds: StateFlow<List<String>?> = _aiMatchedProductIds.asStateFlow()

    private val _isVisualSearchModalOpen = MutableStateFlow(false)
    val isVisualSearchModalOpen: StateFlow<Boolean> = _isVisualSearchModalOpen.asStateFlow()

    private val _visualSearchState = MutableStateFlow<VisualSearchUiState>(VisualSearchUiState.Idle)
    val visualSearchState: StateFlow<VisualSearchUiState> = _visualSearchState.asStateFlow()

    // Modal / Dialog States
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _showCheckoutDialog = MutableStateFlow(false)
    val showCheckoutDialog: StateFlow<Boolean> = _showCheckoutDialog.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder: StateFlow<Order?> = _lastPlacedOrder.asStateFlow()

    // Promo Code
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    // Notifications
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Data from Repository
    val categories: StateFlow<List<Category>> = repository.categories
    val products: StateFlow<List<Product>> = repository.products
    val orders: StateFlow<List<Order>> = repository.orders
    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems

    // Advanced Filtering and Sorting States
    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()

    private val _selectedPriceRange = MutableStateFlow<PriceRangeFilter?>(null)
    val selectedPriceRange: StateFlow<PriceRangeFilter?> = _selectedPriceRange.asStateFlow()

    private val _sortOption = MutableStateFlow(ProductSortOption.DEFAULT)
    val sortOption: StateFlow<ProductSortOption> = _sortOption.asStateFlow()

    val availableBrands: StateFlow<List<String>> = products.map { prods ->
        prods.map { it.brand }.distinct().filter { it.isNotBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFilterCount: StateFlow<Int> = combine(
        selectedCategoryId,
        selectedBrand,
        selectedPriceRange,
        sortOption
    ) { cat, brand, price, sort ->
        var count = 0
        if (cat != "all") count++
        if (brand != null && brand != "all") count++
        if (price != null) count++
        if (sort != ProductSortOption.DEFAULT) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered & Sorted Products with AI Intelligence
    val filteredProducts: StateFlow<List<Product>> = combine(
        combine(products, selectedCategoryId, searchQuery, aiMatchedProductIds) { prods, cat, query, aiIds ->
            FilterBaseBundle(prods, cat, query, aiIds)
        },
        combine(selectedBrand, selectedPriceRange, sortOption) { brand, price, sort ->
            FilterOptionBundle(brand, price, sort)
        }
    ) { base, options ->
        val filtered = base.products.filter { p ->
            val matchCat = (base.cat == "all" || p.categoryId == base.cat)
            val matchQuery = base.query.isBlank() ||
                    p.nameAr.contains(base.query, ignoreCase = true) ||
                    p.nameEn.contains(base.query, ignoreCase = true) ||
                    p.descAr.contains(base.query, ignoreCase = true) ||
                    p.descEn.contains(base.query, ignoreCase = true) ||
                    p.brand.contains(base.query, ignoreCase = true)
            val matchBrand = (options.brand == null || options.brand == "all" || p.brand.equals(options.brand, ignoreCase = true))
            val matchPrice = if (options.price == null) true else {
                val minOk = options.price.minPrice == null || p.price >= options.price.minPrice
                val maxOk = options.price.maxPrice == null || p.price <= options.price.maxPrice
                minOk && maxOk
            }

            matchCat && matchQuery && matchBrand && matchPrice
        }

        val aiPrioritized = if (!base.aiIds.isNullOrEmpty()) {
            val matchedSet = base.aiIds.toSet()
            val matchedProds = filtered.filter { it.id in matchedSet }.sortedBy { base.aiIds.indexOf(it.id) }
            val remainingProds = filtered.filter { it.id !in matchedSet }
            matchedProds + remainingProds
        } else {
            filtered
        }

        when (options.sort) {
            ProductSortOption.DEFAULT -> aiPrioritized
            ProductSortOption.PRICE_LOW_HIGH -> aiPrioritized.sortedBy { it.price }
            ProductSortOption.PRICE_HIGH_LOW -> aiPrioritized.sortedByDescending { it.price }
            ProductSortOption.RATING_HIGH -> aiPrioritized.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBrand(brand: String?) {
        _selectedBrand.value = if (brand == "all") null else brand
    }

    fun setPriceRange(range: PriceRangeFilter?) {
        _selectedPriceRange.value = range
    }

    fun setSortOption(option: ProductSortOption) {
        _sortOption.value = option
    }

    fun resetAllFilters() {
        _selectedCategoryId.value = "all"
        _selectedBrand.value = null
        _selectedPriceRange.value = null
        _sortOption.value = ProductSortOption.DEFAULT
        _searchQuery.value = ""
        _aiMatchedProductIds.value = null
    }

    fun login(emailOrUser: String, pass: String): Boolean {
        val result = repository.login(emailOrUser, pass)
        return result.isSuccess.also { success ->
            if (success) {
                val user = result.getOrNull()!!
                if (user.role == UserRole.ADMIN) {
                    _currentTab.value = AppNavTab.ADMIN_DASHBOARD
                    showNotification("مرحباً بك يا مدير المتجر (${user.name})")
                } else {
                    _currentTab.value = AppNavTab.DISCOVER
                    showNotification("مرحباً بك في متجر BoB Store (${user.name})")
                }
            } else {
                showNotification(result.exceptionOrNull()?.message ?: "خطأ في تسجيل الدخول", isError = true)
            }
        }
    }

    fun registerCustomer(name: String, email: String, pass: String, phone: String?): Boolean {
        val result = repository.registerCustomer(name, email, pass, phone)
        return result.isSuccess.also { success ->
            if (success) {
                _currentTab.value = AppNavTab.DISCOVER
                showNotification("تم إنشاء حسابك بنجاح! مرحباً بك")
            } else {
                showNotification(result.exceptionOrNull()?.message ?: "خطأ في التسجيل", isError = true)
            }
        }
    }

    fun addNewAdmin(name: String, email: String, pass: String, phone: String?): Boolean {
        val result = repository.addNewAdmin(name, email, pass, phone)
        return result.isSuccess.also { success ->
            if (success) {
                showNotification("تمت إضافة حساب الأدمن بنجاح: $name")
            } else {
                showNotification(result.exceptionOrNull()?.message ?: "تعذر إضافة الأدمن", isError = true)
            }
        }
    }

    fun deleteUser(userId: String) {
        if (repository.deleteUser(userId)) {
            showNotification("تم حذف الحساب بنجاح")
        } else {
            showNotification("لا يمكن حذف الأدمن الرئيسي", isError = true)
        }
    }

    fun toggleUserAdminRole(user: User) {
        val newRole = if (user.role == UserRole.ADMIN) UserRole.CUSTOMER else UserRole.ADMIN
        if (repository.updateUserRole(user.id, newRole)) {
            val roleName = if (newRole == UserRole.ADMIN) "أدمن" else "زبون"
            showNotification("تم تعديل رتبة ${user.name} إلى $roleName")
        } else {
            showNotification("لا يمكن تعديل رتبة الأدمن الرئيسي", isError = true)
        }
    }

    // App & Store Customization
    val storeCustomization: StateFlow<StoreCustomization> = repository.storeCustomization

    fun updateCustomization(customization: StoreCustomization) {
        repository.updateCustomization(customization)
        showNotification("تم حفظ وتطبيق تخصيص الألوان والصور بنجاح!")
    }

    fun getSimilarProducts(productId: String, categoryId: String): List<Product> {
        return repository.getSimilarProducts(productId, categoryId)
    }

    fun logout() {
        repository.logout()
        _currentTab.value = AppNavTab.DISCOVER
        _selectedProduct.value = null
        _showCheckoutDialog.value = false
        _lastPlacedOrder.value = null
        showNotification("تم تسجيل الخروج")
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.ARABIC) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.ARABIC
        }
    }

    fun setCurrency(currency: Currency) {
        _currentCurrency.value = currency
    }

    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _aiSearchInsight.value = null
            _aiMatchedProductIds.value = null
        }
    }

    fun triggerAiSearch(query: String = _searchQuery.value) {
        val q = query.trim()
        if (q.isBlank()) return
        _searchQuery.value = q
        viewModelScope.launch {
            _isAiSearching.value = true
            try {
                val result = geminiService.analyzeTextQuery(q, products.value)
                _aiSearchInsight.value = result.insightAr
                _aiMatchedProductIds.value = result.matchedProductIds
                if (result.suggestedCategory != "all" && _selectedCategoryId.value == "all") {
                    _selectedCategoryId.value = result.suggestedCategory
                }
            } catch (e: Exception) {
                _aiSearchInsight.value = "تمت تصفية المنتجات المطابقة لبحثك"
            } finally {
                _isAiSearching.value = false
            }
        }
    }

    fun clearAiSearch() {
        _aiSearchInsight.value = null
        _aiMatchedProductIds.value = null
        _searchQuery.value = ""
    }

    fun openVisualSearchModal() {
        _isVisualSearchModalOpen.value = true
        _visualSearchState.value = VisualSearchUiState.Idle
    }

    fun closeVisualSearchModal() {
        _isVisualSearchModalOpen.value = false
        _visualSearchState.value = VisualSearchUiState.Idle
    }

    fun performVisualSearch(bitmap: Bitmap) {
        _visualSearchState.value = VisualSearchUiState.Analyzing(bitmap)
        viewModelScope.launch {
            try {
                val result = geminiService.analyzeProductImage(bitmap, products.value)
                _visualSearchState.value = VisualSearchUiState.Success(result, bitmap)
                if (!_isVisualSearchModalOpen.value) {
                    applyVisualSearchResultToStore(result)
                }
            } catch (e: Exception) {
                _visualSearchState.value = VisualSearchUiState.Error(e.message ?: "حدث خطأ أثناء تحليل الصورة")
                if (!_isVisualSearchModalOpen.value) {
                    _aiSearchInsight.value = "تعذر التعرف على المنتج في الصورة، يرجى تجربة صورة أوضح"
                }
            }
        }
    }

    fun applyVisualSearchResultToStore(result: VisualAnalysisResult) {
        _selectedCategoryId.value = result.category
        _searchQuery.value = result.identifiedItemAr
        _aiSearchInsight.value = "🎯 نتائج البحث البصري لـ: ${result.identifiedItemAr}"
        _aiMatchedProductIds.value = result.matchedProducts.map { it.id }
        _isVisualSearchModalOpen.value = false
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun setShowCheckout(show: Boolean) {
        _showCheckoutDialog.value = show
    }

    fun setLastPlacedOrder(order: Order?) {
        _lastPlacedOrder.value = order
    }

    // Cart Actions
    fun addToCart(
        product: Product,
        selectedColor: String = "#D4AF37",
        selectedSize: String = "iPhone 16 Pro Max",
        quantity: Int = 1
    ) {
        repository.addToCart(product, selectedColor, selectedSize, quantity)
        val msg = if (_currentLanguage.value == AppLanguage.ARABIC) {
            "تمت إضافة ${product.nameAr} إلى السلة"
        } else {
            "Added ${product.nameEn} to Cart"
        }
        showNotification(msg)
    }

    fun updateCartQuantity(productId: String, color: String, size: String, qty: Int) {
        repository.updateCartItemQuantity(productId, color, size, qty)
    }

    fun removeFromCart(productId: String, color: String, size: String) {
        repository.removeCartItem(productId, color, size)
    }

    fun applyCoupon(code: String): Boolean {
        if (code.trim().uppercase() in listOf("BOB10", "VIP2025", "GOLD")) {
            _appliedCoupon.value = code.trim().uppercase()
            val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "تم تطبيق خصم 10% بنجاح!" else "10% VIP Discount Applied!"
            showNotification(msg)
            return true
        } else {
            val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "كود الخصم غير صالح" else "Invalid Promo Code"
            showNotification(msg, isError = true)
            return false
        }
    }

    fun toggleWishlist(productId: String) {
        repository.toggleWishlist(productId)
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        city: String,
        paymentMethod: String,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                city = city,
                paymentMethod = paymentMethod,
                currency = _currentCurrency.value.code
            )
            result.onSuccess { order ->
                _lastPlacedOrder.value = order
                _showCheckoutDialog.value = false
                _appliedCoupon.value = null
                onSuccess(order)
            }.onFailure { err ->
                showNotification(err.message ?: "Failed to place order", isError = true)
            }
        }
    }

    // Admin Operations
    fun updateOrderStatus(orderId: String, status: String) {
        repository.updateOrderStatus(orderId, status)
        val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "تم تحديث حالة الطلب إلى $status" else "Order status updated to $status"
        showNotification(msg)
    }

    fun addOrUpdateProduct(product: Product) {
        repository.addOrUpdateProduct(product)
        val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "تم حفظ المنتج بنجاح" else "Product saved successfully"
        showNotification(msg)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
        val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "تم حذف المنتج" else "Product deleted"
        showNotification(msg)
    }

    // Customer Real Reviews
    fun addProductReview(productId: String, rating: Int, comment: String) {
        val userName = currentUser.value?.name ?: "عميل BoB"
        repository.addProductReview(productId, userName, rating, comment)
        val msg = if (_currentLanguage.value == AppLanguage.ARABIC) "تمت إضافة تقييمك الحقيقي بنجاح! شكراً لك" else "Review submitted successfully! Thank you"
        showNotification(msg)
    }

    fun getProductReviews(productId: String): List<ProductReview> {
        return repository.getReviewsForProduct(productId)
    }

    fun formatPrice(amountUsd: Double): String {
        val curr = _currentCurrency.value
        val rate = if (curr == Currency.LBP) repository.storeCustomization.value.lbpExchangeRate else 1.0
        val converted = amountUsd * rate
        val symbol = if (_currentLanguage.value == AppLanguage.ARABIC) curr.symbolAr else curr.symbolEn
        return if (curr == Currency.LBP) {
            val formatted = java.text.NumberFormat.getIntegerInstance().format(converted.toLong())
            if (_currentLanguage.value == AppLanguage.ARABIC) "$formatted $symbol" else "$symbol $formatted"
        } else {
            if (_currentLanguage.value == AppLanguage.ARABIC) {
                "%.2f %s".format(converted, symbol)
            } else {
                "%s%.2f".format(symbol, converted)
            }
        }
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _notification.value = UiNotification(message, isError)
        viewModelScope.launch {
            delay(3500)
            if (_notification.value?.message == message) {
                _notification.value = null
            }
        }
    }
}
