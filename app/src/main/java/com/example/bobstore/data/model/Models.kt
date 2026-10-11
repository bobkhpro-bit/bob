package com.example.bobstore.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

enum class UserRole {
    CUSTOMER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val phone: String? = null,
    val createdAt: String = "2026-10-09"
)

@JsonClass(generateAdapter = true)
data class Category(
    @Json(name = "id") val id: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "icon") val iconName: String
)

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "id") val id: String,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "desc_ar") val descAr: String,
    @Json(name = "desc_en") val descEn: String,
    @Json(name = "price") val price: Double,
    @Json(name = "original_price") val originalPrice: Double? = null,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "stock") val stock: Int = 10,
    @Json(name = "rating") val rating: Double = 4.9,
    @Json(name = "reviews_count") val reviewsCount: Int = 15,
    @Json(name = "badge_ar") val badgeAr: String? = null,
    @Json(name = "badge_en") val badgeEn: String? = null,
    @Json(name = "is_featured") val isFeatured: Int = 0,
    @Json(name = "image_res") val imageRes: String = "hero_luxury_case",
    @Json(name = "additional_images") val additionalImages: List<String> = emptyList(),
    @Json(name = "compatibility") val compatibility: String = "Universal",
    @Json(name = "colors") val colors: List<String> = listOf("#D4AF37", "#1E1E24", "#708090", "#8A2BE2"),
    @Json(name = "sizes") val sizes: List<String> = listOf("iPhone 16 Pro Max", "iPhone 16 Pro", "iPhone 15 Pro Max", "Galaxy S25 Ultra"),
    @Json(name = "brand") val brand: String = "BoB Elite"
)

enum class ProductSortOption(val titleAr: String, val titleEn: String) {
    DEFAULT("المميز والأحدث", "Featured & Newest"),
    PRICE_LOW_HIGH("السعر: الأقل للأعلى", "Price: Low to High"),
    PRICE_HIGH_LOW("السعر: الأعلى للأقل", "Price: High to Low"),
    RATING_HIGH("الأعلى تقييماً ★", "Highest Rated ★")
}

data class PriceRangeFilter(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val labelAr: String,
    val labelEn: String
)

data class ProductReview(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String = "اليوم"
)

data class StoreCustomization(
    val storeName: String = "BoB store",
    val lbpExchangeRate: Double = 89500.0,
    val accentColorHex: String = "#D4AF37",
    val bannerTitleAr: String = "إكسسوارات هواتف حصرية",
    val bannerTitleEn: String = "Elite Smartphone Gear",
    val bannerSubtitleAr: String = "كفرات أصلية، منصات شحن Qi2، ودروع حماية",
    val bannerSubtitleEn: String = "Titanium cases, Qi2 chargers & camera armor",
    val bannerImageRes: String = "hero_luxury_case",
    val announcementAr: String = "✨ خصم حصري 10% بكود BOB10 | شحن فوري إلى جميع المناطق",
    val whatsappNumber: String = "0096170198429"
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var selectedColor: String = "#D4AF37",
    var selectedSize: String = "iPhone 16 Pro Max"
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    @Json(name = "product_id") val productId: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double,
    @Json(name = "color_choice") val colorChoice: String? = null,
    @Json(name = "size_choice") val sizeChoice: String? = null
)

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "id") val id: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "customer_address") val customerAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "currency") val currency: String = "USD",
    @Json(name = "tracking_number") val trackingNumber: String,
    @Json(name = "created_at") val createdAt: String = "Just now",
    @Json(name = "items") val items: List<OrderItem> = emptyList()
)

data class AdminNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val orderId: String? = null,
    val isRead: Boolean = false
)

data class AdminAnalytics(
    val totalRevenue: Double = 14850.0,
    val totalOrders: Int = 38,
    val totalProducts: Int = 12,
    val totalStockCount: Int = 117,
    val lowStockCount: Int = 2,
    val pendingOrders: Int = 4,
    val totalAdmins: Int = 2
)
