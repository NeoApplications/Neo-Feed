/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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

package com.saulhdev.feeder.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saulhdev.feeder.data.db.dao.FeedArticleDao
import com.saulhdev.feeder.data.db.dao.FeedSourceDao
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.db.models.ArticleIdWithLink
import com.saulhdev.feeder.data.db.models.Feed
import org.threeten.bp.ZonedDateTime
import java.util.UUID

const val ID_UNSET: Long = 0
const val ID_ALL: Long = -1L

@Database(
    entities = [
        Feed::class,
        Article::class,
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 3,
            to = 4,
            spec = NeoFeedDb.MigrationMoveToArticle::class
        ),
        AutoMigration(
            from = 4,
            to = 5,
            spec = NeoFeedDb.MigrationRemoveFeedArticle::class
        ),
        AutoMigration(
            from = 5,
            to = 6,
            spec = NeoFeedDb.ReplaceThreetenInstances::class
        ),
        AutoMigration(
            from = 6,
            to = 7,
            spec = NeoFeedDb.RemoveLegacyPubDate::class
        ),
    ],
    views = [
        ArticleIdWithLink::class,
    ],
)
@TypeConverters(Converters::class)
abstract class NeoFeedDb : RoomDatabase() {
    abstract fun feedSourceDao(): FeedSourceDao
    abstract fun feedArticleDao(): FeedArticleDao

    companion object {
        @Volatile
        private var instance: NeoFeedDb? = null

        fun getInstance(context: Context): NeoFeedDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): NeoFeedDb {
            return Room.databaseBuilder(context, NeoFeedDb::class.java, "NeoFeed")
                .addMigrations(*allMigrations)
                .build()
        }
    }

    class MigrationMoveToArticle : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            super.onPostMigrate(db)
            val cursor = db.query("SELECT * FROM FeedArticle")
            val insertStmt = db.compileStatement(
                """
                INSERT INTO Article (
                    uuid, guid, title, plainTitle, imageUrl, enclosureLink,
                    plainSnippet, description, author,
                    pubDate, link, feedId, firstSyncedTime, primarySortTime,
                    categories, pinned, bookmarked
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            )

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val guid = cursor.getString(cursor.getColumnIndexOrThrow("guid"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val plainTitle = cursor.getString(cursor.getColumnIndexOrThrow("plainTitle"))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"))
                val enclosureLink = cursor.getString(cursor.getColumnIndexOrThrow("enclosureLink"))
                val plainSnippet = cursor.getString(cursor.getColumnIndexOrThrow("plainSnippet"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val contentHtml = cursor.getString(cursor.getColumnIndexOrThrow("content_html"))
                val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
                val pubDate = cursor.getLong(cursor.getColumnIndexOrThrow("pubDate"))
                val link = cursor.getString(cursor.getColumnIndexOrThrow("link"))
                val feedId = cursor.getLong(cursor.getColumnIndexOrThrow("feedId"))
                val firstSyncedTime =
                    cursor.getLong(cursor.getColumnIndexOrThrow("firstSyncedTime"))
                val primarySortTime =
                    cursor.getLong(cursor.getColumnIndexOrThrow("primarySortTime"))
                val categoriesString = cursor.getString(cursor.getColumnIndexOrThrow("categories"))
                val pinned = cursor.getInt(cursor.getColumnIndexOrThrow("pinned"))
                val bookmarked = cursor.getInt(cursor.getColumnIndexOrThrow("bookmarked"))

                val uuid = UUID.randomUUID().toString()

                insertStmt.bindString(1, uuid)
                insertStmt.bindString(2, guid)
                insertStmt.bindString(3, title)
                insertStmt.bindString(4, plainTitle)

                if (imageUrl != null)
                    insertStmt.bindString(5, imageUrl)
                else
                    insertStmt.bindNull(5)

                if (enclosureLink != null)
                    insertStmt.bindString(6, enclosureLink)
                else
                    insertStmt.bindNull(6)

                insertStmt.bindString(7, plainSnippet)
                insertStmt.bindString(8, description)

                if (author != null)
                    insertStmt.bindString(9, author)
                else
                    insertStmt.bindNull(9)

                // pubDate can be null, so bind accordingly
                if (pubDate != 0L)
                    insertStmt.bindLong(10, pubDate)
                else
                    insertStmt.bindNull(10)

                if (link != null)
                    insertStmt.bindString(11, link)
                else
                    insertStmt.bindNull(11)

                insertStmt.bindLong(12, feedId)
                insertStmt.bindLong(13, firstSyncedTime)
                insertStmt.bindLong(14, primarySortTime)
                insertStmt.bindString(15, categoriesString)
                insertStmt.bindLong(16, pinned.toLong())
                insertStmt.bindLong(17, bookmarked.toLong())

                insertStmt.executeInsert()
            }
            cursor.close()
        }
    }

    @DeleteTable(tableName = "FeedArticle")
    class MigrationRemoveFeedArticle : AutoMigrationSpec

    class ReplaceThreetenInstances : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            // Copy data from legacy pubDate to pubDateV2 before the column is deleted
            val cursor = db.query("SELECT uuid, pubDate FROM article")
            while (cursor.moveToNext()) {
                val uuid = cursor.getString(0)
                val pubDateStr = cursor.getString(1)

                // Convert pubDateStr to epoch millis for pubDateV2
                val pubDateMillis = convertZDT2Millis(pubDateStr)

                val stmt = db.compileStatement("UPDATE article SET pubDateV2 = ? WHERE uuid = ?")
                stmt.bindLong(1, pubDateMillis)
                stmt.bindString(2, uuid)
                stmt.executeUpdateDelete()
            }
            cursor.close()
        }

        // TODO remove in two releases
        private fun convertZDT2Millis(pubDateStr: String?): Long {
            if (pubDateStr == null) return 0L
            return try {
                val zdt = ZonedDateTime.parse(pubDateStr)
                zdt.toInstant().toEpochMilli()
            } catch (_: Exception) {
                0L
            }
        }
    }

    @DeleteColumn(tableName = "Article", columnName = "pubDate")
    class RemoveLegacyPubDate : AutoMigrationSpec
}

val allMigrations = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

@Suppress("ClassName")
object MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE Feeds ADD COLUMN fullTextByDefault INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_2_3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE Feeds ADD COLUMN isEnabled INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )
    }
}