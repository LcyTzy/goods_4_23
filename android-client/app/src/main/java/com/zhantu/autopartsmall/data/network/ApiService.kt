package com.zhantu.autopartsmall.data.network

import com.zhantu.autopartsmall.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("/api/user/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/user/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>

    @GET("/api/product/category/tree")
    suspend fun getCategoryTree(): ApiResponse<List<Category>>

    @GET("/api/product/page")
    suspend fun getProductList(
        @Query("pageNum") current: Int,
        @Query("pageSize") size: Int,
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Long? = null
    ): ApiResponse<PageData<Product>>

    @GET("/api/product/{id}")
    suspend fun getProductDetail(@Path("id") id: Long): ApiResponse<Product>

    @GET("/api/cart/list")
    suspend fun getCartList(@Header("Authorization") token: String): ApiResponse<List<CartItem>>

    @POST("/api/cart/add")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Query("productId") productId: Long,
        @Query("quantity") quantity: Int = 1
    ): ApiResponse<Unit>

    @PUT("/api/cart/update")
    suspend fun updateCartQuantity(
        @Header("Authorization") token: String,
        @Query("cartId") cartId: Long,
        @Query("quantity") quantity: Int
    ): ApiResponse<Unit>

    @DELETE("/api/cart/{id}")
    suspend fun removeFromCart(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Unit>

    @POST("/api/order/create")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): ApiResponse<Order>

    @GET("/api/order/page")
    suspend fun getOrderList(
        @Header("Authorization") token: String,
        @Query("pageNum") current: Int,
        @Query("pageSize") size: Int,
        @Query("status") status: Int? = null
    ): ApiResponse<PageData<Order>>

    @GET("/api/product/page")
    suspend fun getHotProducts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") limit: Int = 10
    ): ApiResponse<PageData<Product>>

    @GET("/api/product/page")
    suspend fun getRecommendProducts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") limit: Int = 10
    ): ApiResponse<PageData<Product>>
}
