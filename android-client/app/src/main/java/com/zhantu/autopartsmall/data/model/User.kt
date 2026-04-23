package com.zhantu.autopartsmall.data.model

data class User(
    val id: Long?,
    val username: String,
    val phone: String?,
    val nickname: String?,
    val avatar: String?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val phone: String?,
    val nickname: String?
)
