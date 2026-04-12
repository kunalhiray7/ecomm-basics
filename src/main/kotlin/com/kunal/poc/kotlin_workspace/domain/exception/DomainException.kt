package com.kunal.poc.kotlin_workspace.domain.exception

open class DomainException(message: String = "") : RuntimeException(message)

class CartNotFoundException(id: Long) : DomainException("Cart $id not found")
class OrderNotFoundException(id: Long) : DomainException("Order $id not found")
class InvalidStatusTransitionException(message: String) : DomainException(message)
