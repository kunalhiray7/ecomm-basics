package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "carts")
class CartJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val customerId: Long = 0L,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = [JoinColumn(name = "cart_id")])
    val items: MutableList<CartItemEmbeddable> = mutableListOf(),
)

@Embeddable
data class CartItemEmbeddable(
    val productId: Long = 0L,
    @Column(name = "item_name") val name: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
)

fun Cart.toEntity() = CartJpaEntity(
    id = id,
    customerId = customerId,
    items = items.map { it.toEmbeddable() }.toMutableList(),
)

fun CartJpaEntity.toDomain() = Cart(
    id = id,
    customerId = customerId,
    items = items.map { it.toDomain() },
)

fun CartItem.toEmbeddable() = CartItemEmbeddable(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)

fun CartItemEmbeddable.toDomain() = CartItem(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)
