package com.servitrust.app.model

data class ProviderRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val serviceType: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)