/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

package com.saulhdev.feeder.manager.nextcloud

import kotlinx.serialization.Serializable

@Serializable
data class NextcloudUser(
    val userId: String = "",
    val avatar: String? = null,
)

@Serializable
data class NextcloudVersion(
    val version: String = "",
)

@Serializable
data class NextcloudStatus(
    val version: String = "",
)

@Serializable
data class NextcloudFolder(
    val id: Long = 0,
    val name: String = "",
)

@Serializable
data class NextcloudFoldersResponse(
    val folders: List<NextcloudFolder> = emptyList(),
)

@Serializable
data class NextcloudFeed(
    val id: Long = 0,
    val url: String = "",
    val title: String = "",
    val faviconLink: String? = null,
    val folderId: Long? = null,
    val unreadCount: Int = 0,
    val pinned: Boolean = false,
)

@Serializable
data class NextcloudFeedsResponse(
    val feeds: List<NextcloudFeed> = emptyList(),
    val starredCount: Int = 0,
)

@Serializable
data class NextcloudItem(
    val id: Long = 0,
    val guid: String? = null,
    val guidHash: String? = null,
    val url: String? = null,
    val title: String? = null,
    val author: String? = null,
    val pubDate: Long = 0L,
    val body: String? = null,
    val enclosureMime: String? = null,
    val enclosureLink: String? = null,
    val feedId: Long = 0,
    val unread: Boolean = true,
    val starred: Boolean = false,
    val lastModified: Long = 0L,
)

@Serializable
data class NextcloudItemsResponse(
    val items: List<NextcloudItem> = emptyList(),
)

@Serializable
data class MultipleItemsRequest(
    val itemIds: List<Long> = emptyList(),
    val items: List<Long> = itemIds,
)
