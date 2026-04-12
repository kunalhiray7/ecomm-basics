package com.kunal.poc.kotlin_workspace.dtos.response

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

data class OrderResponse(
    val id: Long,
    val customerId: Long,
    val items: List<OrderItemResponse>,
    val status: String,
    val cancelReason: String?,
    val totalAmount: Double,
)

data class OrderItemResponse(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun Order.toResponse() = OrderResponse(
    id = id,
    customerId = customerId,
    items = items.map { OrderItemResponse(it.productId, it.name, it.quantity, it.unitPrice) },
    status = status::class.simpleName ?: "Unknown",
    cancelReason = (status as? OrderStatus.Cancelled)?.reason,
    totalAmount = totalAmount,
)
