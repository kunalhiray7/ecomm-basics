package com.kunal.poc.kotlin_workspace.dtos.response

import com.kunal.poc.kotlin_workspace.domain.model.Cart

data class CartResponse(
    val id: Long,
    val customerId: Long,
    val items: List<CartItemResponse>,
    val totalAmount: Double,
)

data class CartItemResponse(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun Cart.toResponse() = CartResponse(
    id = id,
    customerId = customerId,
    items = items.map { CartItemResponse(it.productId, it.name, it.quantity, it.unitPrice) },
    totalAmount = totalAmount,
)
