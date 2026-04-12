package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Order
import com.kunal.poc.kotlin_workspace.domain.model.OrderItem
import com.kunal.poc.kotlin_workspace.domain.model.OrderStatus
import com.kunal.poc.kotlin_workspace.domain.port.inbound.OrderUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class OrderControllerTest {

    @Mock lateinit var orderUseCase: OrderUseCase
    private lateinit var mockMvc: MockMvc

    private val order = Order(
        id = 1L, customerId = 42L,
        items = listOf(OrderItem(productId = 1L, name = "Laptop", quantity = 1, unitPrice = 999.99)),
        totalAmount = 999.99,
    )

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(OrderController(orderUseCase))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

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
