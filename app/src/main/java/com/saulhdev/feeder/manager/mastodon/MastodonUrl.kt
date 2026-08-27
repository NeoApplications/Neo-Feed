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

import com.saulhdev.feeder.data.db.models.Feed
import java.net.URL

private const val STORED_SCHEME_PREFIX = "http://mastodon://"

fun Feed.isMastodon(): Boolean = sourceType == "mastodon"

fun URL.toMastodonInstanceAndAccount(): Pair<String, String>? {
    val raw = toString()

    // Legacy storage used a fake http://mastodon:// scheme.
    if (raw.startsWith(STORED_SCHEME_PREFIX)) {
        val rest = raw.removePrefix(STORED_SCHEME_PREFIX)
        val parts = rest.split("/", limit = 2)
        val instance = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
        val account = parts.getOrNull(1)
            ?.removePrefix("@")
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return instance to account
    }

    if (!raw.startsWith("https://", ignoreCase = true)) return null
    val withoutScheme = raw.removePrefix("https://")
    val instance = withoutScheme.substringBefore('/', "")
        .takeIf { it.isNotBlank() } ?: return null
    val account = withoutScheme.substringAfter('/', "")
        .removePrefix("@")
        .takeIf { it.isNotBlank() } ?: return null
    return instance to account
}

fun buildMastodonFeedUrl(instance: String, account: String): String =
    "https://$instance/@$account"
