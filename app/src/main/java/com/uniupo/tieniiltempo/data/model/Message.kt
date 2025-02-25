package com.uniupo.tieniiltempo.data.model

import java.util.Date

data class Message(
    val id: String = "",
    val activityId: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)