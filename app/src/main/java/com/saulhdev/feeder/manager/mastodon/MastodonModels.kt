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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonAppCredentials(
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,
)

@Serializable
data class MastodonToken(
    @SerialName("access_token") val accessToken: String,
)

@Serializable
data class MastodonAccount(
    @SerialName("display_name") val displayName: String = "",
    val acct: String,
)

@Serializable
data class MastodonMediaAttachment(
    @SerialName("preview_url") val previewUrl: String? = null,
    val url: String? = null,
    val type: String? = null,
)

@Serializable
data class MastodonStatus(
    val id: String,
    val uri: String = "",
    @SerialName("created_at") val createdAt: String,
    val content: String = "",
    val url: String? = null,
    val account: MastodonAccount,
    @SerialName("media_attachments") val mediaAttachments: List<MastodonMediaAttachment> = emptyList(),
    val reblog: MastodonStatus? = null,
    @SerialName("in_reply_to_id") val inReplyToId: String? = null,
)
