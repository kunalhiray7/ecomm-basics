package com.kunal.poc.kotlin_workspace.adapter.inbound.rest

import com.kunal.poc.kotlin_workspace.domain.exception.CartNotFoundException
import com.kunal.poc.kotlin_workspace.domain.exception.EmptyCartException
import com.kunal.poc.kotlin_workspace.domain.exception.InvalidStatusTransitionException
import com.kunal.poc.kotlin_workspace.domain.exception.OrderNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CartNotFoundException::class, OrderNotFoundException::class)
    fun handleNotFound(ex: RuntimeException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(EmptyCartException::class)
    fun handleBadRequest(ex: RuntimeException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleConflict(ex: RuntimeException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflict")
}
