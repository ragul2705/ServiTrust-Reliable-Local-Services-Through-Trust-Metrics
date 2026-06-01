package com.servitrust.app.model

data class ServiceRequestCreateRequest(
    val iserID: Long,
    val providerId: Long,
    val serviceType: String,
    val status: String
)
