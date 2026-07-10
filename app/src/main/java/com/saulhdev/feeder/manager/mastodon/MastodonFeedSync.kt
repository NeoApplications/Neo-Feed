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

package com.saulhdev.feeder.manager.mastodon

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.manager.sync.ResponseFailure
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.getSyncDays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant

private const val TAG = "MastodonFeedSync"

object MastodonFeedSync {
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)
    private val mastodonStorage: MastodonStorage by inject(MastodonStorage::class.java)
    private val mastodonApi: MastodonApi by inject(MastodonApi::class.java)

    suspend fun sync(
        context: Context,
        articleRepo: ArticleRepository,
        feedSql: Feed,
        filesDir: File,
        downloadTime: Instant,
    ) {
        val (instance, account) = feedSql.url.toMastodonInstanceAndAccount()
            ?: throw ResponseFailure("Invalid Mastodon feed URL: ${feedSql.url}")

        val token = mastodonStorage.getAccessToken(instance, account)
            ?: throw ResponseFailure("No token for ${feedSql.title}")

        val limit = prefs.mastodonItemsPerFeed.getValue().toInt().coerceAtLeast(1)

        val statuses = mastodonApi.fetchHomeTimeline(instance, token, limit).getOrElse {
            throw ResponseFailure("Mastodon sync failed for ${feedSql.title}: ${it.message}")
        }

        Log.d(TAG, "Fetched ${statuses.size} Mastodon statuses for ${feedSql.title}")

        val articles = MastodonFeedParser.toArticles(
            statuses = statuses,
            feedId = feedSql.id,
            downloadTime = downloadTime,
            articleRepo = articleRepo,
        )

        val days = getSyncDays(prefs)
        val minKeptPubDate = Clock.System.now().minus(
            period = DateTimePeriod(days = days),
            timeZone = TimeZone.currentSystemDefault()
        ).toEpochMilliseconds()

        val filteredArticles = articles.filter { (article, _) ->
            article.pubDate !in 1..<minKeptPubDate
        }

        articleRepo.updateOrInsertArticle(filteredArticles) { article, text ->
            withContext(Dispatchers.IO) {
                blobOutputStream(article.uuid, filesDir).bufferedWriter().use {
                    it.write(text)
                }
            }
        }

        val ids = articleRepo.getItemsToBeCleanedFromFeed(
            feedId = feedSql.id,
            minKeptPubDate = minKeptPubDate
        )
        Log.d(TAG, "Cleanup ${feedSql.title}: days=$days cutoff=$minKeptPubDate deleting=${ids.size}")

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
}
