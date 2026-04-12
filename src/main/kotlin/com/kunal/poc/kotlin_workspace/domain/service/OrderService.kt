package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository

class OrderService(
    private val orderRepository: OrderRepository,
) : OrderUseCase {

    override fun getOrder(orderId: Long): Order {
        TODO("implement")
    }

    override fun listOrders(customerId: Long): List<Order> {
        TODO("implement")
    }

    override fun transition(orderId: Long, status: OrderStatus): Order {
        TODO("implement")
    }
}
