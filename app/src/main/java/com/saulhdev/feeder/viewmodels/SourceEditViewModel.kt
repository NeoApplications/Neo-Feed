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
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.entity.SourceEditViewState
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalCoroutinesApi::class)
class SourceEditViewModel : NeoViewModel() {
    private val repository: SourcesRepository by inject(SourcesRepository::class.java)

    private val _feedId: MutableStateFlow<Long> = MutableStateFlow(-1L)

    fun setFeedId(value: Long) {
        _feedId.update { value }
    }

    private val feed = _feedId.mapLatest {
        repository.loadFeedById(it) ?: Feed()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Feed()
    )

    fun updateFeed(state: SourceEditViewState) {
        repository.updateSource(
            feed = feed.value.copy(
                title = state.title,
                url = sloppyLinkToStrictURL(state.url),
                tag = state.tag,
                fullTextByDefault = state.fullTextByDefault,
                isEnabled = state.isEnabled,
            ),
            resync = feed.value.fullTextByDefault != state.fullTextByDefault
                    || feed.value.isEnabled != state.isEnabled
        )
    }

    val viewState = feed.map { feed: Feed ->
        SourceEditViewState(
            title = feed.title,
            url = feed.url.toString(),
            tag = feed.tag,
            fullTextByDefault = feed.fullTextByDefault,
            isEnabled = feed.isEnabled
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SourceEditViewState()
    )
}