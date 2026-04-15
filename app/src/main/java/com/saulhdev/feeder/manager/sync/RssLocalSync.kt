/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.manager.sync

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.db.ID_ALL
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.entity.JsonFeed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.models.FeedParser
import com.saulhdev.feeder.manager.models.getResponse
import com.saulhdev.feeder.manager.models.scheduleFullTextParse
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.getSyncDays
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import kotlin.time.Clock
import kotlin.time.Instant

val syncMutex = Mutex()
val prefs: FeedPreferences by inject(FeedPreferences::class.java)
val singleThreadedSync = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
const val TAG = "RssLocalSync"

suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    return syncMutex.withLock {
        withContext(singleThreadedSync) {
            syncFeeds(
                context = context,
                feedId = feedId,
                feedTag = feedTag,
                maxFeedItemCount = prefs.itemsPerFeed.getValue().toInt(),
                forceNetwork = forceNetwork,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        }
    }
}

internal suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    maxFeedItemCount: Int = 100,
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    var result = false
    val feedsRepo: SourcesRepository by inject(SourcesRepository::class.java)
    val articlesRepo: ArticleRepository by inject(ArticleRepository::class.java)
    val downloadTime = Clock.System.now()
    var needFullTextSync = false
    val time = measureTimeMillis {
        try {
            supervisorScope {
                val sRepository: SourcesRepository by inject(SourcesRepository::class.java)
                val staleTime: Long = if (forceNetwork) {
                    Clock.System.now().toEpochMilliseconds()
                } else {
                    Clock.System.now().minus(minFeedAgeMinutes.toLong(), DateTimeUnit.MINUTE)
                        .toEpochMilliseconds()
                }

                val coroutineContext =
                    Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
                        Log.e(TAG, "Error during sync", throwable)
                    }

                val feedsToFetch = feedsToSync(
                    repository = sRepository,
                    feedId = feedId,
                    tag = feedTag,
                    staleTime = staleTime,
                    forceNetwork = forceNetwork
                )

                Log.d(TAG, "Feeds to sync: ${feedsToFetch.size}")

                val jobs = feedsToFetch.map { feed ->
                    needFullTextSync = needFullTextSync || feed.fullTextByDefault
                    launch(coroutineContext) {
                        try {
                            // Mark as syncing START
                            feedsRepo.setCurrentlySyncingOn(feedId = feed.id, syncing = true)

                            syncFeed(
                                feedsRepo = feedsRepo,
                                articleRepo = articlesRepo,
                                feedSql = feed,
                                filesDir = context.filesDir,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork,
                                downloadTime = downloadTime
                            )

                            // Successful sync, update lastSync
                            feedsRepo.setCurrentlySyncingOn(
                                feedId = feed.id,
                                syncing = false,
                                lastSync = Clock.System.now(),
                            )
                        } catch (e: Throwable) {
                            Log.e(TAG, "Failed to sync ${feed.title}: ${feed.url}", e)
                            // Error, clear syncing flag but don't update lastSync
                            feedsRepo.setCurrentlySyncingOn(feedId = feed.id, syncing = false)
                        }
                    }
                }

                jobs.joinAll()
                result = feedsToFetch.isNotEmpty()

            }
        } catch (e: Throwable) {
            Log.e(TAG, "Outer error", e)
        } finally {
            if (needFullTextSync) {
                scheduleFullTextParse()
            }
        }
    }
    Log.d(TAG, "Completed in $time ms")
    return result
}

private suspend fun syncFeed(
    feedsRepo: SourcesRepository,
    articleRepo: ArticleRepository,
    feedSql: Feed,
    filesDir: File,
    maxFeedItemCount: Int,
    forceNetwork: Boolean = false,
    downloadTime: Instant
) {
    Log.d(TAG, "Fetching ${feedSql.title}")

    val okHttpClient = OkHttpClient.Builder()
        .build()
    val response: Response =
        okHttpClient.getResponse(url = feedSql.url, forceNetwork = forceNetwork)
    val feedParser = FeedParser()
    val feed: JsonFeed = response.use {
        response.body.let { responseBody ->
            when {
                !response.isSuccessful -> {
                    throw ResponseFailure("${response.code} when fetching ${feedSql.title}: ${feedSql.url}")
                }

                else                   -> {
                    Log.d(TAG, "Fetching correct ${feedSql.title}")
                    feedParser.parseFeedResponse(
                        url = response.request.url.toUrl(),
                        responseBody = responseBody
                    )
                }
            }
        }
    }.let {
        when {
            it.icon?.startsWith("data") == true -> it.copy(icon = null)
            else                                -> it
        }
    }

    val syncedFeed = feedSql.copy(lastSync = Clock.System.now())
    val items = feed.items
    Log.d(TAG, "Parsed ${items?.size ?: 0} items for ${feedSql.title}")
    val days = getSyncDays(prefs)
    val minKeptPubDate = Clock.System.now().minus(
        period = DateTimePeriod(days = days),
        timeZone = TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
    val articles =
        items?.reversed()
            ?.map { item ->
                val itemGuid = (item.id ?: item.url).toString()
                val article = (articleRepo.getArticleByGuid(
                    guid = itemGuid,
                    feedId = syncedFeed.id
                ) ?: Article(firstSyncedTime = downloadTime))
                    .updateFromParsedEntry(item, itemGuid, feed, syncedFeed.id)
                article to (item.content_html ?: item.content_text ?: "")
            }
            ?.filter { (article, _) ->
                article.pubDate !in 1..<minKeptPubDate
            } ?: emptyList()

    Log.d(TAG, "Prepared ${articles.size} articles for ${feedSql.title}")

    feedsRepo.updateSource(
        syncedFeed.copy(
            title = syncedFeed.title,
            feedImage = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) }
                ?: syncedFeed.feedImage
        ))

    articleRepo.updateOrInsertArticle(articles) { article, text ->
        withContext(Dispatchers.IO) {
            blobOutputStream(article.uuid, filesDir).bufferedWriter().use {
                it.write(text)
            }
        }
    }

    val ids = articleRepo.getItemsToBeCleanedFromFeed(
        feedId = syncedFeed.id,
        minKeptPubDate = minKeptPubDate
    )
    Log.d(
        TAG,
        "Cleanup ${feedSql.title}: days=$days cutoff=$minKeptPubDate deleting=${ids.size}"
    )

    for (id in ids) {
        val file = blobFile(itemId = id, filesDir = filesDir)
        try {
            if (file.isFile) {
                file.delete()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to delete $file", e)
        }
    }

    articleRepo.deleteArticles(ids)
}

class ResponseFailure(message: String?) : Exception(message)

internal suspend fun feedsToSync(
    repository: SourcesRepository,
    feedId: Long,
    tag: String,
    staleTime: Long = -1L,
    forceNetwork: Boolean = false,
): List<Feed> {

    val sources = when {
        feedId > 0 -> {
            if (forceNetwork) {
                repository.loadFeedById(feedId)?.let { listOf(it) } ?: emptyList()
            } else {
                repository.loadFeedIfStale(feedId = feedId, staleTime = staleTime)
            }
        }

        feedId == ID_ALL -> {
            Log.d(TAG, "Checking all feeds  = $forceNetwork")
            if (forceNetwork) {
                repository.getAllSources()

            } else {
                repository.loadFeedIfStale(ID_ALL, staleTime)
            }
        }

        tag.isNotEmpty() -> {
            repository.loadFeedsByTag(tag)
        }

        else -> repository.getAllSources()
    }

    return if (tag.isNotEmpty() && feedId == ID_ALL) {
        Log.d(TAG, "Filtering by tag: $tag")
        sources.filter { it.tag.contains(tag) }
    } else {
        Log.d(TAG, "No tag filtering applied $sources")
        sources
    }
}
