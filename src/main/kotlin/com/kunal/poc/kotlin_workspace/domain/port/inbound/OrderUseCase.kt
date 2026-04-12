package com.kunal.poc.kotlin_workspace.domain.port.inbound

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

interface OrderUseCase {
    fun getOrder(orderId: Long): Order
    fun listOrders(customerId: Long): List<Order>
    fun transition(orderId: Long, status: OrderStatus): Order
}
