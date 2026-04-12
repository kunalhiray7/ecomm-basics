package com.kunal.poc.kotlin_workspace.domain.port.outbound

import com.kunal.poc.kotlin_workspace.domain.model.Order

interface OrderRepository {
    fun findById(id: Long): Order?
    fun findByCustomerId(customerId: Long): List<Order>
    fun save(order: Order): Order
}
