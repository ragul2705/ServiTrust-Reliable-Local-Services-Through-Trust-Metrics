package com.servitrust.app.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)