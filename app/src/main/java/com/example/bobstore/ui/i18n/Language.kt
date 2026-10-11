package com.example.bobstore.ui.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val layoutDirection: LayoutDirection) {
    ARABIC("ar", "العربية", LayoutDirection.Rtl),
    ENGLISH("en", "English", LayoutDirection.Ltr)
}

enum class Currency(val code: String, val symbolAr: String, val symbolEn: String, val rateFromUsd: Double) {
    USD("USD", "$", "$", 1.0),
    LBP("LBP", "ل.ل", "LBP", 89500.0)
}

object Strings {
    // Top Bar & App Identity
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "متجر BoB" else "BoB Store"
    fun luxuryTagline(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إكسسوارات هواتف فاخرة أصلية" else "Luxury Authentic Mobile Accessories"

    // Auth Screen
    fun loginTab(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تسجيل الدخول" else "Sign In"
    fun registerTab(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إنشاء حساب جديد" else "Create Account"
    fun emailOrUsername(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "البريد الإلكتروني أو اسم المستخدم" else "Email or Username"
    fun password(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "كلمة المرور" else "Password"
    fun fullName(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الاسم الكامل" else "Full Name"
    fun phoneNumber(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "رقم الهاتف" else "Phone Number"
    fun loginBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "دخول" else "Login"
    fun registerBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إنشاء الحساب" else "Register"
    fun demoAdminLogin(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "دخول سريع كـ أدمن (تجربة)" else "Quick Admin Demo"
    fun demoCustomerLogin(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "دخول سريع كـ زبون (تجربة)" else "Quick Customer Demo"
    fun authErrorInvalid(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "بيانات الدخول غير صحيحة" else "Invalid credentials"
    fun authSuccess(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم تسجيل الدخول بنجاح" else "Logged in successfully"
    fun registerSuccess(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم إنشاء الحساب بنجاح! تم تسجيل دخولك" else "Account created! You are logged in"
    fun logout(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تسجيل الخروج" else "Log Out"

    // Navigation Tabs (Customer)
    fun tabDiscover(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المتجر" else "Store"
    fun tabWishlist(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المفضلة" else "Wishlist"
    fun tabOrders(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "طلباتي" else "My Orders"
    fun tabCart(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "السلة" else "Cart"
    fun tabProfile(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حسابي" else "Profile"

    // Admin Tabs
    fun tabAdminDashboard(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الرئيسية" else "Overview"
    fun tabAdminUsers(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الحسابات" else "Accounts"
    fun tabAdminOrders(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الطلبات" else "Orders"
    fun tabAdminCustomization(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تخصيص وألوان" else "Design"
    fun tabAdminProducts(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المنتجات" else "Products"

    // Search & Filter
    fun searchPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ابحث ذكياً عن أي إكسسوار أو جهاز..." else "Smart search for any gear..."
    fun aiSearchBadge(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "بحث بالذكاء الاصطناعي" else "AI Search"
    fun aiProcessingText(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري تحليل البحث بالذكاء الاصطناعي..." else "AI analyzing search query..."
    fun aiProcessingImage(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري فحص الصورة ومطابقة المنتجات..." else "AI Vision analyzing product image..."
    fun visualSearchTooltip(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "البحث بالصورة (AI Vision)" else "Visual Search (AI Vision)"
    fun visualSearchTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "البحث البصري بالذكاء الاصطناعي" else "AI Visual Search"
    fun visualSearchSubtitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التقط صورة لمنتج أو اختر من هاتفك، وسيتعرف الذكاء الاصطناعي عليه فوراً ويبحث عن المنتجات المطابقة في المتجر" else "Snap or pick a product photo to let Gemini AI identify it and find similar gear in BoB Store"
    fun takePhotoAction(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التقاط بالكاميرا" else "Take Photo"
    fun pickGalleryAction(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اختيار من الصور" else "Choose Photo"
    fun analyzingImage(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جاري فحص الصورة والتعرف على المنتج بالذكاء الاصطناعي..." else "Analyzing photo with Gemini AI..."
    fun productRecognized(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم التعرف على المنتج بنجاح! 🎯" else "Product Recognized! 🎯"
    fun similarStoreProducts(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المنتجات المشابهة والمطابقة في BoB Store" else "Matching Products in BoB Store"
    fun applySearchInStore(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "عرض النتائج في المتجر" else "Apply to Store Catalog"
    fun retakePhoto(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "صورة أخرى" else "Try Another Photo"
    fun aiQueryInsight(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تحليل الذكاء الاصطناعي لبحثك" else "AI Search Insights"
    fun allCategories(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الكل" else "All"
    fun featuredSection(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المجموعة الحصرية الفاخرة" else "Exclusive Collection"
    fun allProducts(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جميع الإكسسوارات" else "All Accessories"
    fun filterButton(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تصفية" else "Filter"
    fun filterSheetTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تصفية وترتيب المنتجات" else "Filter & Sort Products"
    fun sortByTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ترتيب حسب" else "Sort By"
    fun priceRangeTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "نطاق السعر" else "Price Range"
    fun brandsTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الماركة" else "Brand"
    fun allBrands(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جميع الماركات" else "All Brands"
    fun allPrices(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "جميع الأسعار" else "All Prices"
    fun clearAllFilters(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مسح الكل" else "Clear All"
    fun applyFilters(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تطبيق التصفية" else "Apply Filters"
    fun showResults(lang: AppLanguage, count: Int) = if (lang == AppLanguage.ARABIC) "عرض $count منتج" else "Show $count items"
    fun activeFilters(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الفلاتر المطبقة" else "Active Filters"

    // Product Details
    fun addToCart(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة للسلة" else "Add to Cart"
    fun buyNow(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "طلب فوري" else "Buy Now"
    fun inStock(lang: AppLanguage, count: Int) = if (lang == AppLanguage.ARABIC) "متوفر: $count قطع" else "In Stock: $count pcs"
    fun outOfStock(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "نفد من المخزون" else "Out of Stock"
    fun selectColor(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اختر اللون" else "Select Color"
    fun selectSize(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "اختر المقاس / موديل الجهاز" else "Select Device Model / Size"
    fun compatibility(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "التوافق" else "Compatibility"
    fun specifications(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المواصفات الفنية" else "Specifications"
    fun guarantee(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "ضمان أصلي لمدة سنتين + شحن فاخر آمن" else "2-Year Authentic Warranty + Armored Shipping"
    fun whatsappInquireProduct(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "استفسار عن هذا المنتج عبر واتساب" else "Ask about product via WhatsApp"
    fun whatsappStore(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تواصل مع المتجر عبر واتساب" else "Chat on WhatsApp"

    // Cart & Checkout
    fun cartTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلة التسوق" else "Shopping Cart"
    fun emptyCart(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "سلتك فارغة حالياً" else "Your cart is empty"
    fun emptyCartSubtitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "استكشف تشكيلتنا الفاخرة وأضف أفضل القطع لجهازك" else "Explore our catalog and upgrade your phone gear"
    fun subtotal(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المجموع الفرعي" else "Subtotal"
    fun shipping(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الشحن" else "Shipping"
    fun freeShipping(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "مجاني" else "FREE"
    fun vatTax(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الضريبة (15%)" else "VAT (15%)"
    fun total(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المجموع الإجمالي" else "Total"
    fun proceedToCheckout(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "متابعة إتمام الطلب" else "Proceed to Checkout"
    fun voucherCode(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "كود الخصم (جرب: BOB10)" else "Promo Code (Try: BOB10)"
    fun applyCoupon(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تطبيق" else "Apply"
    fun couponApplied(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم تطبيق خصم 10%!" else "10% Discount Applied!"

    // Checkout Dialog & Whish Money
    fun checkoutTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إتمام وتأكيد الطلب" else "Checkout & Confirm"
    fun city(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المدينة / المنطقة" else "City / Region"
    fun deliveryAddress(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "عنوان التوصيل بالتفصيل" else "Delivery Address"
    fun paymentMethod(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "طريقة الدفع" else "Payment Method"
    fun payWhishMoney(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "Whish Money (ويش موني)" else "Whish Money"
    fun whishMoneyNote(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الدفع السريع والمباشر عبر محفظة Whish Money إلى حساب المتجر" else "Fast direct payment via Whish Money wallet to BoB Store"
    fun payCard(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "بطاقة بنكية / فيزا" else "Credit Card"
    fun payCod(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "الدفع عند الاستلام (Cash)" else "Cash on Delivery"
    fun confirmOrderBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تأكيد الطلب وإرساله لواتساب" else "Confirm Order & Send to WhatsApp"
    fun orderSuccessTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم تأكيد طلبك بنجاح!" else "Order Confirmed Successfully!"
    fun trackingNumLabel(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "رقم التتبع الخاص بك:" else "Your Tracking Number:"
    fun sendOrderWhatsappBtn(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إرسال تفاصيل الطلب عبر واتساب 📲" else "Send Order to WhatsApp 📲"

    // Order Tracking
    fun trackTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "متابعة الطلبات والشحنات" else "Track Orders"
    fun searchTrackingPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "أدخل رقم الطلب أو التتبع..." else "Enter Order or Tracking #..."
    fun statusPending(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "قيد المراجعة" else "Pending"
    fun statusProcessing(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "قيد التجهيز" else "Processing"
    fun statusShipped(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم الشحن" else "Shipped"
    fun statusDelivered(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تم التسليم بنجاح" else "Delivered"

    // Admin Dashboard & Management
    fun adminPortalTitle(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "لوحة تحكم إدارة BoB Store" else "BoB Store Admin Dashboard"
    fun statsRevenue(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إجمالي الإيرادات" else "Total Revenue"
    fun statsOrders(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إجمالي الطلبات" else "Total Orders"
    fun statsProducts(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "المنتجات النشطة" else "Active Products"
    fun statsPending(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "طلبات جديدة معلقة" else "Pending Orders"
    fun manageProducts(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إدارة المنتجات" else "Products"
    fun manageOrders(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إدارة الطلبات" else "Orders"
    fun manageAdmins(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حسابات الأدمن" else "Admin Accounts"
    fun addNewProduct(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة منتج جديد" else "Add Product"
    fun editProduct(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تعديل المنتج" else "Edit Product"
    fun deleteProduct(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حذف" else "Delete"
    fun addNewAdmin(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إضافة أدمن جديد" else "Add New Admin"
    fun saveChanges(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "حفظ التغييرات" else "Save Changes"
    fun cancel(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "إلغاء" else "Cancel"
    fun orderNotifications(lang: AppLanguage) = if (lang == AppLanguage.ARABIC) "تنبيهات وإشعارات الطلبات" else "Order Notifications"
}
