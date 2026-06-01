package com.servitrust.app.model

data class ComplaintRequest(
    val requestId: Long,
    val providerId: Long,
    val userId: Long,
    val reason: String,
    val details: String? = null
)