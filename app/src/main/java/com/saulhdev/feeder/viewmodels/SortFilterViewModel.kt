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
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.entity.SortFilterModel
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class SortFilterViewModel(
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

    val sheetState: StateFlow<SortFilterSheetState> = combine(
        sortFilterState,
        feedsRepo.getEnabledSources(),
        feedsRepo.getAllTagsFlow(),
    ) { sortFilter, activeSources, activeTags ->
        SortFilterSheetState(
            sortFilter = sortFilter,
            activeSources = activeSources,
            activeTags = activeTags,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SortFilterSheetState()
    )
}

data class SortFilterSheetState(
    val sortFilter: SortFilterModel = SortFilterModel(),
    val activeSources: List<Feed> = emptyList(),
    val activeTags: List<String> = emptyList(),
)