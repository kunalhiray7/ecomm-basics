package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Transactional
class CartPersistenceAdapterTest {

    @Autowired lateinit var jpaRepository: CartJpaRepository

    private lateinit var adapter: CartPersistenceAdapter

    @BeforeEach
    fun setUp() {
        adapter = CartPersistenceAdapter(jpaRepository)
    }

    @Test
    fun `save assigns id and persists cart`() {
        val saved = adapter.save(Cart(customerId = 1L))
        assert(saved.id > 0L)
        assertEquals(1L, saved.customerId)
    }

    @Test
    fun `findById returns saved cart`() {
        val saved = adapter.save(Cart(customerId = 1L))
        val found = adapter.findById(saved.id)
        assertNotNull(found)
        assertEquals(saved, found)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(adapter.findById(999L))
    }

    @Test
    fun `save persists cart with items and roundtrips correctly`() {
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 2, unitPrice = 999.99)
        val saved = adapter.save(Cart(customerId = 1L, items = listOf(item)))
        val found = adapter.findById(saved.id)!!
        assertEquals(1, found.items.size)
        assertEquals(item, found.items.first())
    }
}
