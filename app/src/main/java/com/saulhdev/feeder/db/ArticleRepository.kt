/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.db

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.saulhdev.feeder.db.dao.insertOrUpdate
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.db.models.FeedArticle
import com.saulhdev.feeder.db.models.FeedItemIdWithLink
import com.saulhdev.feeder.models.scheduleFullTextParse
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sync.FeedSyncer
import com.saulhdev.feeder.sync.requestFeedSync
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import java.net.URL

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("FeedArticleRepository")
    private val feedSourceDao = NeoFeedDb.getInstance(context).feedSourceDao()
    private val workManager: WorkManager by inject(WorkManager::class.java)

    fun insertFeed(feed: Feed) {
        scope.launch {
            feedSourceDao.insert(feed)
        }
    }

    fun updateFeed(title: String, url: URL, fullTextByDefault: Boolean, isEnabled: Boolean) {
        scope.launch {
            feedSourceDao.getFeedByURL(url)
                ?.copy(
                    title = title,
                    url = url,
                    fullTextByDefault = fullTextByDefault,
                    isEnabled = isEnabled
                )?.let {
                    feedSourceDao.update(it)
                }
        }
    }

    fun updateFeed(feed: Feed, resync: Boolean = false) {
        scope.launch {
            val list: List<Feed> = feedSourceDao.findFeedById(feed.id)
            if (list.isNotEmpty()) {
                feed.lastSync = ZonedDateTime.now().toInstant()
                feedSourceDao.update(feed)
                if (resync) requestFeedSync(feed.id)
                if (feed.fullTextByDefault) scheduleFullTextParse()
            }
        }
    }

    suspend fun getFeed(feedId: Long): Feed? = feedSourceDao.loadFeedById(feedId)

    fun getAllFeeds(): List<Feed> {
        return feedSourceDao.loadFeeds()
    }

    fun getFeedById(id: Long): Flow<Feed?> {
        return feedSourceDao.getFeedById(id)
    }

    val isSyncing: StateFlow<Boolean> =
        workManager.getWorkInfosByTagFlow(FeedSyncer::class.qualifiedName!!)
            .map {
                workManager.pruneWork()
                it.any { work ->
                    work.state == WorkInfo.State.RUNNING || work.state == WorkInfo.State.BLOCKED
                }
            }
            .debounce(1000L)
            .stateIn(
                scope,
                SharingStarted.Lazily,
                false
            )

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean) {
        scope.launch {
            feedSourceDao.setCurrentlySyncingOn(feedId, syncing)
        }
    }

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: Instant) {
        scope.launch {
            feedSourceDao.setCurrentlySyncingOn(feedId, syncing, lastSync)
        }
    }

    /* Articles */
    private val feedArticleDao = NeoFeedDb.getInstance(context).feedArticleDao()

    suspend fun getFeedArticles(feed: Feed): ArrayList<FeedArticle> = withContext(Dispatchers.IO) {
        val list: ArrayList<FeedArticle> = arrayListOf()
        list.addAll(feedArticleDao.loadArticles(feed.id))
        list
    }

    suspend fun deleteArticles(ids: List<Long>) {
        feedArticleDao.deleteArticles(ids)
    }

    suspend fun getArticleByGuid(guid: String, feedId: Long): FeedArticle? {
        return feedArticleDao.loadArticle(guid = guid, feedId = feedId)
    }

    fun getArticleById(feedId: Long): Flow<FeedArticle?> {
        return feedArticleDao.loadArticleById(id = feedId)
    }

    fun getFeedArticles(): Flow<List<FeedItem>> = combine(
        feedArticleDao.getAllEnabledFeedArticles(),
        feedSourceDao.getEnabledFeeds()
    ) { articles, feeds ->
        articles.mapNotNull { article ->
            feeds.find { it.id == article.feedId }?.let { feed ->
                FeedItem(article, feed)
            }
        }
    }

    suspend fun updateOrInsertArticle(
        itemsWithText: List<Pair<FeedArticle, String>>,
        block: suspend (FeedArticle, String) -> Unit
    ) {
        feedArticleDao.insertOrUpdate(itemsWithText, block)
    }

    suspend fun bookmarkArticle(
        articleId: Long,
        bookmark: Boolean,
    ) = feedArticleDao.getArticleById(articleId)?.let {
        feedArticleDao.updateFeedArticle(it.copy(bookmarked = bookmark, pinned = bookmark))
    }

    suspend fun unpinArticle(
        articleId: Long,
        pin: Boolean = false,
    ) = feedArticleDao.getArticleById(articleId)?.let {
        feedArticleDao.updateFeedArticle(it.copy(pinned = pin))
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int) =
        feedArticleDao.getItemsToBeCleanedFromFeed(feedId = feedId, keepCount = keepCount)

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>> =
        feedArticleDao.getFeedsItemsWithDefaultFullTextParse()

    fun getBookmarkedArticlesMap(): Flow<Map<FeedArticle, Feed>> =
        feedArticleDao.getAllBookmarked().mapLatest {
            it.associateWith { fa ->
                feedSourceDao.findFeedById(fa.feedId).first()
            }
        }
}