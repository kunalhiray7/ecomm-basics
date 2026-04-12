# Ecommerce App — Cart & Orders Design

**Date:** 2026-04-11
**Stack:** Spring Boot 4, Kotlin, Spring Data JPA + H2, JUnit5
**Architecture:** Hexagonal (Ports & Adapters)
**Pairing mode:** Collaborative TDD — coach writes test + explains, user implements, coach reviews

---

## Goal

Build a Cart → Order domain incrementally using TDD, with the primary objective of sharpening idiomatic Kotlin skills. Start with moderate features (add/remove/update cart items, inventory-checked checkout, order status transitions), then grow toward pricing rules, discounts, and cancellation.

---

## Package Structure

```
com.kunal.poc.kotlin_workspace
├── domain/
│   ├── model/               # Pure Kotlin domain types
│   ├── port/
│   │   ├── inbound/         # Use case interfaces
│   │   └── outbound/        # Repository interfaces
│   ├── service/             # Domain services implementing inbound ports
│   └── exception/           # Sealed domain exceptions
├── adapter/
│   ├── inbound/
│   │   └── rest/            # Spring MVC controllers
│   └── outbound/
│       └── persistence/     # JPA entities + adapters
├── dtos/                    # REST request/response models
└── fixtures/                # Test data
```

**Rule:** `domain/` has zero Spring or JPA imports. Adapters depend on domain; domain never depends on adapters.

---

## Domain Model

### OrderStatus — sealed class
```kotlin
sealed class OrderStatus {
    object Pending : OrderStatus()
    object Confirmed : OrderStatus()
    object Shipped : OrderStatus()
    object Delivered : OrderStatus()
    data class Cancelled(val reason: String) : OrderStatus()
}
```
Using `sealed class` (not enum) so states can carry data and `when` expressions are exhaustive.

### Value Objects
```kotlin
data class CartItem(val productId: Long, val name: String, val quantity: Int, val unitPrice: Double)
data class OrderItem(val productId: Long, val name: String, val quantity: Int, val unitPrice: Double)
```

### Cart — rich domain entity
```kotlin
data class Cart(
    val id: Long,
    val customerId: Long,
    val items: List<CartItem> = emptyList(),
) {
    fun addItem(item: CartItem): Cart
    fun removeItem(productId: Long): Cart
    fun updateQuantity(productId: Long, quantity: Int): Cart
    fun checkout(): Order
    val totalAmount: Double get() = items.sumOf { it.quantity * it.unitPrice }
}
```

### Order — rich domain entity
```kotlin
data class Order(
    val id: Long,
    val customerId: Long,
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.Pending,
    val totalAmount: Double,
) {
    fun transition(next: OrderStatus): Order  // validated state machine
}
```

---

## Ports

### Inbound (Use Cases)
```kotlin
interface CartUseCase {
    fun getCart(cartId: Long): Cart
    fun addItem(cartId: Long, item: CartItem): Cart
    fun removeItem(cartId: Long, productId: Long): Cart
    fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart
    fun checkout(cartId: Long): Order
}

interface OrderUseCase {
    fun getOrder(orderId: Long): Order
    fun listOrders(customerId: Long): List<Order>
    fun transition(orderId: Long, status: OrderStatus): Order
}
```

### Outbound (Repositories)
```kotlin
interface CartRepository {
    fun findById(cartId: Long): Cart?
    fun save(cart: Cart): Cart
}

interface OrderRepository {
    fun findById(orderId: Long): Order?
    fun findByCustomerId(customerId: Long): List<Order>
    fun save(order: Order): Order
}
```
Outbound ports return domain types, never JPA entities. `findById` returns nullable — callers must handle absence explicitly.

---

## Persistence Adapter

JPA entities live in `adapter/outbound/persistence/` and are invisible to the domain. Adapters implement outbound ports and map between JPA entities and domain models via extension functions:

```kotlin
@Component
class CartPersistenceAdapter(private val jpaRepo: CartJpaRepository) : CartRepository {
    override fun findById(id: Long): Cart? = jpaRepo.findById(id).orElse(null)?.toDomain()
    override fun save(cart: Cart): Cart = jpaRepo.save(cart.toEntity()).toDomain()
}
```

---

## Error Handling

Domain exceptions are sealed — exhaustive, typed, no string matching:

```kotlin
sealed class DomainException(message: String) : RuntimeException(message) {
    class CartNotFoundException(id: Long) : DomainException("Cart $id not found")
    class EmptyCartException : DomainException("Cannot checkout an empty cart")
    class InvalidStatusTransitionException(from: OrderStatus, to: OrderStatus)
        : DomainException("Cannot transition from $from to $to")
}
```

A `@ControllerAdvice` in the inbound adapter layer maps domain exceptions to HTTP responses.

---

## TDD Pairing Approach

**Mode C — Collaborative:**
1. Coach writes a failing test + explains the Kotlin patterns used
2. User implements production code to make it pass
3. Coach reviews — flags non-idiomatic patterns, suggests improvements
4. Refactor together before next test

**Order of implementation (inside-out):**
1. Domain model (`Cart`, `Order`, `OrderStatus`)
2. Domain services (`CartService`, `OrderService`)
3. Persistence adapters (JPA entities + mapping)
4. REST controllers

Domain model and service tests are plain JUnit5 — no Spring context, no H2, fast feedback.

---

## Incremental Scope

**Phase 1 (current):** Add/remove/update cart items, checkout with empty-cart guard, order status transitions with validation.

**Phase 2 (next):** Inventory checks on checkout, pricing rules, discount codes, order cancellation with stock restoration.
