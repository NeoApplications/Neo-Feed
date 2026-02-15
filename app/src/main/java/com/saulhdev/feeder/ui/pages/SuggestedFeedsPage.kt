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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.entity.SuggestedCategory
import com.saulhdev.feeder.data.entity.SuggestedFeed
import com.saulhdev.feeder.data.entity.SuggestedFeedsData
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.CaretDown
import com.saulhdev.feeder.ui.icons.phosphor.CheckCircle
import com.saulhdev.feeder.ui.icons.phosphor.FunnelSimple
import com.saulhdev.feeder.ui.icons.phosphor.Plus
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.viewmodels.SourceListViewModel

private val languageStringRes =
    mapOf(
        "fr" to R.string.lang_fr,
        "en" to R.string.lang_en,
        "youtube" to R.string.lang_youtube,
    )

private val categoryStringRes =
    mapOf(
        "news" to R.string.cat_news,
        "tech" to R.string.cat_tech,
        "science" to R.string.cat_science,
        "lifestyle" to R.string.cat_lifestyle,
        "cooking" to R.string.cat_cooking,
        "sport" to R.string.cat_sport,
        "gaming" to R.string.cat_gaming,
        "auto" to R.string.cat_auto,
        "android" to R.string.cat_android,
        "opensource" to R.string.cat_opensource,
        "design" to R.string.cat_design,
        "finance" to R.string.cat_finance,
        "youtube" to R.string.cat_youtube,
    )

@Composable
fun SuggestedFeedsPage(
    viewModel: SourceListViewModel = koinNeoViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val existingUrlMap = state.allSources.associate { it.url.toString() to it.id }
    var searchQuery by remember { mutableStateOf("") }
    val collapsedGroups = remember { mutableStateMapOf<String, Boolean>() }
    val collapsedCategories = remember { mutableStateMapOf<String, Boolean>() }

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
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_feeds)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Phosphor.FunnelSimple,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                )
            }

            SuggestedFeedsData.groups.forEach { group ->
                val langResId = languageStringRes[group.key] ?: R.string.lang_en
                val isGroupCollapsed = collapsedGroups[group.key] == true
                val isSearching = searchQuery.isNotBlank()

                item(key = "lang_${group.key}") {
                    val langName = stringResource(langResId)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    collapsedGroups[group.key] = !isGroupCollapsed
                                }.padding(top = 12.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = group.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = langName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                        )
                        Icon(
                            imageVector = Phosphor.CaretDown,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(20.dp)
                                    .rotate(if (isGroupCollapsed && !isSearching) -90f else 0f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (!isGroupCollapsed || isSearching) {
                    group.categories.forEach { category ->
                        val catKey = "${group.key}_${category.key}"
                        val catResId = categoryStringRes[category.key] ?: R.string.cat_news
                        val isCatCollapsed = collapsedCategories[catKey] == true

                        val filteredFeeds =
                            if (isSearching) {
                                category.feeds.filter {
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.description.contains(searchQuery, ignoreCase = true)
                                }
                            } else {
                                category.feeds
                            }

                        if (filteredFeeds.isNotEmpty()) {
                            item(key = "header_$catKey") {
                                val catName = stringResource(catResId)
                                CategoryHeader(
                                    category = category,
                                    catName = catName,
                                    isCollapsed = isCatCollapsed && !isSearching,
                                    addedCount = filteredFeeds.count { existingUrlMap.containsKey(it.url) },
                                    totalCount = filteredFeeds.size,
                                    onToggle = {
                                        collapsedCategories[catKey] = !isCatCollapsed
                                    },
                                    onAddAll = {
                                        filteredFeeds
                                            .filter { !existingUrlMap.containsKey(it.url) }
                                            .forEach { sf ->
                                                viewModel.insertFeed(
                                                    Feed(
                                                        title = sf.title,
                                                        url = sloppyLinkToStrictURL(sf.url),
                                                        description = sf.description,
                                                        tag = category.key,
                                                    ),
                                                )
                                            }
                                    },
                                    onRemoveAll = {
                                        filteredFeeds
                                            .mapNotNull { existingUrlMap[it.url] }
                                            .forEach { id -> viewModel.deleteFeed(id) }
                                    },
                                    allAdded =
                                        filteredFeeds.all { existingUrlMap.containsKey(it.url) },
                                )
                            }

                            if ((!isCatCollapsed || isSearching)) {
                                items(
                                    filteredFeeds,
                                    key = { "${group.key}_${it.url}" },
                                ) { suggestedFeed ->
                                    val feedId = existingUrlMap[suggestedFeed.url]
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = expandVertically(),
                                        exit = shrinkVertically(),
                                    ) {
                                        SuggestedFeedItem(
                                            feed = suggestedFeed,
                                            isAdded = feedId != null,
                                            onAdd = {
                                                viewModel.insertFeed(
                                                    Feed(
                                                        title = suggestedFeed.title,
                                                        url =
                                                            sloppyLinkToStrictURL(suggestedFeed.url),
                                                        description = suggestedFeed.description,
                                                        tag = category.key,
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
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: SuggestedCategory,
    catName: String,
    isCollapsed: Boolean,
    addedCount: Int,
    totalCount: Int,
    onToggle: () -> Unit,
    onAddAll: () -> Unit,
    onRemoveAll: () -> Unit,
    allAdded: Boolean,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = catName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
            Text(
                text = "($addedCount/$totalCount)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = if (allAdded) onRemoveAll else onAddAll,
                ) {
                    Text(
                        text =
                            stringResource(
                                if (allAdded) R.string.remove_all else R.string.add_all,
                            ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Icon(
                    imageVector = Phosphor.CaretDown,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(16.dp)
                            .rotate(if (isCollapsed) -90f else 0f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
