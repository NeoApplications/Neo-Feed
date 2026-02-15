package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedItem
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SourceListViewModel(
    private val feedsRepo: SourcesRepository,
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
            val (enabledSources, disabledSources) = allSources.partition { it.isEnabled }
            SourceListState(
                allSources = allSources,
                enabledSources = enabledSources,
                disabledSources = disabledSources,
                tagsSourcesMap =
                    allTags.plus("").associateWith { tag ->
                        allSources.filter { it.tag.contains(tag) }
                    },
                bookmarked = bookmarked,
            )
        }.stateIn(
            ioScope,
            SharingStarted.Eagerly,
            SourceListState(),
        )

    fun insertFeed(feed: Feed) {
        viewModelScope.launch {
            feedsRepo.insertSource(feed)
        }
    }

    fun deleteFeed(feedId: Long) {
        viewModelScope.launch {
            feedsRepo.deleteFeed(feedId)
        }
    }

    fun updateFeed(
        source: Feed,
        enable: Boolean,
    ) {
        viewModelScope.launch {
            feedsRepo.updateSource(source, enable)
        }
    }

    fun saveFeed(results: List<SearchResult>) {
        results.forEach { result ->
            if (result.isError) {
                return@forEach
            } else {
                val feed =
                    Feed(
                        title = result.title,
                        description = result.description,
                        url = sloppyLinkToStrictURL(result.url),
                        feedImage = sloppyLinkToStrictURL(result.url),
                    )
                insertFeed(feed)
            }
        }
    }
}

data class SourceListState(
    val allSources: List<Feed> = emptyList(),
    val enabledSources: List<Feed> = emptyList(),
    val disabledSources: List<Feed> = emptyList(),
    val tagsSourcesMap: Map<String, List<Feed>> = emptyMap(),
    val bookmarked: List<FeedItem> = emptyList(),
)