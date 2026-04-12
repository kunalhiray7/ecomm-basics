package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.InvalidStatusTransitionException

data class Order(
    val id: Long = 0L,
    val customerId: Long,
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.Pending,
    val totalAmount: Double,
) {
    fun transition(next: OrderStatus): Order = when (status) {
        is OrderStatus.Pending -> when (next) {
            is OrderStatus.Confirmed, is OrderStatus.Cancelled ->
                copy(status = next)

            else -> throwStateTransitionException(next)
        }

        is OrderStatus.Confirmed -> when (next) {
            is OrderStatus.Shipped, is OrderStatus.Cancelled ->
                copy(status = next)

            else -> throwStateTransitionException(next)
        }

        is OrderStatus.Shipped -> when (next) {
            is OrderStatus.Delivered, is OrderStatus.Cancelled ->
                copy(status = next)

            else -> throwStateTransitionException(next)
        }

        is OrderStatus.Delivered,
        is OrderStatus.Cancelled ->
            throwStateTransitionException(next)
    }

    private fun throwStateTransitionException(next: OrderStatus): Nothing {
        throw InvalidStatusTransitionException("Invalid status transition - from $status to $next")
    }
}
