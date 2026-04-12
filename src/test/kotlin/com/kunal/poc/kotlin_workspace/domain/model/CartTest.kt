package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.EmptyCartException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CartTest {

    private val laptop = CartItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    private val mouse  = CartItem(productId = 2L, name = "Mouse",  quantity = 1, unitPrice = 29.99)
    private val emptyCart = Cart(id = 1L, customerId = 42L)

    @Test
    fun `addItem adds new item to empty cart`() {
        val result = emptyCart.addItem(laptop)
        assertEquals(1, result.items.size)
        assertEquals(laptop, result.items.first())
    }

    @Test
    fun `addItem merges quantity when product already in cart`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.addItem(laptop.copy(quantity = 2))
        assertEquals(1, result.items.size)
        assertEquals(3, result.items.first().quantity)
    }

    @Test
    fun `addItem does not mutate original cart`() {
        val result = emptyCart.addItem(laptop)
        assertTrue(emptyCart.items.isEmpty())
        assertEquals(1, result.items.size)
    }

    @Test
    fun `removeItem removes item by productId`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        val result = cart.removeItem(laptop.productId)
        assertEquals(1, result.items.size)
        assertEquals(mouse, result.items.first())
    }

    @Test
    fun `removeItem on missing productId returns cart unchanged`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.removeItem(999L)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `updateQuantity changes quantity of existing item`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.updateQuantity(laptop.productId, 5)
        assertEquals(5, result.items.first().quantity)
    }

    @Test
    fun `totalAmount sums all items correctly`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        assertEquals(1029.98, cart.totalAmount, 0.001)
    }

    @Test
    fun `checkout throws EmptyCartException for empty cart`() {
        assertFailsWith<EmptyCartException> { emptyCart.checkout() }
    }

    @Test
    fun `checkout converts cart to pending order`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        val order = cart.checkout()
        assertEquals(cart.customerId, order.customerId)
        assertEquals(2, order.items.size)
        assertEquals(OrderStatus.Pending, order.status)
        assertEquals(cart.totalAmount, order.totalAmount, 0.001)
    }
}
