package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
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

    override fun createCart(customerId: Long): Cart = cartRepository.save(Cart(customerId = customerId))

    override fun getCart(cartId: Long): Cart = cartRepository.findById(cartId) ?: throw CartNotFoundException(cartId)

    override fun addItem(cartId: Long, item: CartItem): Cart =
        getCart(cartId).addItem(item).let { cartRepository.save(it) }

    override fun removeItem(cartId: Long, productId: Long): Cart =
        getCart(cartId).removeItem(productId).let { cartRepository.save(it) }

    override fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart =
        getCart(cartId).updateQuantity(productId, quantity).let { cartRepository.save(it) }

    override fun checkout(cartId: Long): Order =
        getCart(cartId).checkout().let { orderRepository.save(it) }
}
