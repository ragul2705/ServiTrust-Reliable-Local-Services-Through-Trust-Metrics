package com.servitrust.app.model

data class ApiResponse<T>(
    val requestId: String?,
    val message: String?,
    val path: String?,
    val timestamp: Long?,
    val data: T?
)