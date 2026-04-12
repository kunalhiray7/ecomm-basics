package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository

class CartService(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : CartUseCase {

    override fun createCart(customerId: Long): Cart {
        TODO("implement")
    }

    override fun getCart(cartId: Long): Cart {
        TODO("implement")
    }

    override fun addItem(cartId: Long, item: CartItem): Cart {
        TODO("implement")
    }

    override fun removeItem(cartId: Long, productId: Long): Cart {
        TODO("implement")
    }

    override fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart {
        TODO("implement")
    }

    override fun checkout(cartId: Long): Order {
        TODO("implement")
    }
}
