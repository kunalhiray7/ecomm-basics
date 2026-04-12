package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
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
@Table(name = "orders")
class OrderJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val customerId: Long = 0L,
    val status: String = "Pending",
    val cancelReason: String? = null,
    val totalAmount: Double = 0.0,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = [JoinColumn(name = "order_id")])
    val items: MutableList<OrderItemEmbeddable> = mutableListOf(),
)

@Embeddable
data class OrderItemEmbeddable(
    val productId: Long = 0L,
    @Column(name = "item_name") val name: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
)

fun Order.toEntity() = OrderJpaEntity(
    id = id,
    customerId = customerId,
    status = status.toStatusString(),
    cancelReason = (status as? OrderStatus.Cancelled)?.reason,
    totalAmount = totalAmount,
    items = items.map { it.toEmbeddable() }.toMutableList(),
)

fun OrderJpaEntity.toDomain() = Order(
    id = id,
    customerId = customerId,
    status = status.toDomainStatus(cancelReason),
    totalAmount = totalAmount,
    items = items.map { it.toDomain() },
)

fun OrderItem.toEmbeddable() = OrderItemEmbeddable(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)

fun OrderItemEmbeddable.toDomain() = OrderItem(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)

private fun OrderStatus.toStatusString() = when (this) {
    is OrderStatus.Pending   -> "Pending"
    is OrderStatus.Confirmed -> "Confirmed"
    is OrderStatus.Shipped   -> "Shipped"
    is OrderStatus.Delivered -> "Delivered"
    is OrderStatus.Cancelled -> "Cancelled"
}

private fun String.toDomainStatus(cancelReason: String?) = when (this) {
    "Pending"   -> OrderStatus.Pending
    "Confirmed" -> OrderStatus.Confirmed
    "Shipped"   -> OrderStatus.Shipped
    "Delivered" -> OrderStatus.Delivered
    "Cancelled" -> OrderStatus.Cancelled(cancelReason ?: "")
    else        -> throw IllegalStateException("Unknown order status: $this")
}
