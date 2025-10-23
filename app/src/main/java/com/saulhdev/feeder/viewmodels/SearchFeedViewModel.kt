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

package com.saulhdev.feeder.viewmodels

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Immutable
import com.saulhdev.feeder.manager.models.FeedParser
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.parcelize.Parcelize
import java.net.URL

class SearchFeedViewModel : NeoViewModel() {
    private val feedParser: FeedParser = FeedParser()

    fun searchForFeeds(url: URL) =
        flow {
            emit(url)
            feedParser.getAlternateFeedLinksAtUrl(url)
                .forEach {
                    emit(sloppyLinkToStrictURL(it.first))
                }
        }.mapNotNull {
            try {
                feedParser.parseFeedUrl(it)?.let { feed ->
                    SearchResult(
                        title = feed.title ?: "",
                        url = feed.feed_url ?: it.toString(),
                        description = feed.description ?: "",
                        isError = false
                    )
                }
            } catch (t: Throwable) {
                Log.e("searchForFeeds", "Failed to parse", t)
                SearchResult(
                    title = FAILED_TO_PARSE_PLACEHOLDER,
                    url = it.toString(),
                    description = t.message ?: "",
                    isError = true
                )
            }
        }
            .flowOn(Dispatchers.Default)

    companion object {
        const val FAILED_TO_PARSE_PLACEHOLDER = "failed_to_parse"
    }
}

@Immutable
@Parcelize
data class SearchResult(
    val title: String,
    val url: String,
    val description: String,
    val isError: Boolean,
) : Parcelable