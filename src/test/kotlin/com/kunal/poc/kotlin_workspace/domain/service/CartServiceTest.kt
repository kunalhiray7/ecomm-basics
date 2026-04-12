package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class CartServiceTest {

    private class FakeCartRepository : CartRepository {
        private val store = mutableMapOf<Long, Cart>()
        private var nextId = 1L
        override fun findById(id: Long): Cart? = store[id]
        override fun save(cart: Cart): Cart {
            val toSave = if (cart.id == 0L) cart.copy(id = nextId++) else cart
            return toSave.also { store[it.id] = it }
        }
    }

    private class FakeOrderRepository : OrderRepository {
        private val store = mutableMapOf<Long, Order>()
        private var nextId = 1L
        override fun findById(id: Long): Order? = store[id]
        override fun findByCustomerId(customerId: Long) = store.values.filter { it.customerId == customerId }
        override fun save(order: Order): Order {
            val toSave = if (order.id == 0L) order.copy(id = nextId++) else order
            return toSave.also { store[it.id] = it }
        }
    }

    private lateinit var cartRepo: FakeCartRepository
    private lateinit var orderRepo: FakeOrderRepository
    private lateinit var service: CartService

    @BeforeTest
    fun setUp() {
        cartRepo = FakeCartRepository()
        orderRepo = FakeOrderRepository()
        service = CartService(cartRepo, orderRepo)
    }

    @Test
    fun `createCart saves and returns new cart for customer`() {
        val cart = service.createCart(customerId = 99L)
        assertNotNull(cart.id)
        assertEquals(99L, cart.customerId)
    }

    @Test
    fun `getCart returns cart by id`() {
        val created = service.createCart(customerId = 1L)
        val found = service.getCart(created.id)
        assertEquals(created, found)
    }

    @Test
    fun `getCart throws CartNotFoundException for unknown id`() {
        assertFailsWith<CartNotFoundException> { service.getCart(999L) }
    }

    @Test
    fun `addItem adds item to cart and persists`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        val result = service.addItem(cart.id, item)
        assertEquals(1, result.items.size)
        assertEquals(item, result.items.first())
    }

    @Test
    fun `removeItem removes item from cart`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val result = service.removeItem(cart.id, item.productId)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `updateQuantity updates item quantity in cart`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val result = service.updateQuantity(cart.id, item.productId, 3)
        assertEquals(3, result.items.first().quantity)
    }

    @Test
    fun `checkout converts cart to persisted order`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val order = service.checkout(cart.id)
        assertIs<OrderStatus.Pending>(order.status)
        assertEquals(1L, order.customerId)
        assertNotNull(orderRepo.findById(order.id))
    }
}
