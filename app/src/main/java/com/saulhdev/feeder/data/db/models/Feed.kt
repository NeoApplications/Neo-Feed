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

package com.saulhdev.feeder.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import java.net.URL
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(
    tableName = "Feeds",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["id", "url", "title"], unique = true)
    ]
)
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = ID_UNSET,
    val title: String = "",
    val description: String = "",
    val url: URL = sloppyLinkToStrictURL(""),
    val feedImage: URL = sloppyLinkToStrictURL(""),
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val lastSync: Instant = Clock.System.now(),
    val alternateId: Boolean = false,
    val fullTextByDefault: Boolean = false,
    val tag: String = "",
    val currentlySyncing: Boolean = false,
    val isEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "rss")
    val sourceType: String = "rss",
)