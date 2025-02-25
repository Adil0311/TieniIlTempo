package com.uniupo.tieniiltempo.data.model

data class Activity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val caregiverId: String = "",
    val userId: String = "",
    val createdAt: Date = Date(),
    val status: String = "pending",
    val maxTime: Long? = null,
    val feedbackRating: Int? = null,
    val feedbackComment: String? = null
)