package com.kunal.poc.kotlin_workspace.dtos.request

data class TransitionStatusRequest(
    val status: String,
    val cancelReason: String? = null,
)
