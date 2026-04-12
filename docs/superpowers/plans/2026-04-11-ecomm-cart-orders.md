# Ecommerce Cart & Orders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Cart → Order domain using hexagonal architecture and TDD, with H2 persistence and Spring Boot 4 REST adapters.

**Architecture:** Hexagonal (Ports & Adapters). Domain layer (`domain/`) is pure Kotlin with zero Spring/JPA imports. Adapters (`adapter/`) wire the domain to HTTP and JPA. Domain services implement inbound ports; JPA adapters implement outbound ports.

**Tech Stack:** Kotlin, Spring Boot 4, Spring Data JPA, H2, JUnit5, kotlin-test

---

## File Map

### New Files — Domain
| File | Responsibility |
|------|---------------|
| `domain/model/OrderStatus.kt` | Sealed class for order state |
| `domain/model/CartItem.kt` | Value object |
| `domain/model/OrderItem.kt` | Value object |
| `domain/model/Cart.kt` | Rich entity — cart mutation logic |
| `domain/model/Order.kt` | Rich entity — status transition state machine |
| `domain/exception/DomainException.kt` | Sealed domain exceptions |
| `domain/port/inbound/CartUseCase.kt` | Inbound port — cart operations |
| `domain/port/inbound/OrderUseCase.kt` | Inbound port — order operations |
| `domain/port/outbound/CartRepository.kt` | Outbound port — cart persistence contract |
| `domain/port/outbound/OrderRepository.kt` | Outbound port — order persistence contract |
| `domain/service/CartService.kt` | Implements CartUseCase, orchestrates domain + repos |
| `domain/service/OrderService.kt` | Implements OrderUseCase, orchestrates domain + repos |

### New Files — Adapters
| File | Responsibility |
|------|---------------|
| `adapter/outbound/persistence/CartJpaEntity.kt` | JPA entity + embeddable for cart |
| `adapter/outbound/persistence/OrderJpaEntity.kt` | JPA entity + embeddable for order |
| `adapter/outbound/persistence/CartJpaRepository.kt` | Spring Data JPA interface |
| `adapter/outbound/persistence/OrderJpaRepository.kt` | Spring Data JPA interface |
| `adapter/outbound/persistence/CartPersistenceAdapter.kt` | Implements CartRepository outbound port |
| `adapter/outbound/persistence/OrderPersistenceAdapter.kt` | Implements OrderRepository outbound port |
| `adapter/inbound/rest/CartController.kt` | REST adapter for cart use cases |
| `adapter/inbound/rest/GlobalExceptionHandler.kt` | Maps DomainException → HTTP status |

### New Files — DTOs
| File | Responsibility |
|------|---------------|
| `dtos/request/AddItemRequest.kt` | Request body for adding cart item |
| `dtos/request/UpdateQuantityRequest.kt` | Request body for quantity update |
| `dtos/request/TransitionStatusRequest.kt` | Request body for order status change |
| `dtos/response/CartResponse.kt` | REST response for cart |
| `dtos/response/OrderResponse.kt` | REST response for order |

### Modified Files
| File | Change |
|------|--------|
| `build.gradle.kts` | Add JPA + H2 dependencies |
| `src/main/resources/application.properties` | H2 datasource config |
| `adapter/inbound/rest/OrderController.kt` | Move from `controllers/`, wire to OrderUseCase |
| `fixtures/OrderFixtures.kt` | Update to use new domain types |

### Deleted Files
| File | Reason |
|------|--------|
| `controllers/OrderController.kt` | Replaced by `adapter/inbound/rest/OrderController.kt` |
| `dtos/Order.kt` | Replaced by `domain/model/` types + `dtos/response/OrderResponse.kt` |

### Test Files
| File | Type |
|------|------|
| `domain/model/CartTest.kt` | Pure unit — no Spring |
| `domain/model/OrderTest.kt` | Pure unit — no Spring |
| `domain/service/CartServiceTest.kt` | Unit with fake repos — no Spring |
| `domain/service/OrderServiceTest.kt` | Unit with fake repos — no Spring |
| `adapter/outbound/persistence/CartPersistenceAdapterTest.kt` | `@DataJpaTest` |
| `adapter/outbound/persistence/OrderPersistenceAdapterTest.kt` | `@DataJpaTest` |
| `adapter/inbound/rest/CartControllerTest.kt` | `@WebMvcTest` |
| `adapter/inbound/rest/OrderControllerTest.kt` | `@WebMvcTest` |

---

## Task 1: Add JPA + H2 Dependencies

**Files:**
- Modify: `build.gradle.kts`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Add dependencies to build.gradle.kts**

In the `dependencies` block, add:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("com.h2database:h2")
```

- [ ] **Step 2: Configure H2 in application.properties**

Create/update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:ecommdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

- [ ] **Step 3: Verify the build still passes**

```bash
./gradlew build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts src/main/resources/application.properties
git commit -m "chore: add spring-data-jpa and h2 dependencies"
```

---

## Task 2: Domain Value Types

No behaviour tests needed — these are pure data holders. Getting the types right unlocks all subsequent tasks.

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/OrderStatus.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/CartItem.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/OrderItem.kt`

- [ ] **Step 1: Create OrderStatus as a sealed class**

```kotlin
// domain/model/OrderStatus.kt
package com.kunal.poc.kotlin_workspace.domain.model

sealed class OrderStatus {
    object Pending : OrderStatus()
    object Confirmed : OrderStatus()
    object Shipped : OrderStatus()
    object Delivered : OrderStatus()
    data class Cancelled(val reason: String) : OrderStatus()
}
```

Why `sealed class` not `enum`: `Cancelled` carries a reason string. Sealed classes let each variant hold its own data. The compiler enforces exhaustive `when` expressions — you can never forget a case.

- [ ] **Step 2: Create CartItem and OrderItem**

```kotlin
// domain/model/CartItem.kt
package com.kunal.poc.kotlin_workspace.domain.model

data class CartItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun CartItem.toOrderItem() = OrderItem(
    productId = productId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
)
```

```kotlin
// domain/model/OrderItem.kt
package com.kunal.poc.kotlin_workspace.domain.model

data class OrderItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)
```

The `toOrderItem()` extension function on `CartItem` keeps the mapping logic close to the type it extends, rather than inside `Cart.checkout()`.

- [ ] **Step 3: Build to verify types compile**

```bash
./gradlew compileKotlin
```
Expected: no errors

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/
git commit -m "feat: add domain value types OrderStatus, CartItem, OrderItem"
```

---

## Task 3: Cart Domain Entity

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/Cart.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/CartTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/CartTest.kt
package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.EmptyCartException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CartTest {

    private val laptop = CartItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    private val mouse  = CartItem(productId = 2L, name = "Mouse",  quantity = 1, unitPrice = 29.99)
    private val emptyCart = Cart(id = 1L, customerId = 42L)

    @Test
    fun `addItem adds new item to empty cart`() {
        val result = emptyCart.addItem(laptop)
        assertEquals(1, result.items.size)
        assertEquals(laptop, result.items.first())
    }

    @Test
    fun `addItem merges quantity when product already in cart`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.addItem(laptop.copy(quantity = 2))
        assertEquals(1, result.items.size)
        assertEquals(3, result.items.first().quantity)
    }

    @Test
    fun `addItem does not mutate original cart`() {
        val result = emptyCart.addItem(laptop)
        assertTrue(emptyCart.items.isEmpty())
        assertEquals(1, result.items.size)
    }

    @Test
    fun `removeItem removes item by productId`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        val result = cart.removeItem(laptop.productId)
        assertEquals(1, result.items.size)
        assertEquals(mouse, result.items.first())
    }

    @Test
    fun `removeItem on missing productId returns cart unchanged`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.removeItem(999L)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `updateQuantity changes quantity of existing item`() {
        val cart = emptyCart.addItem(laptop)
        val result = cart.updateQuantity(laptop.productId, 5)
        assertEquals(5, result.items.first().quantity)
    }

    @Test
    fun `totalAmount sums all items correctly`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        assertEquals(1029.98, cart.totalAmount, 0.001)
    }

    @Test
    fun `checkout throws EmptyCartException for empty cart`() {
        assertFailsWith<EmptyCartException> { emptyCart.checkout() }
    }

    @Test
    fun `checkout converts cart to pending order`() {
        val cart = emptyCart.addItem(laptop).addItem(mouse)
        val order = cart.checkout()
        assertEquals(cart.customerId, order.customerId)
        assertEquals(2, order.items.size)
        assertEquals(OrderStatus.Pending, order.status)
        assertEquals(cart.totalAmount, order.totalAmount, 0.001)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.model.CartTest"
```
Expected: FAIL — `Cart` and `EmptyCartException` do not exist yet.

- [ ] **Step 3: Create DomainException (needed by Cart)**

```kotlin
// src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/exception/DomainException.kt
package com.kunal.poc.kotlin_workspace.domain.exception

import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

sealed class DomainException(message: String) : RuntimeException(message) {
    class CartNotFoundException(id: Long) : DomainException("Cart $id not found")
    class OrderNotFoundException(id: Long) : DomainException("Order $id not found")
    class EmptyCartException : DomainException("Cannot checkout an empty cart")
    class InvalidStatusTransitionException(from: OrderStatus, to: OrderStatus)
        : DomainException("Cannot transition from ${from::class.simpleName} to ${to::class.simpleName}")
}

// Type aliases for convenience at call sites
typealias CartNotFoundException = DomainException.CartNotFoundException
typealias OrderNotFoundException = DomainException.OrderNotFoundException
typealias EmptyCartException = DomainException.EmptyCartException
typealias InvalidStatusTransitionException = DomainException.InvalidStatusTransitionException
```

- [ ] **Step 4: Implement Cart**

```kotlin
// src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/Cart.kt
package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.EmptyCartException

data class Cart(
    val id: Long = 0L,
    val customerId: Long,
    val items: List<CartItem> = emptyList(),
) {
    val totalAmount: Double get() = items.sumOf { it.quantity * it.unitPrice }

    fun addItem(item: CartItem): Cart {
        val existing = items.find { it.productId == item.productId }
        return if (existing != null) {
            copy(items = items.map {
                if (it.productId == item.productId) it.copy(quantity = it.quantity + item.quantity)
                else it
            })
        } else {
            copy(items = items + item)
        }
    }

    fun removeItem(productId: Long): Cart =
        copy(items = items.filter { it.productId != productId })

    fun updateQuantity(productId: Long, quantity: Int): Cart =
        copy(items = items.map {
            if (it.productId == productId) it.copy(quantity = quantity) else it
        })

    fun checkout(): Order {
        if (items.isEmpty()) throw EmptyCartException()
        return Order(
            customerId = customerId,
            items = items.map { it.toOrderItem() },
            totalAmount = totalAmount,
        )
    }
}
```

Key patterns: `copy()` for immutable updates, `find`/`filter`/`map` instead of loops, computed property `totalAmount`.

- [ ] **Step 5: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.model.CartTest"
```
Expected: all 9 tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/ \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/CartTest.kt
git commit -m "feat: add Cart domain entity with immutable mutation methods"
```

---

## Task 4: Order Domain Entity

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/Order.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/OrderTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/OrderTest.kt
package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.InvalidStatusTransitionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OrderTest {

    private val item = OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    private val pendingOrder = Order(id = 1L, customerId = 42L, items = listOf(item), totalAmount = 999.99)

    @Test
    fun `default status is Pending`() {
        assertIs<OrderStatus.Pending>(pendingOrder.status)
    }

    @Test
    fun `Pending transitions to Confirmed`() {
        val result = pendingOrder.transition(OrderStatus.Confirmed)
        assertIs<OrderStatus.Confirmed>(result.status)
    }

    @Test
    fun `Pending transitions to Cancelled with reason`() {
        val result = pendingOrder.transition(OrderStatus.Cancelled("Customer request"))
        val status = assertIs<OrderStatus.Cancelled>(result.status)
        assertEquals("Customer request", status.reason)
    }

    @Test
    fun `Pending cannot transition to Shipped`() {
        assertFailsWith<InvalidStatusTransitionException> {
            pendingOrder.transition(OrderStatus.Shipped)
        }
    }

    @Test
    fun `Confirmed transitions to Shipped`() {
        val confirmed = pendingOrder.transition(OrderStatus.Confirmed)
        val result = confirmed.transition(OrderStatus.Shipped)
        assertIs<OrderStatus.Shipped>(result.status)
    }

    @Test
    fun `Shipped transitions to Delivered`() {
        val shipped = pendingOrder
            .transition(OrderStatus.Confirmed)
            .transition(OrderStatus.Shipped)
        val result = shipped.transition(OrderStatus.Delivered)
        assertIs<OrderStatus.Delivered>(result.status)
    }

    @Test
    fun `Delivered is a terminal state — no further transitions allowed`() {
        val delivered = pendingOrder
            .transition(OrderStatus.Confirmed)
            .transition(OrderStatus.Shipped)
            .transition(OrderStatus.Delivered)
        assertFailsWith<InvalidStatusTransitionException> {
            delivered.transition(OrderStatus.Cancelled("Too late"))
        }
    }

    @Test
    fun `transition does not mutate original order`() {
        val confirmed = pendingOrder.transition(OrderStatus.Confirmed)
        assertIs<OrderStatus.Pending>(pendingOrder.status)
        assertIs<OrderStatus.Confirmed>(confirmed.status)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.model.OrderTest"
```
Expected: FAIL — `Order` does not exist yet.

- [ ] **Step 3: Implement Order**

```kotlin
// src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/Order.kt
package com.kunal.poc.kotlin_workspace.domain.model

import com.kunal.poc.kotlin_workspace.domain.exception.InvalidStatusTransitionException

data class Order(
    val id: Long = 0L,
    val customerId: Long,
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.Pending,
    val totalAmount: Double,
) {
    fun transition(next: OrderStatus): Order {
        val allowed = when (status) {
            is OrderStatus.Pending   -> next is OrderStatus.Confirmed || next is OrderStatus.Cancelled
            is OrderStatus.Confirmed -> next is OrderStatus.Shipped   || next is OrderStatus.Cancelled
            is OrderStatus.Shipped   -> next is OrderStatus.Delivered || next is OrderStatus.Cancelled
            is OrderStatus.Delivered,
            is OrderStatus.Cancelled -> false
        }
        if (!allowed) throw InvalidStatusTransitionException(status, next)
        return copy(status = next)
    }
}
```

The `when` on `status` is exhaustive — the compiler will error if you add a new `OrderStatus` subtype without handling it here. That's the sealed class guarantee.

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.model.OrderTest"
```
Expected: all 8 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/model/Order.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/model/OrderTest.kt
git commit -m "feat: add Order domain entity with validated status transition state machine"
```

---

## Task 5: Ports

No behaviour to test — these are interfaces. Getting signatures right here locks in the contracts for services and adapters.

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/port/inbound/CartUseCase.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/port/inbound/OrderUseCase.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/port/outbound/CartRepository.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/port/outbound/OrderRepository.kt`

- [ ] **Step 1: Create inbound ports**

```kotlin
// domain/port/inbound/CartUseCase.kt
package com.kunal.poc.kotlin_workspace.domain.port.inbound

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order

interface CartUseCase {
    fun createCart(customerId: Long): Cart
    fun getCart(cartId: Long): Cart
    fun addItem(cartId: Long, item: CartItem): Cart
    fun removeItem(cartId: Long, productId: Long): Cart
    fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart
    fun checkout(cartId: Long): Order
}
```

```kotlin
// domain/port/inbound/OrderUseCase.kt
package com.kunal.poc.kotlin_workspace.domain.port.inbound

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

interface OrderUseCase {
    fun getOrder(orderId: Long): Order
    fun listOrders(customerId: Long): List<Order>
    fun transition(orderId: Long, status: OrderStatus): Order
}
```

- [ ] **Step 2: Create outbound ports**

```kotlin
// domain/port/outbound/CartRepository.kt
package com.kunal.poc.kotlin_workspace.domain.port.outbound

import com.kunal.poc.kotlin_workspace.domain.model.Cart

interface CartRepository {
    fun findById(id: Long): Cart?
    fun save(cart: Cart): Cart
}
```

```kotlin
// domain/port/outbound/OrderRepository.kt
package com.kunal.poc.kotlin_workspace.domain.port.outbound

import com.kunal.poc.kotlin_workspace.domain.model.Order

interface OrderRepository {
    fun findById(id: Long): Order?
    fun findByCustomerId(customerId: Long): List<Order>
    fun save(order: Order): Order
}
```

- [ ] **Step 3: Build to verify all types resolve**

```bash
./gradlew compileKotlin
```
Expected: no errors

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/port/
git commit -m "feat: add inbound and outbound port interfaces"
```

---

## Task 6: CartService

Tests use a hand-rolled in-memory fake — no Mockito, no Spring. This is idiomatic Kotlin for fast, readable unit tests.

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartService.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartServiceTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartServiceTest.kt
package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class CartServiceTest {

    // In-memory fakes — idiomatic Kotlin, no Mockito needed
    private class FakeCartRepository : CartRepository {
        private val store = mutableMapOf<Long, Cart>()
        private var nextId = 1L
        override fun findById(id: Long): Cart? = store[id]
        override fun save(cart: Cart): Cart {
            val toSave = if (cart.id == 0L) cart.copy(id = nextId++) else cart
            return toSave.also { store[it.id] = it }
        }
    }

    private class FakeOrderRepository : OrderRepository {
        private val store = mutableMapOf<Long, Order>()
        private var nextId = 1L
        override fun findById(id: Long): Order? = store[id]
        override fun findByCustomerId(customerId: Long) = store.values.filter { it.customerId == customerId }
        override fun save(order: Order): Order {
            val toSave = if (order.id == 0L) order.copy(id = nextId++) else order
            return toSave.also { store[it.id] = it }
        }
    }

    private lateinit var cartRepo: FakeCartRepository
    private lateinit var orderRepo: FakeOrderRepository
    private lateinit var service: CartService

    @BeforeTest
    fun setUp() {
        cartRepo = FakeCartRepository()
        orderRepo = FakeOrderRepository()
        service = CartService(cartRepo, orderRepo)
    }

    @Test
    fun `createCart saves and returns new cart for customer`() {
        val cart = service.createCart(customerId = 99L)
        assertNotNull(cart.id)
        assertEquals(99L, cart.customerId)
    }

    @Test
    fun `getCart returns cart by id`() {
        val created = service.createCart(customerId = 1L)
        val found = service.getCart(created.id)
        assertEquals(created, found)
    }

    @Test
    fun `getCart throws CartNotFoundException for unknown id`() {
        assertFailsWith<CartNotFoundException> { service.getCart(999L) }
    }

    @Test
    fun `addItem adds item to cart and persists`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        val result = service.addItem(cart.id, item)
        assertEquals(1, result.items.size)
        assertEquals(item, result.items.first())
    }

    @Test
    fun `removeItem removes item from cart`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val result = service.removeItem(cart.id, item.productId)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `updateQuantity updates item quantity in cart`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val result = service.updateQuantity(cart.id, item.productId, 3)
        assertEquals(3, result.items.first().quantity)
    }

    @Test
    fun `checkout converts cart to persisted order`() {
        val cart = service.createCart(customerId = 1L)
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
        service.addItem(cart.id, item)
        val order = service.checkout(cart.id)
        assertIs<OrderStatus.Pending>(order.status)
        assertEquals(1L, order.customerId)
        assertNotNull(orderRepo.findById(order.id))
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.service.CartServiceTest"
```
Expected: FAIL — `CartService` does not exist yet.

- [ ] **Step 3: Implement CartService**

```kotlin
// src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartService.kt
package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository

class CartService(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : CartUseCase {

    override fun createCart(customerId: Long): Cart =
        cartRepository.save(Cart(customerId = customerId))

    override fun getCart(cartId: Long): Cart =
        cartRepository.findById(cartId) ?: throw CartNotFoundException(cartId)

    override fun addItem(cartId: Long, item: CartItem): Cart =
        getCart(cartId).addItem(item).let { cartRepository.save(it) }

    override fun removeItem(cartId: Long, productId: Long): Cart =
        getCart(cartId).removeItem(productId).let { cartRepository.save(it) }

    override fun updateQuantity(cartId: Long, productId: Long, quantity: Int): Cart =
        getCart(cartId).updateQuantity(productId, quantity).let { cartRepository.save(it) }

    override fun checkout(cartId: Long): Order =
        getCart(cartId).checkout().let { orderRepository.save(it) }
}
```

`let` chains — load domain object, call domain method (returns new instance), persist, return result. No intermediate variables needed.

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.service.CartServiceTest"
```
Expected: all 8 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartService.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/CartServiceTest.kt
git commit -m "feat: add CartService implementing CartUseCase"
```

---

## Task 7: OrderService

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderService.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderServiceTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderServiceTest.kt
package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OrderServiceTest {

    private class FakeOrderRepository : OrderRepository {
        private val store = mutableMapOf<Long, Order>()
        private var nextId = 1L
        override fun findById(id: Long): Order? = store[id]
        override fun findByCustomerId(customerId: Long) = store.values.filter { it.customerId == customerId }
        override fun save(order: Order): Order {
            val toSave = if (order.id == 0L) order.copy(id = nextId++) else order
            return toSave.also { store[it.id] = it }
        }
    }

    private lateinit var orderRepo: FakeOrderRepository
    private lateinit var service: OrderService

    private val sampleOrder = Order(
        customerId = 1L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @BeforeTest
    fun setUp() {
        orderRepo = FakeOrderRepository()
        service = OrderService(orderRepo)
    }

    @Test
    fun `getOrder returns order by id`() {
        val saved = orderRepo.save(sampleOrder)
        val found = service.getOrder(saved.id)
        assertEquals(saved, found)
    }

    @Test
    fun `getOrder throws OrderNotFoundException for unknown id`() {
        assertFailsWith<OrderNotFoundException> { service.getOrder(999L) }
    }

    @Test
    fun `listOrders returns all orders for customer`() {
        orderRepo.save(sampleOrder)
        orderRepo.save(sampleOrder.copy(customerId = 2L))
        val results = service.listOrders(customerId = 1L)
        assertEquals(1, results.size)
        assertEquals(1L, results.first().customerId)
    }

    @Test
    fun `transition updates order status`() {
        val saved = orderRepo.save(sampleOrder)
        val result = service.transition(saved.id, OrderStatus.Confirmed)
        assertIs<OrderStatus.Confirmed>(result.status)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.service.OrderServiceTest"
```
Expected: FAIL — `OrderService` does not exist yet.

- [ ] **Step 3: Implement OrderService**

```kotlin
// src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderService.kt
package com.kunal.poc.kotlin_workspace.domain.service

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository

class OrderService(
    private val orderRepository: OrderRepository,
) : OrderUseCase {

    override fun getOrder(orderId: Long): Order =
        orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)

    override fun listOrders(customerId: Long): List<Order> =
        orderRepository.findByCustomerId(customerId)

    override fun transition(orderId: Long, status: OrderStatus): Order =
        getOrder(orderId).transition(status).let { orderRepository.save(it) }
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.domain.service.OrderServiceTest"
```
Expected: all 4 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderService.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/domain/service/OrderServiceTest.kt
git commit -m "feat: add OrderService implementing OrderUseCase"
```

---

## Task 8: JPA Entities + Mapping

No behaviour tests — these are structural. The persistence adapter tests in Tasks 9–10 will validate round-trip correctness.

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartJpaEntity.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderJpaEntity.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartJpaRepository.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderJpaRepository.kt`

- [ ] **Step 1: Create Cart JPA entity**

```kotlin
// adapter/outbound/persistence/CartJpaEntity.kt
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

// Mapping extensions — domain ↔ JPA
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
```

- [ ] **Step 2: Create Order JPA entity**

```kotlin
// adapter/outbound/persistence/OrderJpaEntity.kt
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

// Mapping extensions
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
```

- [ ] **Step 3: Create Spring Data JPA repositories**

```kotlin
// adapter/outbound/persistence/CartJpaRepository.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CartJpaRepository : JpaRepository<CartJpaEntity, Long>
```

```kotlin
// adapter/outbound/persistence/OrderJpaRepository.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, Long> {
    fun findByCustomerId(customerId: Long): List<OrderJpaEntity>
}
```

- [ ] **Step 4: Build to verify**

```bash
./gradlew compileKotlin
```
Expected: no errors

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/
git commit -m "feat: add JPA entities and domain mapping extensions for Cart and Order"
```

---

## Task 9: CartPersistenceAdapter

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartPersistenceAdapter.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartPersistenceAdapterTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartPersistenceAdapterTest.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
class CartPersistenceAdapterTest {

    @Autowired lateinit var jpaRepository: CartJpaRepository

    private lateinit var adapter: CartPersistenceAdapter

    @BeforeTest
    fun setUp() {
        adapter = CartPersistenceAdapter(jpaRepository)
    }

    @Test
    fun `save assigns id and persists cart`() {
        val cart = Cart(customerId = 1L)
        val saved = adapter.save(cart)
        assert(saved.id > 0L)
        assertEquals(1L, saved.customerId)
    }

    @Test
    fun `findById returns saved cart`() {
        val saved = adapter.save(Cart(customerId = 1L))
        val found = adapter.findById(saved.id)
        assertNotNull(found)
        assertEquals(saved, found)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(adapter.findById(999L))
    }

    @Test
    fun `save persists cart with items and roundtrips correctly`() {
        val item = CartItem(productId = 10L, name = "Laptop", quantity = 2, unitPrice = 999.99)
        val cart = Cart(customerId = 1L, items = listOf(item))
        val saved = adapter.save(cart)
        val found = adapter.findById(saved.id)!!
        assertEquals(1, found.items.size)
        assertEquals(item, found.items.first())
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.outbound.persistence.CartPersistenceAdapterTest"
```
Expected: FAIL — `CartPersistenceAdapter` does not exist yet.

- [ ] **Step 3: Implement CartPersistenceAdapter**

```kotlin
// adapter/outbound/persistence/CartPersistenceAdapter.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import org.springframework.stereotype.Component

@Component
class CartPersistenceAdapter(
    private val jpaRepository: CartJpaRepository,
) : CartRepository {

    override fun findById(id: Long): Cart? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun save(cart: Cart): Cart =
        jpaRepository.save(cart.toEntity()).toDomain()
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.outbound.persistence.CartPersistenceAdapterTest"
```
Expected: all 4 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartPersistenceAdapter.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/CartPersistenceAdapterTest.kt
git commit -m "feat: add CartPersistenceAdapter wiring JPA to CartRepository port"
```

---

## Task 10: OrderPersistenceAdapter

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderPersistenceAdapter.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderPersistenceAdapterTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderPersistenceAdapterTest.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
class OrderPersistenceAdapterTest {

    @Autowired lateinit var jpaRepository: OrderJpaRepository

    private lateinit var adapter: OrderPersistenceAdapter

    private val sampleOrder = Order(
        customerId = 1L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @BeforeTest
    fun setUp() {
        adapter = OrderPersistenceAdapter(jpaRepository)
    }

    @Test
    fun `save assigns id and persists order`() {
        val saved = adapter.save(sampleOrder)
        assert(saved.id > 0L)
        assertIs<OrderStatus.Pending>(saved.status)
    }

    @Test
    fun `findById returns saved order`() {
        val saved = adapter.save(sampleOrder)
        val found = adapter.findById(saved.id)
        assertNotNull(found)
        assertEquals(saved, found)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(adapter.findById(999L))
    }

    @Test
    fun `findByCustomerId returns orders for customer`() {
        adapter.save(sampleOrder)
        adapter.save(sampleOrder.copy(customerId = 2L))
        val results = adapter.findByCustomerId(1L)
        assertEquals(1, results.size)
    }

    @Test
    fun `Cancelled status roundtrips with reason`() {
        val cancelled = sampleOrder.copy(status = OrderStatus.Cancelled("Customer request"))
        val saved = adapter.save(cancelled)
        val found = adapter.findById(saved.id)!!
        val status = assertIs<OrderStatus.Cancelled>(found.status)
        assertEquals("Customer request", status.reason)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.outbound.persistence.OrderPersistenceAdapterTest"
```
Expected: FAIL — `OrderPersistenceAdapter` does not exist yet.

- [ ] **Step 3: Implement OrderPersistenceAdapter**

```kotlin
// adapter/outbound/persistence/OrderPersistenceAdapter.kt
package com.kunal.poc.kotlin_workspace.adapter.outbound.persistence

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderPersistenceAdapter(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun findById(id: Long): Order? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCustomerId(customerId: Long): List<Order> =
        jpaRepository.findByCustomerId(customerId).map { it.toDomain() }

    override fun save(order: Order): Order =
        jpaRepository.save(order.toEntity()).toDomain()
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.outbound.persistence.OrderPersistenceAdapterTest"
```
Expected: all 5 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderPersistenceAdapter.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/outbound/persistence/OrderPersistenceAdapterTest.kt
git commit -m "feat: add OrderPersistenceAdapter wiring JPA to OrderRepository port"
```

---

## Task 11: Wire Services as Spring Beans

Domain services are plain Kotlin classes — no `@Service`. Register them as `@Bean` in a configuration class so Spring can inject them.

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/config/DomainConfig.kt`

- [ ] **Step 1: Create DomainConfig**

```kotlin
// config/DomainConfig.kt
package com.kunal.poc.kotlin_workspace.config

import com.kunal.poc.kotlin_workspace.domain.port.outbound.CartRepository
import com.kunal.poc.kotlin_workspace.domain.port.outbound.OrderRepository
import com.kunal.poc.kotlin_workspace.domain.service.CartService
import com.kunal.poc.kotlin_workspace.domain.service.OrderService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {

    @Bean
    fun cartService(cartRepository: CartRepository, orderRepository: OrderRepository) =
        CartService(cartRepository, orderRepository)

    @Bean
    fun orderService(orderRepository: OrderRepository) =
        OrderService(orderRepository)
}
```

- [ ] **Step 2: Verify the full build passes**

```bash
./gradlew build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/config/DomainConfig.kt
git commit -m "chore: wire domain services as Spring beans via DomainConfig"
```

---

## Task 12: REST DTOs

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/request/AddItemRequest.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/request/UpdateQuantityRequest.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/request/TransitionStatusRequest.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/response/CartResponse.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/response/OrderResponse.kt`

- [ ] **Step 1: Create request DTOs**

```kotlin
// dtos/request/AddItemRequest.kt
package com.kunal.poc.kotlin_workspace.dtos.request

data class AddItemRequest(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)
```

```kotlin
// dtos/request/UpdateQuantityRequest.kt
package com.kunal.poc.kotlin_workspace.dtos.request

data class UpdateQuantityRequest(val quantity: Int)
```

```kotlin
// dtos/request/TransitionStatusRequest.kt
package com.kunal.poc.kotlin_workspace.dtos.request

data class TransitionStatusRequest(
    val status: String,         // "Confirmed", "Shipped", "Delivered", "Cancelled"
    val cancelReason: String?,  // required when status == "Cancelled"
)
```

- [ ] **Step 2: Create response DTOs + mapping extensions**

```kotlin
// dtos/response/CartResponse.kt
package com.kunal.poc.kotlin_workspace.dtos.response

import com.kunal.poc.kotlin_workspace.domain.model.Cart

data class CartResponse(
    val id: Long,
    val customerId: Long,
    val items: List<CartItemResponse>,
    val totalAmount: Double,
)

data class CartItemResponse(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun Cart.toResponse() = CartResponse(
    id = id,
    customerId = customerId,
    items = items.map { CartItemResponse(it.productId, it.name, it.quantity, it.unitPrice) },
    totalAmount = totalAmount,
)
```

```kotlin
// dtos/response/OrderResponse.kt
package com.kunal.poc.kotlin_workspace.dtos.response

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

data class OrderResponse(
    val id: Long,
    val customerId: Long,
    val items: List<OrderItemResponse>,
    val status: String,
    val cancelReason: String?,
    val totalAmount: Double,
)

data class OrderItemResponse(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

fun Order.toResponse() = OrderResponse(
    id = id,
    customerId = customerId,
    items = items.map { OrderItemResponse(it.productId, it.name, it.quantity, it.unitPrice) },
    status = status::class.simpleName ?: "Unknown",
    cancelReason = (status as? OrderStatus.Cancelled)?.reason,
    totalAmount = totalAmount,
)
```

- [ ] **Step 3: Build to verify**

```bash
./gradlew compileKotlin
```
Expected: no errors

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/
git commit -m "feat: add REST request and response DTOs with mapping extensions"
```

---

## Task 13: CartController

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/CartController.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/CartControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/CartControllerTest.kt
package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(CartController::class)
class CartControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var cartUseCase: CartUseCase

    private val cart = Cart(id = 1L, customerId = 42L, items = listOf(
        CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    ))

    @Test
    fun `POST create cart returns 201 with cart`() {
        whenever(cartUseCase.createCart(42L)).thenReturn(cart)

        mockMvc.post("/api/v1/carts?customerId=42").andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.customerId") { value(42) }
        }
    }

    @Test
    fun `GET cart returns 200 with cart`() {
        whenever(cartUseCase.getCart(1L)).thenReturn(cart)

        mockMvc.get("/api/v1/carts/1").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.items[0].name") { value("Laptop") }
        }
    }

    @Test
    fun `GET cart returns 404 when not found`() {
        whenever(cartUseCase.getCart(99L)).thenThrow(CartNotFoundException(99L))

        mockMvc.get("/api/v1/carts/99").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `POST add item returns 200 with updated cart`() {
        whenever(cartUseCase.addItem(eq(1L), any())).thenReturn(cart)

        mockMvc.post("/api/v1/carts/1/items") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"productId":10,"name":"Laptop","quantity":1,"unitPrice":999.99}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.items[0].name") { value("Laptop") }
        }
    }

    @Test
    fun `POST checkout returns 201 with order`() {
        val order = cart.checkout()
        whenever(cartUseCase.checkout(1L)).thenReturn(order)

        mockMvc.post("/api/v1/carts/1/checkout").andExpect {
            status { isCreated() }
            jsonPath("$.status") { value("Pending") }
        }
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.inbound.rest.CartControllerTest"
```
Expected: FAIL — `CartController` does not exist yet.

- [ ] **Step 3: Implement CartController**

```kotlin
// adapter/inbound/rest/CartController.kt
package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import com.kunal.poc.kotlin_workspace.dtos.request.AddItemRequest
import com.kunal.poc.kotlin_workspace.dtos.request.UpdateQuantityRequest
import com.kunal.poc.kotlin_workspace.dtos.response.CartResponse
import com.kunal.poc.kotlin_workspace.dtos.response.OrderResponse
import com.kunal.poc.kotlin_workspace.dtos.response.toResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/carts")
class CartController(private val cartUseCase: CartUseCase) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCart(@RequestParam customerId: Long): CartResponse =
        cartUseCase.createCart(customerId).toResponse()

    @GetMapping("/{cartId}")
    fun getCart(@PathVariable cartId: Long): CartResponse =
        cartUseCase.getCart(cartId).toResponse()

    @PostMapping("/{cartId}/items")
    fun addItem(@PathVariable cartId: Long, @RequestBody request: AddItemRequest): CartResponse =
        cartUseCase.addItem(cartId, CartItem(request.productId, request.name, request.quantity, request.unitPrice))
            .toResponse()

    @DeleteMapping("/{cartId}/items/{productId}")
    fun removeItem(@PathVariable cartId: Long, @PathVariable productId: Long): CartResponse =
        cartUseCase.removeItem(cartId, productId).toResponse()

    @PatchMapping("/{cartId}/items/{productId}")
    fun updateQuantity(
        @PathVariable cartId: Long,
        @PathVariable productId: Long,
        @RequestBody request: UpdateQuantityRequest,
    ): CartResponse = cartUseCase.updateQuantity(cartId, productId, request.quantity).toResponse()

    @PostMapping("/{cartId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    fun checkout(@PathVariable cartId: Long): OrderResponse =
        cartUseCase.checkout(cartId).toResponse()
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.inbound.rest.CartControllerTest"
```
Expected: all 5 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/CartController.kt \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/CartControllerTest.kt
git commit -m "feat: add CartController REST adapter"
```

---

## Task 14: GlobalExceptionHandler + Refactor OrderController

**Files:**
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/GlobalExceptionHandler.kt`
- Create: `src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/OrderController.kt`
- Create: `src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/OrderControllerTest.kt`
- Delete: `src/main/kotlin/com/kunal/poc/kotlin_workspace/controllers/OrderController.kt`
- Delete: `src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/Order.kt`

- [ ] **Step 1: Create GlobalExceptionHandler**

```kotlin
// adapter/inbound/rest/GlobalExceptionHandler.kt
package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.CartNotFoundException::class, DomainException.OrderNotFoundException::class)
    fun handleNotFound(ex: DomainException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(DomainException.EmptyCartException::class)
    fun handleBadRequest(ex: DomainException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(DomainException.InvalidStatusTransitionException::class)
    fun handleConflict(ex: DomainException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflict")
}
```

- [ ] **Step 2: Write failing tests for OrderController**

```kotlin
// src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/OrderControllerTest.kt
package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var orderUseCase: OrderUseCase

    private val order = Order(
        id = 1L, customerId = 42L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @Test
    fun `GET order returns 200 with order`() {
        whenever(orderUseCase.getOrder(1L)).thenReturn(order)

        mockMvc.get("/api/v1/orders/1").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.status") { value("Pending") }
        }
    }

    @Test
    fun `GET order returns 404 when not found`() {
        whenever(orderUseCase.getOrder(99L)).thenThrow(OrderNotFoundException(99L))

        mockMvc.get("/api/v1/orders/99").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `GET orders by customer returns list`() {
        whenever(orderUseCase.listOrders(42L)).thenReturn(listOf(order))

        mockMvc.get("/api/v1/orders?customerId=42").andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value(1) }
        }
    }

    @Test
    fun `PATCH transition updates order status`() {
        val confirmed = order.copy(status = OrderStatus.Confirmed)
        whenever(orderUseCase.transition(1L, OrderStatus.Confirmed)).thenReturn(confirmed)

        mockMvc.patch("/api/v1/orders/1/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"Confirmed"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("Confirmed") }
        }
    }
}
```

- [ ] **Step 3: Run tests — verify they fail**

```bash
./gradlew test --tests "com.kunal.poc.kotlin_workspace.adapter.inbound.rest.OrderControllerTest"
```
Expected: FAIL — new `OrderController` in adapter layer does not exist.

- [ ] **Step 4: Create new OrderController in adapter layer**

```kotlin
// adapter/inbound/rest/OrderController.kt
package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import com.kunal.poc.kotlin_workspace.dtos.request.TransitionStatusRequest
import com.kunal.poc.kotlin_workspace.dtos.response.OrderResponse
import com.kunal.poc.kotlin_workspace.dtos.response.toResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderUseCase: OrderUseCase) {

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): OrderResponse =
        orderUseCase.getOrder(orderId).toResponse()

    @GetMapping
    fun listOrders(@RequestParam customerId: Long): List<OrderResponse> =
        orderUseCase.listOrders(customerId).map { it.toResponse() }

    @PatchMapping("/{orderId}/status")
    fun transition(
        @PathVariable orderId: Long,
        @RequestBody request: TransitionStatusRequest,
    ): OrderResponse {
        val status = when (request.status) {
            "Confirmed" -> OrderStatus.Confirmed
            "Shipped"   -> OrderStatus.Shipped
            "Delivered" -> OrderStatus.Delivered
            "Cancelled" -> OrderStatus.Cancelled(request.cancelReason ?: "")
            else        -> throw IllegalArgumentException("Unknown status: ${request.status}")
        }
        return orderUseCase.transition(orderId, status).toResponse()
    }
}
```

- [ ] **Step 5: Delete old files**

```bash
rm src/main/kotlin/com/kunal/poc/kotlin_workspace/controllers/OrderController.kt
rm src/main/kotlin/com/kunal/poc/kotlin_workspace/dtos/Order.kt
```

- [ ] **Step 6: Update fixtures to use domain types**

```kotlin
// fixtures/OrderFixtures.kt
package com.kunal.poc.kotlin_workspace.fixtures

import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus

object OrderFixtures {
    val orders: List<Order> = listOf(
        Order(
            id = 1L, customerId = 101L,
            items = listOf(
                OrderItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99),
                OrderItem(productId = 11L, name = "Mouse", quantity = 2, unitPrice = 29.99),
            ),
            status = OrderStatus.Delivered,
            totalAmount = 1059.97,
        ),
        Order(
            id = 2L, customerId = 102L,
            items = listOf(OrderItem(productId = 20L, name = "Desk Chair", quantity = 1, unitPrice = 249.99)),
            status = OrderStatus.Shipped,
            totalAmount = 249.99,
        ),
        Order(
            id = 3L, customerId = 103L,
            items = listOf(
                OrderItem(productId = 30L, name = "Monitor", quantity = 2, unitPrice = 399.99),
                OrderItem(productId = 31L, name = "HDMI Cable", quantity = 1, unitPrice = 14.99),
            ),
            status = OrderStatus.Pending,
            totalAmount = 814.97,
        ),
    )
}
```

- [ ] **Step 7: Run all tests**

```bash
./gradlew test
```
Expected: all tests PASS, `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add src/main/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/ \
        src/test/kotlin/com/kunal/poc/kotlin_workspace/adapter/inbound/rest/ \
        src/main/kotlin/com/kunal/poc/kotlin_workspace/fixtures/OrderFixtures.kt
git commit -m "feat: add OrderController, GlobalExceptionHandler; remove old controllers and dtos"
```

---

## Final Verification

- [ ] **Run full test suite**

```bash
./gradlew test
```
Expected: all tests PASS

- [ ] **Start the application and smoke-test the API**

```bash
./gradlew bootRun &
# Create a cart
curl -s -X POST "http://localhost:8080/api/v1/carts?customerId=1" | jq .
# Add an item
curl -s -X POST "http://localhost:8080/api/v1/carts/1/items" \
  -H "Content-Type: application/json" \
  -d '{"productId":10,"name":"Laptop","quantity":1,"unitPrice":999.99}' | jq .
# Checkout
curl -s -X POST "http://localhost:8080/api/v1/carts/1/checkout" | jq .
```
