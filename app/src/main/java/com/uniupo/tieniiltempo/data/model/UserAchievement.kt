// app/src/main/java/com/uniupo/tieniiltempo/data/model/UserAchievement.kt
package com.uniupo.tieniiltempo.data.model

data class UserAchievement(
    val userId: String = "",
    val points: Int = 0,
    val badges: List<String> = emptyList(),
    val completedActivities: Int = 0,
    val completedOnTime: Int = 0
)