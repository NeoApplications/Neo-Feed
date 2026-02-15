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

package com.saulhdev.feeder.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.entity.SuggestedFeed
import com.saulhdev.feeder.data.entity.SuggestedFeedsData
import com.saulhdev.feeder.ui.components.PreferenceGroupHeading
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.CheckCircle
import com.saulhdev.feeder.ui.icons.phosphor.Plus
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.viewmodels.SourceListViewModel

@Composable
fun SuggestedFeedsPage(
    viewModel: SourceListViewModel = koinNeoViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val existingUrlMap = state.allSources.associate { it.url.toString() to it.id }

    ViewWithActionBar(
        title = stringResource(R.string.suggested_feeds),
        showBackButton = true,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestedFeedsData.groups.forEach { group ->
                item(key = "lang_${group.language}") {
                    Text(
                        text = group.language,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 8.dp),
                    )
                }
                group.categories.forEach { (category, feeds) ->
                    item(key = "header_${group.language}_$category") {
                        PreferenceGroupHeading(heading = category)
                    }
                    items(feeds, key = { "${group.language}_${it.url}" }) { suggestedFeed ->
                        val feedId = existingUrlMap[suggestedFeed.url]
                        SuggestedFeedItem(
                            feed = suggestedFeed,
                            isAdded = feedId != null,
                            onAdd = {
                                viewModel.insertFeed(
                                    Feed(
                                        title = suggestedFeed.title,
                                        url = sloppyLinkToStrictURL(suggestedFeed.url),
                                        description = suggestedFeed.description,
                                        tag = category,
                                    ),
                                )
                            },
                            onRemove = {
                                feedId?.let { viewModel.deleteFeed(it) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedFeedItem(
    feed: SuggestedFeed,
    isAdded: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = if (isAdded) onRemove else onAdd),
        headlineContent = {
            Text(text = feed.title)
        },
        supportingContent = {
            Text(text = feed.description)
        },
        trailingContent = {
            if (isAdded) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Phosphor.CheckCircle,
                        contentDescription = stringResource(R.string.feed_already_added),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Phosphor.Plus,
                        contentDescription = stringResource(R.string.suggested_feeds),
                    )
                }
            }
        },
    )
}
