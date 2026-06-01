package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("role")
    val role: String,

    // ✅ this MUST match backend response key: "providerId"
    @SerializedName("providerId")
    val providerId: Long
)