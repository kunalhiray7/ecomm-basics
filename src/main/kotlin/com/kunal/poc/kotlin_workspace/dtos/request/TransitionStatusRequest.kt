package com.kunal.poc.kotlin_workspace.dtos.request

import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

data class TransitionStatusRequest(
    val status: String,
    val cancelReason: String? = null,
)

fun TransitionStatusRequest.toOrderStatus(): OrderStatus = when(status) {
    "Confirmed" -> OrderStatus.Confirmed
    "Shipped"   -> OrderStatus.Shipped
    "Delivered" -> OrderStatus.Delivered
    "Cancelled" -> OrderStatus.Cancelled(cancelReason ?: "")
    else        -> throw IllegalArgumentException("Unknown status: $status")
}