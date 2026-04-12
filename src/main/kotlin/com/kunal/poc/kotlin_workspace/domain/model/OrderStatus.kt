package com.kunal.poc.kotlin_workspace.domain.model

sealed class OrderStatus {
    object Pending : OrderStatus()
    object Confirmed : OrderStatus()
    object Shipped : OrderStatus()
    object Delivered : OrderStatus()
    data class Cancelled(val reason: String) : OrderStatus()
}
