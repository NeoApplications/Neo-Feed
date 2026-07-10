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

import android.util.Log
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import kotlin.time.Clock
import kotlin.time.Instant

object MastodonFeedParser {

    private const val TAG = "MastodonFeedParser"

    suspend fun toArticles(
        statuses: List<MastodonStatus>,
        feedId: Long,
        downloadTime: Instant,
        articleRepo: ArticleRepository,
    ): List<Pair<Article, String>> {
        val converter = HtmlToPlainTextConverter()
        return statuses.map { status ->
            val guid = status.id
            val existing = articleRepo.getArticleByGuid(guid, feedId)
            val author = status.account.displayName.ifBlank { status.account.acct }
            val plainSnippet = converter.convert(status.content).take(200)
            val pubDate = parseDate(status.createdAt)
            val primarySortTime = if (pubDate > 0L) {
                minOf(downloadTime, Instant.fromEpochMilliseconds(pubDate))
            } else {
                downloadTime
            }
            val article = (existing ?: Article(firstSyncedTime = downloadTime)).copy(
                uuid = existing?.uuid ?: "",
                guid = guid,
                title = author,
                plainTitle = author,
                description = status.content,
                plainSnippet = plainSnippet,
                imageUrl = status.mediaAttachments.firstOrNull()?.previewUrl,
                author = author,
                link = status.url,
                pubDate = pubDate,
                primarySortTime = primarySortTime,
                feedId = feedId,
            )
            article to status.content
        }
    }

    private fun parseDate(createdAt: String): Long {
        return try {
            Instant.parse(createdAt).toEpochMilliseconds()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to parse date: $createdAt", e)
            Clock.System.now().toEpochMilliseconds()
        }
    }
}
