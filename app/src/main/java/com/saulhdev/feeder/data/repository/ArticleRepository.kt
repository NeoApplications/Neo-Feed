/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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

package com.saulhdev.feeder.data.repository

import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.db.models.ArticleIdWithLink
import com.saulhdev.feeder.data.db.models.FeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleRepository(db: NeoFeedDb) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()
    private val articlesDao = db.feedArticleDao()

    suspend fun deleteArticles(ids: List<String>) = withContext(jcc) {
        articlesDao.deleteArticles(ids)
    }

    suspend fun getArticleByGuid(guid: String, feedId: Long): Article? {
        return withContext(jcc) {
            articlesDao.loadArticle(guid = guid, feedId = feedId)
        }
    }

    fun getArticleById(articleId: String): Flow<Article?> =
        articlesDao.loadArticleById(id = articleId)
            .flowOn(cc)

    fun getEnabledFeedItems(): Flow<List<FeedItem>> = articlesDao.getAllEnabledFeedItems()
        .flowOn(cc)

    fun getFeedItemsByTags(tags: Set<String>): Flow<List<FeedItem>> =
        articlesDao.getFeedItemsByTagsSimple(tags)
            .flowOn(cc)

    suspend fun updateOrInsertArticle(
        itemsWithText: List<Pair<Article, String>>,
        block: suspend (Article, String) -> Unit
    ) = withContext(jcc) {
        articlesDao.insertOrUpdate(itemsWithText, block)
    }

    suspend fun bookmarkArticle(
        articleId: String,
        bookmark: Boolean,
    ) = withContext(jcc) {
        articlesDao.getArticleById(articleId)?.let {
            articlesDao.updateFeedArticle(it.copy(bookmarked = bookmark, pinned = bookmark))
        }
    }

    suspend fun unpinArticle(
        articleId: String,
        pin: Boolean = false,
    ) = withContext(jcc) {
        articlesDao.getArticleById(articleId)?.let {
            articlesDao.updateFeedArticle(it.copy(pinned = pin))
        }
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, minKeptPubDate: Long) = withContext(jcc) {
        articlesDao.getItemsToBeCleanedFromFeed(
            feedId = feedId,
            minKeptPubDate = minKeptPubDate
        )
    }

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<ArticleIdWithLink>> =
        articlesDao.getArticleIdLinks()
            .flowOn(cc)

    fun getBookmarkedFeedItems(): Flow<List<FeedItem>> = articlesDao.getAllBookmarkedFeedItems()
        .flowOn(cc)

    fun getPinnedFeedItems(): Flow<List<FeedItem>> = articlesDao.getPinnedFeedItems()
        .flowOn(cc)
}