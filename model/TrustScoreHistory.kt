package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class TrustScoreHistory(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("providerId")
    val providerId: Long,

    @SerializedName("delta")
    val delta: Double? = 0.0,

    @SerializedName("reason")
    val reason: String? = "",

    @SerializedName("scoreAfter")
    val scoreAfter: Double? = 0.0,

    @SerializedName("createdAt")
    val createdAt: String? = ""
)