package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OrderServiceTest {

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

    private lateinit var orderRepo: FakeOrderRepository
    private lateinit var service: OrderService

    private val sampleOrder = Order(
        customerId = 1L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @BeforeTest
    fun setUp() {
        orderRepo = FakeOrderRepository()
        service = OrderService(orderRepo)
    }

    @Test
    fun `getOrder returns order by id`() {
        val saved = orderRepo.save(sampleOrder)
        val found = service.getOrder(saved.id)
        assertEquals(saved, found)
    }

    @Test
    fun `getOrder throws OrderNotFoundException for unknown id`() {
        assertFailsWith<OrderNotFoundException> { service.getOrder(999L) }
    }

    @Test
    fun `listOrders returns all orders for customer`() {
        orderRepo.save(sampleOrder)
        orderRepo.save(sampleOrder.copy(customerId = 2L))
        val results = service.listOrders(customerId = 1L)
        assertEquals(1, results.size)
        assertEquals(1L, results.first().customerId)
    }

    @Test
    fun `transition updates order status`() {
        val saved = orderRepo.save(sampleOrder)
        val result = service.transition(saved.id, OrderStatus.Confirmed)
        assertIs<OrderStatus.Confirmed>(result.status)
    }
}
