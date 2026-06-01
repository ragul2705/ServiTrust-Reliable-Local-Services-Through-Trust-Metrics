package com.servitrust.app.model

import com.google.gson.annotations.SerializedName

data class TrustScoreResponse(
    @SerializedName("providerId")
    val providerId: Long,

    @SerializedName("trustScore")
    val trustScore: Double,

    @SerializedName("level")
    val level: String,

    @SerializedName("completionRate")
    val completionRate: Double,

    @SerializedName("averageRating")
    val averageRating: Double,

    @SerializedName("completedJobs")
    val completedJobs: Int,

    @SerializedName("totalRequests")
    val totalRequests: Int,

    @SerializedName("availabilityScore")
    val availabilityScore: Double,

    @SerializedName("penalty")
    val penalty: Double,

    @SerializedName("breakdown")
    val breakdown: Map<String, Double> = emptyMap()
)