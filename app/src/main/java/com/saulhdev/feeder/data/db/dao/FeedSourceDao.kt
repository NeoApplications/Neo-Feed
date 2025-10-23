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

package com.saulhdev.feeder.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saulhdev.feeder.data.db.models.Feed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URL
import kotlin.time.Instant

@Dao
interface FeedSourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: Feed): Long

    @Update
    suspend fun update(feed: Feed): Int

    @Delete
    suspend fun delete(feed: Feed): Int

    @Query("SELECT * FROM Feeds WHERE url = :url")
    suspend fun getFeedByURL(url: URL): Feed?

    @Query("SELECT * FROM Feeds WHERE id = :id")
    fun getFeedById(id: Long): Flow<Feed?>

    @Query("SELECT * FROM Feeds WHERE id = :id")
    suspend fun loadFeedById(id: Long): Feed?

    @Query("SELECT EXISTS (SELECT 1 FROM Feeds WHERE id = :id)")
    suspend fun existsById(id: Long): Boolean

    @Query("SELECT * FROM Feeds WHERE isEnabled IS 1")
    suspend fun loadFeeds(): List<Feed>

    @Query("SELECT * FROM Feeds WHERE isEnabled IS 1")
    fun getEnabledFeeds(): Flow<List<Feed>>

    @Query("SELECT id FROM Feeds WHERE isEnabled IS 1")
    suspend fun loadFeedIds(): List<Long>

    @Query("SELECT * FROM Feeds")
    fun getAllFeeds(): Flow<List<Feed>>

    @Query("SELECT DISTINCT tag FROM Feeds ORDER BY tag COLLATE NOCASE")
    fun getAllTagsFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT tag FROM Feeds ORDER BY tag COLLATE NOCASE")
    suspend fun getAllTags(): List<String>

    @Query("SELECT * FROM feeds WHERE ',' || tag || ',' LIKE :pattern")
    suspend fun loadFeedsByTag(pattern: String): List<Feed>

    fun getFeedByTags(tags: Set<String>): Flow<List<Feed>> = flow {
        val allFeeds = tags.flatMap { tag ->
            loadFeedsByTag("%,$tag,%")
        }.distinctBy { it.id }
        emit(allFeeds)
    }

    @Query(
        """
       SELECT * FROM Feeds
       WHERE id is :feedId
       AND lastSync < :staleTime
    """
    )
    suspend fun loadFeedIfStale(feedId: Long, staleTime: Long): Feed?

    @Query(
        """
       SELECT * FROM Feeds
       WHERE lastSync < :staleTime and isEnabled IS 1
    """
    )
    suspend fun loadFeedIfStale(staleTime: Long): Feed?

    @Query(
        """
            UPDATE feeds
            SET currentlySyncing = :syncing
            WHERE id IS :feedId
        """
    )
    suspend fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean): Int

    @Query(
        """
            UPDATE feeds
            SET isEnabled = :isEnabled
            WHERE id IS :feedId
        """
    )
    suspend fun setIsEnabled(feedId: Long, isEnabled: Boolean)

    @Query(
        """
            UPDATE feeds
            SET currentlySyncing = :syncing, lastSync = :lastSync
            WHERE id IS :feedId
        """
    )
    suspend fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: Instant)
}
