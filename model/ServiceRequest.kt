package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class ServiceRequest(
    val id: Long? = null,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("providerId")
    val providerId: Long,

    @SerializedName(value = "userName", alternate = ["user_name"])
    val userName: String = "",

    @SerializedName(value = "serviceType", alternate = ["service_type"])
    val serviceType: String,

    @SerializedName("status")
    val status: String, 

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("rating")
    val rating: Int? = null,

    @SerializedName("feedback")
    val feedback: String? = null,

    @SerializedName("reviewed")
    val reviewed: Boolean = false,

    @SerializedName("complaintRaised")
    val complaintRaised: Boolean = false
)