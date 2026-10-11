package com.example.bobstore.data.remote

import com.example.bobstore.data.model.Category
import com.example.bobstore.data.model.Order
import com.example.bobstore.data.model.Product
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "customer_address") val customerAddress: String,
    @Json(name = "city") val city: String,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "items") val items: List<OrderReqItem>
)

@JsonClass(generateAdapter = true)
data class OrderReqItem(
    @Json(name = "product_id") val productId: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "color_choice") val colorChoice: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminLoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "totp_code") val totpCode: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminLoginResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "token") val token: String? = null,
    @Json(name = "requires_totp") val requiresTotp: Boolean? = false,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class TotpHelperResponse(
    @Json(name = "demo_username") val username: String,
    @Json(name = "demo_password") val password: String,
    @Json(name = "current_totp_code") val currentTotpCode: String,
    @Json(name = "valid_window_seconds") val validWindowSeconds: Int
)

interface BoBApiService {

    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<Product>>>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ApiResponse<Product>>

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<Map<String, Any>>>

    @GET("api/orders/{id}")
    suspend fun trackOrder(@Path("id") id: String): Response<ApiResponse<Order>>

    @POST("api/admin/login")
    suspend fun adminLogin(@Body req: AdminLoginRequest): Response<AdminLoginResponse>

    @GET("api/admin/demo-totp-helper")
    suspend fun getDemoTotp(): Response<TotpHelperResponse>

    @GET("api/admin/orders")
    suspend fun getAdminOrders(@Header("X-Admin-Token") token: String): Response<ApiResponse<List<Order>>>

    @PATCH("api/admin/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("X-Admin-Token") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @POST("api/admin/products")
    suspend fun createProduct(
        @Header("X-Admin-Token") token: String,
        @Body product: Product
    ): Response<ApiResponse<Unit>>

    @DELETE("api/admin/products/{id}")
    suspend fun deleteProduct(
        @Header("X-Admin-Token") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
