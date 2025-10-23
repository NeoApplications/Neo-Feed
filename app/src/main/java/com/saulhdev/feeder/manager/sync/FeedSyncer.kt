package com.saulhdev.feeder.manager.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.ID_UNSET
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

class FeedSyncer(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(context, notificationManager)
    }

    override suspend fun doWork(): Result {
        var success: Boolean

        try {
            val feedId = inputData.getLong("feed_id", ID_UNSET)
            val feedTag = inputData.getString("feed_tag") ?: ""
            val forceNetwork = inputData.getBoolean("force_network", false)
            val minFeedAgeMinutes = inputData.getInt("min_feed_age_minutes", 5)

            success = syncFeeds(
                context = context,
                feedId = feedId,
                feedTag = feedTag,
                forceNetwork = forceNetwork,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        } catch (e: Exception) {
            success = false
            Log.e("FeederFeedSyncer", "Failure during sync", e)
        }

        return when (success) {
            true  -> Result.success()
            false -> Result.failure()
        }
    }
}

private const val syncNotificationId = 42624
private const val syncChannelId = "feederSyncNotifications"
private const val syncNotificationGroup = "com.saulhdev.neofeed.SYNC"

private fun createNotificationChannel(
    context: Context,
    notificationManager: NotificationManagerCompat
) {
    val name = context.getString(R.string.sync_status)
    val description = context.getString(R.string.sync_status)

    val channel =
        NotificationChannel(syncChannelId, name, NotificationManager.IMPORTANCE_LOW)
    channel.description = description

    notificationManager.createNotificationChannel(channel)
}

fun createForegroundInfo(
    context: Context,
    notificationManager: NotificationManagerCompat
): ForegroundInfo {
    createNotificationChannel(context, notificationManager)

    val syncingText = context.getString(R.string.syncing)

    val notification =
        NotificationCompat.Builder(context.applicationContext, syncChannelId)
            .setContentTitle(syncingText)
            .setTicker(syncingText)
            .setGroup(syncNotificationGroup)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

    return ForegroundInfo(
        syncNotificationId,
        notification,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        else 0
    )
}

// TODO fix manually running sync
fun requestFeedSync(
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
) {
    val workManager: WorkManager by inject(WorkManager::class.java)
    val data = workDataOf(
        "feed_id" to feedId,
        "feed_tag" to feedTag,
        "force_network" to forceNetwork,
    )

    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()
        .addTag("FeedSyncer")
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .keepResultsForAtLeast(1, TimeUnit.MINUTES)
        .setInputData(data)
        .build()

    workManager.enqueueUniqueWork(
        "feeder_sync_onetime_$feedId",
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}