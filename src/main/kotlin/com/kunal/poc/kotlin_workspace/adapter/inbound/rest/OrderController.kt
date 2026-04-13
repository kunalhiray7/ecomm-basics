package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.dtos.request.TransitionStatusRequest
import com.kunal.poc.kotlin_workspace.dtos.request.toOrderStatus
import com.kunal.poc.kotlin_workspace.dtos.response.OrderResponse
import com.kunal.poc.kotlin_workspace.dtos.response.toResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderUseCase: OrderUseCase) {

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): OrderResponse = orderUseCase.getOrder(orderId).toResponse()

    @GetMapping
    fun listOrders(@RequestParam customerId: Long): List<OrderResponse> =
        orderUseCase.listOrders(customerId).map { it.toResponse() }

    @PatchMapping("/{orderId}/status")
    fun transition(
        @PathVariable orderId: Long,
        @RequestBody request: TransitionStatusRequest,
    ): OrderResponse = orderUseCase.transition(orderId, request.toOrderStatus()).toResponse()
}
