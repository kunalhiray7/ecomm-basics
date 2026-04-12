package com.kunal.poc.kotlin_workspace.domain.port.outbound

import com.kunal.poc.kotlin_workspace.domain.model.Cart

interface CartRepository {
    fun findById(id: Long): Cart?
    fun save(cart: Cart): Cart
}
