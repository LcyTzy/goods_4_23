package com.zhantu.autopartsmall.data.model

data class Product(
    val id: Long,
    val code: String,
    val name: String,
    val subName: String?,
    val oem: String?,
    val price: Double,
    val originalPrice: Double?,
    val stock: Int,
    val categoryId: Long?,
    val series: String?,
    val qualityGrade: String?,
    val viscosity: String?,
    val spec: String?,
    val unit: String?,
    val brand: String?,
    val status: Int,
    val sales: Int?,
    val createTime: String?
)
