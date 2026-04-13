package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository

class OrderService(
    private val orderRepository: OrderRepository,
) : OrderUseCase {

    override fun getOrder(orderId: Long): Order =
        orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)

    override fun listOrders(customerId: Long): List<Order> = orderRepository.findByCustomerId(customerId)

    override fun transition(orderId: Long, status: OrderStatus): Order =
        getOrder(orderId).let { orderRepository.save(it.transition(status)) }
}
