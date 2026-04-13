package com.kunal.poc.kotlin_workspace.dtos.request

import com.kunal.poc.kotlin_workspace.domain.model.CartItem

data class AddItemRequest(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun AddItemRequest.toCartItem() = CartItem(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)