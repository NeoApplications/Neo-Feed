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

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.db.models.FeedItem
import com.saulhdev.feeder.data.entity.SORT_CHRONOLOGICAL
import com.saulhdev.feeder.data.entity.SORT_SOURCE
import com.saulhdev.feeder.data.entity.SORT_TITLE
import com.saulhdev.feeder.data.entity.SortFilterModel
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ArticleListViewModel(
    private val articleRepo: ArticleRepository,
    feedsRepo: SourcesRepository,
    val prefs: FeedPreferences,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    private val sortFilterState = combine(
        prefs.sortingFilter.get(),
        prefs.sortingAsc.get(),
        prefs.sourcesFilter.get(),
        prefs.tagsFilter.get()
    ) { sort, sortAsc, sources, tags ->
        SortFilterModel(sort, sortAsc, sources, tags)
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            SortFilterModel()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val articleListState: StateFlow<ArticleListState> = combine(
        prefs.tagsFilter.get().flatMapLatest { tags ->
            if (tags.any()) articleRepo.getFeedItemsByTags(tags)
            else articleRepo.getEnabledFeedItems()
        },
        sortFilterState,
        prefs.removeDuplicates.get(),
        feedsRepo.isSyncing
    ) { articles, sfm, removeDuplicate, isSyncing ->
        ArticleListState(
            articles = processArticles(articles, sfm, removeDuplicate),
            isFilterModified = sfm != SortFilterModel(),
            isSyncing = isSyncing
        )
    }.stateIn(
        ioScope,
        SharingStarted.Eagerly,
        ArticleListState()
    )

    val bookmarksState: StateFlow<BookmarksState> = combine(
        articleRepo.getBookmarkedFeedItems(),
        sortFilterState,
        prefs.removeDuplicates.get(),
        feedsRepo.isSyncing
    ) { articles, sfm, removeDuplicate, isSyncing ->
        BookmarksState(
            bookmarkedArticles = processArticles(articles, sfm, removeDuplicate),
            isSyncing = isSyncing
        )
    }.stateIn(
        ioScope,
        SharingStarted.Eagerly,
        BookmarksState()
    )

    fun unpinArticle(id: String) {
        viewModelScope.launch {
            articleRepo.unpinArticle(id)
        }
    }

    fun bookmarkArticle(id: String, boolean: Boolean) {
        viewModelScope.launch {
            articleRepo.bookmarkArticle(id, boolean)
        }
    }

    // HELPERS
    private fun processArticles(
        articles: List<FeedItem>,
        sfm: SortFilterModel,
        removeDuplicate: Boolean
    ): List<FeedItem> {
        return articles
            .let { if (removeDuplicate) it.distinctBy { item -> item.link } else it }
            .let { list ->
                if (sfm.sourcesFilter.isEmpty()) list
                else list.filterNot { it.sourceId in sfm.sourcesFilter }
            }
            .let { list ->
                if (sfm.tagsFilter.isEmpty()) list
                else list.filterNot { it.feedTag in sfm.tagsFilter }
            }
            .let { list ->
                when {
                    sfm.sort == SORT_CHRONOLOGICAL && !sfm.sortAsc ->
                        list.sortedByDescending(FeedItem::timeMillis)

                    sfm.sort == SORT_CHRONOLOGICAL && sfm.sortAsc  ->
                        list.sortedBy(FeedItem::timeMillis)

                    sfm.sort == SORT_TITLE && sfm.sortAsc          ->
                        list.sortedBy(FeedItem::contentTitle)

                    sfm.sort == SORT_TITLE && !sfm.sortAsc         ->
                        list.sortedByDescending(FeedItem::contentTitle)

                    sfm.sort == SORT_SOURCE && sfm.sortAsc         ->
                        list.sortedBy(FeedItem::displayTitle)

                    sfm.sort == SORT_SOURCE && !sfm.sortAsc        ->
                        list.sortedByDescending(FeedItem::displayTitle)

                    else                                           -> list.sortedByDescending(
                        FeedItem::timeMillis
                    )
                }
            }
    }
}

data class ArticleListState(
    val articles: List<FeedItem> = emptyList(),
    val isFilterModified: Boolean = false,
    val isSyncing: Boolean = false,
)

data class BookmarksState(
    val bookmarkedArticles: List<FeedItem> = emptyList(),
    val isSyncing: Boolean = false,
)