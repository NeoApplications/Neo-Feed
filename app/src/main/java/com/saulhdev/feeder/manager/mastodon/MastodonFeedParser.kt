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
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import kotlin.time.Clock
import kotlin.time.Instant

object MastodonFeedParser {

    private const val TAG = "MastodonFeedParser"
    private val hrefRegex = """<a[^>]+href=\"(https?://[^\"]+)\"[^>]*>""".toRegex(RegexOption.IGNORE_CASE)

    suspend fun toArticles(
        statuses: List<MastodonStatus>,
        feed: Feed,
        downloadTime: Instant,
        articleRepo: ArticleRepository,
    ): List<Pair<Article, String>> {
        val converter = HtmlToPlainTextConverter()
        return statuses.mapNotNull { status ->
            val original = status.reblog ?: status

            if (feed.excludeReplies && original.inReplyToId != null) return@mapNotNull null
            if (feed.requireLink && !hasExternalLink(original.content)) return@mapNotNull null
            if (feed.requireImage && !hasPicture(original.mediaAttachments)) return@mapNotNull null

            val guid = status.id
            val existing = articleRepo.getArticleByGuid(guid, feed.id)
            val author = original.account.displayName.ifBlank { original.account.acct }
            val plainSnippet = converter.convert(original.content).take(200)
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
                description = original.content,
                plainSnippet = plainSnippet,
                imageUrl = original.mediaAttachments.firstOrNull()?.previewUrl,
                author = author,
                link = original.url ?: original.uri,
                pubDate = pubDate,
                primarySortTime = primarySortTime,
                feedId = feed.id,
            )
            article to original.content
        }
    }

    private fun hasExternalLink(content: String): Boolean {
        return hrefRegex.findAll(content).any { match ->
            val path = match.groupValues[1]
                .substringAfter("://")
                .substringAfter('/', "")
            !(path.startsWith("@") || path.startsWith("tags/", ignoreCase = true))
        }
    }

    private fun hasPicture(attachments: List<MastodonMediaAttachment>): Boolean {
        return attachments.any {
            it.type.equals("image", ignoreCase = true) || it.type.equals("gifv", ignoreCase = true)
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
