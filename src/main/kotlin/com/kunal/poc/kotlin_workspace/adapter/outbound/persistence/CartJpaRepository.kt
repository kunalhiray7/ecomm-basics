package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CartJpaRepository : JpaRepository<CartJpaEntity, Long>
