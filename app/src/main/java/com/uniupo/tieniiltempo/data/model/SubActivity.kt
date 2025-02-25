package com.uniupo.tieniiltempo.data.model

data class SubActivity(
    val id: String = "",
    val activityId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val order: Int = 0,
    val status: String = "pending",
    val maxTime: Long? = null,
    val actualTime: Long? = null,
    val isParallel: Boolean = false,
    val requireLocation: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userComment: String? = null,
    val userImage: String? = null
)