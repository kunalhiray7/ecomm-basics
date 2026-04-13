package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrElse

@Component
class CartPersistenceAdapter(
    private val jpaRepository: CartJpaRepository,
) : CartRepository {

    override fun findById(id: Long): Cart? = jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun save(cart: Cart): Cart = jpaRepository.save(cart.toEntity()).toDomain()
}
