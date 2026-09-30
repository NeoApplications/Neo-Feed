package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedItem
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class SourcesPluginsViewModel(
    feedsRepo: SourcesRepository,
    articleRepo: ArticleRepository,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val state =
        combine(
                feedsRepo.getAllSourcesFlow(),
                feedsRepo.getAllTagsFlow(),
                // TODO move the getter eventually to SourcesRepository
                articleRepo.getBookmarkedFeedItems(),
            ) { allSources, allTags, bookmarked ->
                SourcesPluginsState(
                    tagsSourcesMap =
                        allTags.plus("").associateWith { tag ->
                            allSources.filter { it.tag.contains(tag) }
                        },
                    bookmarked = bookmarked,
                )
            }
            .stateIn(
                ioScope,
                SharingStarted.Eagerly,
                SourcesPluginsState(),
            )
}

data class SourcesPluginsState(
    val tagsSourcesMap: Map<String, List<Feed>> = emptyMap(),
    val bookmarked: List<FeedItem> = emptyList(),
)
