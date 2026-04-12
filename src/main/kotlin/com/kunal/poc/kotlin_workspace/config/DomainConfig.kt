package com.kunal.poc.kotlin_workspace.config

import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import com.kunal.poc.kotlin_workspace.domain.service.CartService
import com.kunal.poc.kotlin_workspace.domain.service.OrderService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {

    @Bean
    fun cartService(cartRepository: CartRepository, orderRepository: OrderRepository) =
        CartService(cartRepository, orderRepository)

    @Bean
    fun orderService(orderRepository: OrderRepository) =
        OrderService(orderRepository)
}
