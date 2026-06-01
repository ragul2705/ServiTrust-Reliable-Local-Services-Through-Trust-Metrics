package com.servitrust.app.model

data class ServiceRequestResponse(
    val id: Long,
    val userId: Long,
    val providerId: Long,
    val serviceType: String,
    val status: String
)
