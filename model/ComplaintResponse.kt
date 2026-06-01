package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class ComplaintResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("requestId") val requestId: Long,
    @SerializedName("providerId") val providerId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("details") val details: String?,
    @SerializedName("createdAt") val createdAt: String?
)