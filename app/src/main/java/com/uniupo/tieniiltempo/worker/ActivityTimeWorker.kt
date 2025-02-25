package com.uniupo.tieniiltempo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityTimerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val activityId = inputData.getString("ACTIVITY_ID") ?: return@withContext Result.failure()
            val subActivityId = inputData.getString("SUB_ACTIVITY_ID") ?: return@withContext Result.failure()

            // Invia notifica di timeout
            notificationRepository.sendTimeoutNotification(activityId, subActivityId)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}