# ecomm-basics

A learning project for building an e-commerce backend in Kotlin using **hexagonal architecture** and **TDD**.

## Stack

- **Kotlin** + Spring Boot 4
- **Spring Data JPA** + H2 (in-memory)
- **JUnit5** + kotlin-test

## Architecture

Hexagonal (Ports & Adapters). The domain layer has zero Spring or JPA imports.

```
src/main/kotlin/.../
├── domain/
│   ├── model/          # Cart, Order, OrderStatus, CartItem, OrderItem
│   ├── exception/      # DomainException and subtypes
│   ├── port/
│   │   ├── inbound/    # CartUseCase, OrderUseCase
│   │   └── outbound/   # CartRepository, OrderRepository
│   └── service/        # CartService, OrderService
├── adapter/
│   ├── inbound/rest/   # CartController, OrderController, GlobalExceptionHandler
│   └── outbound/
│       └── persistence/ # JPA entities + adapters
├── config/             # DomainConfig — wires domain services as Spring beans
└── dtos/               # REST request/response models
```

## API

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/carts?customerId={id}` | Create cart |
| `GET` | `/api/v1/carts/{cartId}` | Get cart |
| `POST` | `/api/v1/carts/{cartId}/items` | Add item |
| `DELETE` | `/api/v1/carts/{cartId}/items/{productId}` | Remove item |
| `PATCH` | `/api/v1/carts/{cartId}/items/{productId}` | Update quantity |
| `POST` | `/api/v1/carts/{cartId}/checkout` | Checkout → creates Order |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/orders/{orderId}` | Get order |
| `GET` | `/api/v1/orders?customerId={id}` | List orders for customer |
| `PATCH` | `/api/v1/orders/{orderId}/status` | Transition order status |

### Order Status Transitions
```
Pending → Confirmed → Shipped → Delivered
   ↓           ↓          ↓
Cancelled   Cancelled  Cancelled
```

## Running

```bash
./gradlew bootRun
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:ecommdb`).

## Testing

```bash
./gradlew test
```
