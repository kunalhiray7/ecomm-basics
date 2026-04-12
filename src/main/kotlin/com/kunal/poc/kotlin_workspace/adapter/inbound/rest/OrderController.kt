package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.dtos.request.TransitionStatusRequest
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
    fun getOrder(@PathVariable orderId: Long): OrderResponse {
        TODO("implement")
    }

    @GetMapping
    fun listOrders(@RequestParam customerId: Long): List<OrderResponse> {
        TODO("implement")
    }

    @PatchMapping("/{orderId}/status")
    fun transition(
        @PathVariable orderId: Long,
        @RequestBody request: TransitionStatusRequest,
    ): OrderResponse {
        TODO("implement")
    }
}
