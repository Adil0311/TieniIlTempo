package com.uniupo.tieniiltempo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.MainActivity
import com.uniupo.tieniiltempo.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Gestisci diversi tipi di notifiche
        val type = remoteMessage.data["type"] ?: return
        val title = remoteMessage.data["title"] ?: "Notifica"
        val message = remoteMessage.data["message"] ?: "Hai una nuova notifica"

        when (type) {
            "new_activity" -> {
                val activityId = remoteMessage.data["activityId"] ?: return
                sendActivityNotification(title, message, activityId)
            }
            "new_message" -> {
                val activityId = remoteMessage.data["activityId"] ?: return
                sendChatNotification(title, message, activityId)
            }
            "activity_timeout" -> {
                val activityId = remoteMessage.data["activityId"] ?: return
                sendTimeoutNotification(title, message, activityId)
            }
            else -> sendDefaultNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Invia il nuovo token al server
        updateUserFcmToken(token)
    }

    private fun updateUserFcmToken(token: String) {
        val currentUser = userRepository.getCurrentUser() ?: return
        userRepository.updateUserFcmToken(currentUser.uid, token)
    }

    private fun sendActivityNotification(title: String, message: String, activityId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NOTIFICATION_TYPE", "new_activity")
            putExtra("ACTIVITY_ID", activityId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        sendNotification(title, message, intent, CHANNEL_NEW_ACTIVITY)
    }

    private fun sendChatNotification(title: String, message: String, activityId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NOTIFICATION_TYPE", "new_message")
            putExtra("ACTIVITY_ID", activityId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        sendNotification(title, message, intent, CHANNEL_MESSAGES)
    }

    private fun sendTimeoutNotification(title: String, message: String, activityId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NOTIFICATION_TYPE", "activity_timeout")
            putExtra("ACTIVITY_ID", activityId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        sendNotification(title, message, intent, CHANNEL_TIMEOUTS)
    }

    private fun sendDefaultNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        sendNotification(title, message, intent, CHANNEL_DEFAULT)
    }

    private fun sendNotification(
        title: String,
        message: String,
        intent: Intent,
        channelId: String
    ) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)

        // Controlla il permesso per le notifiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = notificationManager.areNotificationsEnabled()
            if (!permissionGranted) {
                // In una situazione reale potresti voler richiedere il permesso
                return
            }
        }

        notificationManager.notify(getNotificationId(), notificationBuilder.build())
    }

    private fun getNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }

    companion object {
        const val CHANNEL_DEFAULT = "default_channel"
        const val CHANNEL_MESSAGES = "messages_channel"
        const val CHANNEL_NEW_ACTIVITY = "new_activity_channel"
        const val CHANNEL_TIMEOUTS = "timeouts_channel"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val defaultChannel = NotificationChannel(
                    CHANNEL_DEFAULT,
                    "Notifiche generali",
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                val messagesChannel = NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messaggi",
                    NotificationManager.IMPORTANCE_HIGH
                )

                val activitiesChannel = NotificationChannel(
                    CHANNEL_NEW_ACTIVITY,
                    "Nuove attività",
                    NotificationManager.IMPORTANCE_HIGH
                )

                val timeoutsChannel = NotificationChannel(
                    CHANNEL_TIMEOUTS,
                    "Scadenze",
                    NotificationManager.IMPORTANCE_HIGH
                )

                notificationManager.createNotificationChannels(
                    listOf(defaultChannel, messagesChannel, activitiesChannel, timeoutsChannel)
                )
            }
        }
    }
}