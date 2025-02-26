// Modifica il file app/src/main/java/com/uniupo/tieniiltempo/worker/ActivityTimerWorker.kt

package com.uniupo.tieniiltempo.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.NotificationRepository
import com.uniupo.tieniiltempo.ui.user.UserActivityDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityTimerWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    companion object {
        const val CHANNEL_ID = "activity_timer_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val activityId = inputData.getString("ACTIVITY_ID") ?: return@withContext Result.failure()
            val subActivityId = inputData.getString("SUB_ACTIVITY_ID") ?: return@withContext Result.failure()

            // Ottieni i dettagli della sotto-attività
            val subActivity = activityRepository.getSubActivityById(subActivityId)
            val activity = activityRepository.getActivityById(activityId)

            if (subActivity != null && activity != null) {
                // Invia notifica di timeout tramite repository
                notificationRepository.sendTimeoutNotification(activityId, subActivityId)

                // Crea anche una notifica locale
                showTimeoutNotification(activityId, activity.title, subActivity.title)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showTimeoutNotification(activityId: String, activityTitle: String, subActivityTitle: String) {
        createNotificationChannel()

        val intent = Intent(context, UserActivityDetailActivity::class.java).apply {
            putExtra("ACTIVITY_ID", activityId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tempo scaduto!")
            .setContentText("Tempo superato per: $subActivityTitle in $activityTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timeout attività"
            val descriptionText = "Notifiche per il superamento del tempo massimo delle attività"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}