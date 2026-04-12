package com.kunal.poc.kotlin_workspace.domain.port.inbound

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order

interface CartUseCase {
    fun createCart(customerId: Long): Cart
    fun getCart(cartId: Long): Cart
    fun addItem(cartId: Long, item: CartItem): Cart
    fun removeItem(cartId: Long, productId: Long): Cart
    fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart
    fun checkout(cartId: Long): Order
}
