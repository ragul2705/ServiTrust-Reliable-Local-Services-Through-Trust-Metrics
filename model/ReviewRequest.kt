package com.servitrust.app.model

data class ReviewRequest (
    val rating: Int,
    val feedback: String? = null
)
