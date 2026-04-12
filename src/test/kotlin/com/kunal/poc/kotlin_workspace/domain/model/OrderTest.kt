package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.InvalidStatusTransitionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OrderTest {

    private val item = OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    private val pendingOrder = Order(id = 1L, customerId = 42L, items = listOf(item), totalAmount = 999.99)

    @Test
    fun `default status is Pending`() {
        assertIs<OrderStatus.Pending>(pendingOrder.status)
    }

    @Test
    fun `Pending transitions to Confirmed`() {
        val result = pendingOrder.transition(OrderStatus.Confirmed)
        assertIs<OrderStatus.Confirmed>(result.status)
    }

    @Test
    fun `Pending transitions to Cancelled with reason`() {
        val result = pendingOrder.transition(OrderStatus.Cancelled("Customer request"))
        val status = assertIs<OrderStatus.Cancelled>(result.status)
        assertEquals("Customer request", status.reason)
    }

    @Test
    fun `Pending cannot transition to Shipped`() {
        assertFailsWith<InvalidStatusTransitionException> {
            pendingOrder.transition(OrderStatus.Shipped)
        }
    }

    @Test
    fun `Confirmed transitions to Shipped`() {
        val confirmed = pendingOrder.transition(OrderStatus.Confirmed)
        val result = confirmed.transition(OrderStatus.Shipped)
        assertIs<OrderStatus.Shipped>(result.status)
    }

    @Test
    fun `Confirmed transitions to Cancelled with reason`() {
        val confirmed = pendingOrder.transition(OrderStatus.Confirmed)
        val result = confirmed.transition(OrderStatus.Cancelled("Changed mind"))
        val status = assertIs<OrderStatus.Cancelled>(result.status)
        assertEquals("Changed mind", status.reason)
    }

    @Test
    fun `Shipped transitions to Cancelled with reason`() {
        val shipped = pendingOrder.transition(OrderStatus.Confirmed).transition(OrderStatus.Shipped)
        val result = shipped.transition(OrderStatus.Cancelled("Lost in transit"))
        val status = assertIs<OrderStatus.Cancelled>(result.status)
        assertEquals("Lost in transit", status.reason)
    }

    @Test
    fun `Cancelled is a terminal state`() {
        val cancelled = pendingOrder.transition(OrderStatus.Cancelled("Changed mind"))
        assertFailsWith<InvalidStatusTransitionException> {
            cancelled.transition(OrderStatus.Confirmed)
        }
    }

    @Test
    fun `Shipped transitions to Delivered`() {
        val shipped = pendingOrder
            .transition(OrderStatus.Confirmed)
            .transition(OrderStatus.Shipped)
        val result = shipped.transition(OrderStatus.Delivered)
        assertIs<OrderStatus.Delivered>(result.status)
    }

    @Test
    fun `Delivered is a terminal state`() {
        val delivered = pendingOrder
            .transition(OrderStatus.Confirmed)
            .transition(OrderStatus.Shipped)
            .transition(OrderStatus.Delivered)
        assertFailsWith<InvalidStatusTransitionException> {
            delivered.transition(OrderStatus.Cancelled("Too late"))
        }
    }

    @Test
    fun `transition does not mutate original order`() {
        val confirmed = pendingOrder.transition(OrderStatus.Confirmed)
        assertIs<OrderStatus.Pending>(pendingOrder.status)
        assertIs<OrderStatus.Confirmed>(confirmed.status)
    }
}
