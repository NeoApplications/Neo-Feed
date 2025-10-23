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

package com.saulhdev.feeder.data.repository

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.saulhdev.feeder.data.db.ID_ALL
import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.manager.models.scheduleFullTextParse
import com.saulhdev.feeder.manager.sync.FeedSyncer
import com.saulhdev.feeder.manager.sync.requestFeedSync
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Clock

class SourcesRepository(db: NeoFeedDb) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("FeedSourceRepository")
    private val feedsDao = db.feedSourceDao()
    private val workManager: WorkManager by inject(WorkManager::class.java)

    suspend fun insertSource(feed: Feed) = withContext(jcc) {
        feedsDao.insert(feed)
    }

    fun updateSource(feed: Feed, resync: Boolean = false) {
        scope.launch {
            if (feedsDao.existsById(feed.id)) {
                val newFeed = feed.copy(lastSync = Clock.System.now())
                feedsDao.update(newFeed)
                if (resync) requestFeedSync(newFeed.id)
                if (newFeed.fullTextByDefault) scheduleFullTextParse()
            }
        }
    }

    fun getAllSourcesFlow(): Flow<List<Feed>> = feedsDao.getAllFeeds()
        .flowOn(cc)

    suspend fun getAllSources(): List<Feed> = feedsDao.loadFeeds()

    fun getEnabledSources(): Flow<List<Feed>> = feedsDao.getEnabledFeeds()
        .flowOn(cc)

    fun getSourceById(id: Long): Flow<Feed?> = feedsDao.getFeedById(id)
        .flowOn(cc)

    suspend fun loadFeedsByTag(tag: String): List<Feed> = withContext(jcc) {
        feedsDao.loadFeedsByTag(tag)
    }

    suspend fun loadFeedById(feedId: Long): Feed? = withContext(jcc) {
        feedsDao.loadFeedById(feedId)
    }

    suspend fun loadFeedIfStale(feedId: Long, staleTime: Long): Feed? = withContext(jcc) {
        if (feedId == ID_ALL)
            feedsDao.loadFeedIfStale(staleTime)
        else
            feedsDao.loadFeedIfStale(feedId, staleTime)
    }

    suspend fun getAllTags(): List<String> {
        return feedsDao.getAllTags()
    }

    fun getAllTagsFlow(): Flow<List<String>> = feedsDao.getAllTagsFlow()
        .map { rawTags ->
            rawTags.flatMap { tagString ->
                tagString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }.distinct()
        }
        .flowOn(cc)

    suspend fun loadFeedIds(): List<Long> = withContext(jcc) {
        feedsDao.loadFeedIds()
    }

    @OptIn(FlowPreview::class)
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
            feedsDao.setCurrentlySyncingOn(feedId, syncing)
        }
    }

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: kotlin.time.Instant) {
        scope.launch {
            feedsDao.setCurrentlySyncingOn(feedId, syncing, lastSync)
        }
    }

    fun deleteFeed(value: Feed) {
        scope.launch {
            feedsDao.delete(value)
        }
    }
}