package com.kunal.poc.kotlin_workspace.fixtures

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

object OrderFixtures {
    val orders: List<Order> = listOf(
        Order(
            id = 1L, customerId = 101L,
            items = listOf(
                OrderItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99),
                OrderItem(productId = 11L, name = "Mouse", quantity = 2, unitPrice = 29.99),
            ),
            status = OrderStatus.Delivered,
            totalAmount = 1059.97,
        ),
        Order(
            id = 2L, customerId = 102L,
            items = listOf(OrderItem(productId = 20L, name = "Desk Chair", quantity = 1, unitPrice = 249.99)),
            status = OrderStatus.Shipped,
            totalAmount = 249.99,
        ),
        Order(
            id = 3L, customerId = 103L,
            items = listOf(
                OrderItem(productId = 30L, name = "Monitor", quantity = 2, unitPrice = 399.99),
                OrderItem(productId = 31L, name = "HDMI Cable", quantity = 1, unitPrice = 14.99),
            ),
            status = OrderStatus.Pending,
            totalAmount = 814.97,
        ),
    )
}
