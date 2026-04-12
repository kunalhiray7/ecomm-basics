package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Transactional
class OrderPersistenceAdapterTest {

    @Autowired lateinit var jpaRepository: OrderJpaRepository

    private lateinit var adapter: OrderPersistenceAdapter

    private val sampleOrder = Order(
        customerId = 1L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @BeforeEach
    fun setUp() {
        adapter = OrderPersistenceAdapter(jpaRepository)
    }

    @Test
    fun `save assigns id and persists order`() {
        val saved = adapter.save(sampleOrder)
        assert(saved.id > 0L)
        assertIs<OrderStatus.Pending>(saved.status)
    }

    @Test
    fun `findById returns saved order`() {
        val saved = adapter.save(sampleOrder)
        val found = adapter.findById(saved.id)
        assertNotNull(found)
        assertEquals(saved, found)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(adapter.findById(999L))
    }

    @Test
    fun `findByCustomerId returns orders for customer`() {
        adapter.save(sampleOrder)
        adapter.save(sampleOrder.copy(customerId = 2L))
        val results = adapter.findByCustomerId(1L)
        assertEquals(1, results.size)
    }

    @Test
    fun `Cancelled status roundtrips with reason`() {
        val cancelled = sampleOrder.copy(status = OrderStatus.Cancelled("Customer request"))
        val saved = adapter.save(cancelled)
        val found = adapter.findById(saved.id)!!
        val status = assertIs<OrderStatus.Cancelled>(found.status)
        assertEquals("Customer request", status.reason)
    }
}
