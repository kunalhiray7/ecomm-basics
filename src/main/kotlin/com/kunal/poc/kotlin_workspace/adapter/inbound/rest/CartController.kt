package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import com.kunal.poc.kotlin_workspace.dtos.request.AddItemRequest
import com.kunal.poc.kotlin_workspace.dtos.request.UpdateQuantityRequest
import com.kunal.poc.kotlin_workspace.dtos.response.CartResponse
import com.kunal.poc.kotlin_workspace.dtos.response.OrderResponse
import com.kunal.poc.kotlin_workspace.dtos.response.toResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/carts")
class CartController(private val cartUseCase: CartUseCase) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCart(@RequestParam customerId: Long): CartResponse {
        TODO("implement")
    }

    @GetMapping("/{cartId}")
    fun getCart(@PathVariable cartId: Long): CartResponse {
        TODO("implement")
    }

    @PostMapping("/{cartId}/items")
    fun addItem(@PathVariable cartId: Long, @RequestBody request: AddItemRequest): CartResponse {
        TODO("implement")
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    fun removeItem(@PathVariable cartId: Long, @PathVariable productId: Long): CartResponse {
        TODO("implement")
    }

    @PatchMapping("/{cartId}/items/{productId}")
    fun updateQuantity(
        @PathVariable cartId: Long,
        @PathVariable productId: Long,
        @RequestBody request: UpdateQuantityRequest,
    ): CartResponse {
        TODO("implement")
    }

    @PostMapping("/{cartId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    fun checkout(@PathVariable cartId: Long): OrderResponse {
        TODO("implement")
    }
}
