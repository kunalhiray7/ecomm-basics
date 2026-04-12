package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, Long> {
    fun findByCustomerId(customerId: Long): List<OrderJpaEntity>
}
