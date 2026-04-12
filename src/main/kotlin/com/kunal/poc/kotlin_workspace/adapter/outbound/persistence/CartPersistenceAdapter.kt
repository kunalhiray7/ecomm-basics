package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import org.springframework.stereotype.Component

@Component
class CartPersistenceAdapter(
    private val jpaRepository: CartJpaRepository,
) : CartRepository {

    override fun findById(id: Long): Cart? {
        TODO("implement")
    }

    override fun save(cart: Cart): Cart {
        TODO("implement")
    }
}
