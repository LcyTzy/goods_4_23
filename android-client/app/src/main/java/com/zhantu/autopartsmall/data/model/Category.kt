package com.zhantu.autopartsmall.data.model

data class Category(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val level: Int,
    val sort: Int,
    val icon: String?,
    val status: Int,
    val children: List<Category>? = null
)
