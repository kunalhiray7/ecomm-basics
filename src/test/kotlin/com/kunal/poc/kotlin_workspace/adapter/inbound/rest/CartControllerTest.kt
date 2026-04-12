package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.model.Cart
import com.kunal.poc.kotlin_workspace.domain.model.CartItem
import com.kunal.poc.kotlin_workspace.domain.port.inbound.CartUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class CartControllerTest {

    @Mock lateinit var cartUseCase: CartUseCase
    private lateinit var mockMvc: MockMvc

    private val cart = Cart(id = 1L, customerId = 42L, items = listOf(
        CartItem(productId = 10L, name = "Laptop", quantity = 1, unitPrice = 999.99)
    ))

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(CartController(cartUseCase))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

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
