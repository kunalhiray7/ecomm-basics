package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.EmptyCartException
import com.kunal.poc.kotlin_workspace.domain.exception.ItemNotFoundException

data class Cart(
    val id: Long = 0L,
    val customerId: Long,
    val items: List<CartItem> = emptyList(),
) {
    val totalAmount: Double get() = items.sumOf { it.quantity * it.unitPrice }

    fun addItem(item: CartItem): Cart {
        val existing = items.find { it.productId == item.productId }
        return if (existing != null) {
            copy(items = items.map {
                if (it.productId == item.productId) it.copy(quantity = it.quantity + item.quantity)
                else it
            })
        } else {
            copy(items = items + item)
        }
    }

    fun removeItem(productId: Long): Cart =
        copy(items = items.filter { it.productId != productId })

    fun updateQuantity(productId: Long, quantity: Int): Cart {
        items.find { it.productId == productId }
            ?: throw ItemNotFoundException("Item with productId: $productId does not exist in the cart")
        return copy(items = items.map {
            if (it.productId == productId) it.copy(quantity = quantity) else it
        })
    }

    fun checkout(): Order {
        if (items.isEmpty()) {
            throw EmptyCartException("Cart is empty, cannot checkout")
        }
        return Order(
            customerId = customerId,
            status = OrderStatus.Pending,
            totalAmount = totalAmount,
            items = items.map { it.toOrderItem() }
        )
    }
}
