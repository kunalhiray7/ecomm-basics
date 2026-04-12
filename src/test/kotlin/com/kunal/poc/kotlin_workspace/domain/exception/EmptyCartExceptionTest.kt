package com.kunal.poc.kotlin_workspace.domain.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EmptyCartExceptionTest {

    @Test
    fun `should throw exception with correct message`() {
        val e = assertThrows(EmptyCartException::class.java) {
            throw EmptyCartException("Cart is empty")
        }

        assertEquals("Cart is empty", e.message)
    }
}