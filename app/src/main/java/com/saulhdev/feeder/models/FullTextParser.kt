package com.saulhdev.feeder.models

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.models.FeedItemForFetching
import com.saulhdev.feeder.utils.blobFullFile
import com.saulhdev.feeder.utils.blobFullOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

fun scheduleFullTextParse() {
    Log.i("FeederFullText", "Scheduling a full text parse work")
    val workRequest = OneTimeWorkRequestBuilder<FullTextWorker>()
        .addTag("FullTextWorker")
        .keepResultsForAtLeast(1, TimeUnit.MINUTES)
    val workManager: WorkManager by inject(WorkManager::class.java)
    workManager.enqueueUniqueWork(
        "FullTextWorker",
        ExistingWorkPolicy.KEEP,
        workRequest.build()
    )
}

class FullTextWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    val repository: ArticleRepository by inject(ArticleRepository::class.java)

    override suspend fun doWork(): Result {
        Log.i("FeederFullText", "Parsing full texts for articles if missing")
        val itemsToSync: List<FeedItemForFetching> =
            repository.getFeedsItemsWithDefaultFullTextParse()
                .firstOrNull()
                ?: return Result.success()

        val success: Boolean = itemsToSync
            .map { feedItem ->
                parseFullArticleIfMissing(
                    feedItem = feedItem,
                    okHttpClient = okHttpClient,
                    filesDir = context.filesDir
                )
            }
            .fold(true) { acc, value ->
                acc && value
            }

        return when (success) {
            true -> Result.success()
            false -> Result.failure()
        }
    }
}

suspend fun parseFullArticleIfMissing(
    feedItem: FeedItemForFetching,
    okHttpClient: OkHttpClient,
    filesDir: File
): Boolean {
    val fullArticleFile = blobFullFile(itemId = feedItem.id, filesDir = filesDir)
    return fullArticleFile.isFile || parseFullArticle(
        feedItem = feedItem,
        okHttpClient = okHttpClient,
        filesDir = filesDir
    ).first
}

suspend fun parseFullArticle(
    feedItem: FeedItemForFetching,
    okHttpClient: OkHttpClient,
    filesDir: File
): Pair<Boolean, Throwable?> = withContext(Dispatchers.Default) {
    return@withContext try {
        val url = feedItem.link ?: return@withContext false to null
        Log.d("FeederFullText", "Fetching full page ${feedItem.link}")
        val html: String = okHttpClient.curl(URL(url)) ?: return@withContext false to null

        // TODO verify encoding is respected in reader
        Log.i("FeederFullText", "Parsing article ${feedItem.link}")
        val article = Readability4JExtended(url, html).parse()

        // TODO set image on item if none already
        // naiveFindImageLink(article.content)?.let { Parser.unescapeEntities(it, true) }

        Log.d("FeederFullText", "Writing article ${feedItem.link}")
        withContext(Dispatchers.IO) {
            blobFullOutputStream(feedItem.id, filesDir).bufferedWriter().use { writer ->
                writer.write(article.contentWithUtf8Encoding)
            }
        }
        true to null
    } catch (e: Throwable) {
        Log.e(
            "FeederFullText",
            "Failed to get fulltext for ${feedItem.link}: ${e.message}",
            e
        )
        false to e
    }
}
