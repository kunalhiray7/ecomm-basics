package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderPersistenceAdapter(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun findById(id: Long): Order? {
        TODO("implement")
    }

    override fun findByCustomerId(customerId: Long): List<Order> {
        TODO("implement")
    }

    override fun save(order: Order): Order {
        TODO("implement")
    }
}
