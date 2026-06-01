package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,

    val verified: Boolean? = false,

    val location: String? = null,

    @SerializedName(value = "latitude", alternate = ["lat"])
    val latitude: Double? = null,

    @SerializedName(value = "longitude", alternate = ["lng"])
    val longitude: Double? = null
)