package com.uniupo.tieniiltempo.data.model

import java.util.Date

data class ChatPreview(
    val activityId: String,
    val activityTitle: String,
    val userId: String,
    val userName: String,
    val userImage: String? = null,
    val lastMessage: String,
    val lastMessageTime: Date? = null,
    val hasUnreadMessages: Boolean = false
)