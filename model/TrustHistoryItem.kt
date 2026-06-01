package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class TrustHistoryItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("providerId")
    val providerId: Long,

    @SerializedName("delta")
    val delta: Double,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("scoreAfter")
    val scoreAfter: Double,

    @SerializedName("createdAt")
    val createdAt: String
)