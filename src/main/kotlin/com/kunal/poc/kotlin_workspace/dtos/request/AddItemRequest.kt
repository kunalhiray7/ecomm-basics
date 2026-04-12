package com.kunal.poc.kotlin_workspace.dtos.request

data class AddItemRequest(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)
