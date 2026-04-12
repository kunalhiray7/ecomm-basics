package com.kunal.poc.kotlin_workspace.domain.model

data class OrderItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)
