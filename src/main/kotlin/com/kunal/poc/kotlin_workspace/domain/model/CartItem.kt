package com.kunal.poc.kotlin_workspace.domain.model

data class CartItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun CartItem.toOrderItem() = OrderItem(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)
