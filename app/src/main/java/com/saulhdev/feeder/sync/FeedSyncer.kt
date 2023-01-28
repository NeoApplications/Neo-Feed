package com.saulhdev.feeder.sync

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.saulhdev.feeder.db.ID_UNSET
import org.kodein.di.DI
import org.kodein.di.instance
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
            true -> Result.success()
            false -> Result.failure()
        }
    }
}

private const val syncNotificationId = 42623
private const val syncChannelId = "feederSyncNotifications"
private const val syncNotificationGroup = "com.saulhdev.neofeed.SYNC"

@TargetApi(Build.VERSION_CODES.O)
@RequiresApi(Build.VERSION_CODES.O)
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context, notificationManager)
    }

    val syncingText = context.getString(R.string.syncing)

    val notification =
        NotificationCompat.Builder(context.applicationContext, syncChannelId)
            .setContentTitle(syncingText)
            .setTicker(syncingText)
            .setGroup(syncNotificationGroup)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            syncNotificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
        )
    } else {
        ForegroundInfo(syncNotificationId, notification)
    }
}

fun requestFeedSync(
    di: DI,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
) {
    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()
        .addTag("feeder")
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .keepResultsForAtLeast(5, TimeUnit.MINUTES)

    val data = workDataOf(
        "feed_id" to feedId,
        "feed_tag" to feedTag,
        "force_network" to forceNetwork,
    )

    workRequest.setInputData(data)
    val workManager by di.instance<WorkManager>()
    workManager.enqueueUniqueWork(
        "feeder_sync_onetime",
        ExistingWorkPolicy.KEEP,
        workRequest.build()
    )
}