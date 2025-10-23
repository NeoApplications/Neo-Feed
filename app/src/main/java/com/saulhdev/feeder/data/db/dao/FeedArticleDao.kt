/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.saulhdev.feeder.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.db.models.ArticleIdWithLink
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FeedArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedArticle(item: Article): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedArticle(items: List<Article>): List<Long>

    @Update
    suspend fun updateFeedArticle(item: Article): Int

    @Update
    suspend fun updateFeedArticle(items: List<Article>): Int

    @Delete
    suspend fun deleteFeedArticle(item: Article): Int

    @Query(
        """
        DELETE FROM Article WHERE uuid IN (:ids)
        """
    )
    suspend fun deleteArticles(ids: List<String>): Int

    @Query(
        """
        DELETE FROM Article WHERE uuid IN (:ids)
        """
    )
    suspend fun deleteFeedArticle(ids: List<String>): Int

    @Query(
        """
        DELETE FROM Article WHERE feedId = :feedId
        """
    )
    suspend fun deleteFeedArticle(feedId: Long?): Int

    @Query("SELECT * FROM Article WHERE guid IS :guid AND feedId IS :feedId")
    suspend fun loadArticle(guid: String, feedId: Long?): Article?

    @Query("SELECT * FROM Article WHERE guid IS :guid")
    suspend fun loadArticleByGuid(guid: String): Article?

    @Query("SELECT * FROM Article WHERE uuid IS :id")
    suspend fun getArticleById(id: String): Article?

    @Query("SELECT * FROM Article WHERE uuid IS :id")
    fun loadArticleById(id: String): Flow<Article?>

    @Query("SELECT * FROM Article WHERE feedId IS :feedId")
    suspend fun loadArticles(feedId: Long?): List<Article>

    @Query(
        """
        SELECT Article.* FROM Article
        JOIN Feeds ON Article.feedId = Feeds.id
        WHERE Feeds.isEnabled = 1
    """
    )
    fun getAllEnabledFeedArticles(): Flow<List<Article>>

    @Query(
        """
        SELECT uuid FROM Article
        WHERE feedId IS :feedId AND pinned = 0 AND bookmarked = 0
        ORDER BY primarySortTime DESC, pubDateV2 DESC
        LIMIT -1 OFFSET :keepCount
        """
    )
    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int): List<String>

    @Query("SELECT * FROM ArticleIdWithLink")
    fun getArticleIdLinks(): Flow<List<ArticleIdWithLink>>

    @Query(
        """
            SELECT *
            FROM Article
            WHERE bookmarked = 1
            ORDER BY pinned DESC, pubDateV2 DESC
        """
    )
    fun getAllBookmarked(): Flow<List<Article>>

    // Embedded FeedItem
    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Feeds.isEnabled = 1
    ORDER BY Article.primarySortTime DESC
    """
    )
    fun getAllEnabledFeedItems(): Flow<List<FeedItem>>

    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Article.bookmarked = 1 AND Feeds.isEnabled = 1
    ORDER BY Article.pinned DESC, Article.pubDateV2 DESC
    """
    )
    fun getAllBookmarkedFeedItems(): Flow<List<FeedItem>>

    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Feeds.isEnabled = 1 AND Feeds.tag IN (:tags)
    ORDER BY Article.primarySortTime DESC
    """
    )
    fun getFeedItemsByTagsSimple(tags: Set<String>): Flow<List<FeedItem>>

    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Article.feedId = :feedId AND Feeds.isEnabled = 1
    ORDER BY Article.primarySortTime DESC
    """
    )
    fun getFeedItemsForFeed(feedId: Long): Flow<List<FeedItem>>

    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Article.pinned = 1 AND Feeds.isEnabled = 1
    ORDER BY Article.primarySortTime DESC
    """
    )
    fun getPinnedFeedItems(): Flow<List<FeedItem>>

    @Query("SELECT * FROM Feeds WHERE ',' || tag || ',' LIKE :pattern AND isEnabled = 1")
    suspend fun getEnabledFeedsByTagPattern(pattern: String): List<Feed>

    @Transaction
    suspend fun getFeedItemsByTagsComplex(tags: Set<String>): List<FeedItem> {
        val feedIds = tags.flatMap { tag ->
            getEnabledFeedsByTagPattern("%,$tag,%")
        }.map { it.id }.distinct()

        return getFeedItemsByFeedIds(feedIds)
    }

    @Query(
        """
    SELECT Article.* FROM Article
    JOIN Feeds ON Article.feedId = Feeds.id
    WHERE Article.feedId IN (:feedIds) AND Feeds.isEnabled = 1
    ORDER BY Article.primarySortTime DESC
    """
    )
    suspend fun getFeedItemsByFeedIds(feedIds: List<Long>): List<FeedItem>

    @Transaction
    suspend fun insertOrUpdate(
        itemsWithText: List<Pair<Article, String>>,
        block: suspend (Article, String) -> Unit
    ) {
        val (toUpdateItems, toInsertItems) = itemsWithText.partition { (item, _) ->
            item.uuid.isNotEmpty()
        }

        updateFeedArticle(toUpdateItems.map { (item, _) -> item })
        toUpdateItems.forEach { (item, text) ->
            block(item, text)
        }

        toInsertItems.map { (item, text) -> item.copy(uuid = UUID.randomUUID().toString()) to text }
            .apply {
                forEach { (article, text) ->
                    insertFeedArticle(article)
                    block(article, text)
                }
            }
    }
}
