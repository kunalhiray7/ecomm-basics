package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderPersistenceAdapter(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun findById(id: Long): Order?  = jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByCustomerId(customerId: Long): List<Order> = jpaRepository.findByCustomerId(customerId).map { it.toDomain() }

    override fun save(order: Order): Order = jpaRepository.save(order.toEntity()).toDomain()
}
