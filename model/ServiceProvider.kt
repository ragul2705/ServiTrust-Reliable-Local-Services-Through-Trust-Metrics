package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class ServiceProvider(
    val id: Long? = null,
    val name: String,

    @SerializedName(value = "serviceType", alternate = ["service_type"])
    val serviceType: String,

    val location: String,

    @SerializedName(value = "availabilityStatus", alternate = ["availability_status"])
    val availabilityStatus: String? = null,

    val rating: Double? = 0.0,

    @SerializedName(value = "completedJobs", alternate = ["completed_jobs"])
    val completedJobs: Int? = 0,

    @SerializedName(value = "acceptedCount", alternate = ["accepted_count"])
    val acceptedCount: Int? = 0,

    @SerializedName(value = "cancelledCount", alternate = ["cancelled_count"])
    val cancelledCount: Int? = 0,

    @SerializedName(value = "trustScore", alternate = ["trust_score"])
    val trustScore: Double? = 0.0,

    @SerializedName(value = "latitude", alternate = ["lat"])
    val latitude: Double? = null,

    @SerializedName(value = "longitude", alternate = ["lng"])
    val longitude: Double? = null,

    val verified: Boolean? = false
)