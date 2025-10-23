/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Saul Henriquez <henriquez.saul@gmail.com>
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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.NeoApp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.utils.extensions.launchView
import com.saulhdev.feeder.manager.sync.SyncRestClient
import com.saulhdev.feeder.ui.components.ArticleItem
import com.saulhdev.feeder.ui.components.BookmarkItem
import com.saulhdev.feeder.ui.components.OverflowMenu
import com.saulhdev.feeder.ui.components.PullToRefreshLazyColumn
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.ArrowCounterClockwise
import com.saulhdev.feeder.ui.icons.phosphor.Bookmarks
import com.saulhdev.feeder.ui.icons.phosphor.CaretUp
import com.saulhdev.feeder.ui.icons.phosphor.Filter
import com.saulhdev.feeder.ui.icons.phosphor.Filtered
import com.saulhdev.feeder.ui.icons.phosphor.Power
import com.saulhdev.feeder.utils.openLinkInCustomTab
import com.saulhdev.feeder.viewmodels.ArticleListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun ArticleListPage(
    prefs: FeedPreferences = koinInject(),
    syncClient: SyncRestClient = koinInject(),
    viewModel: ArticleListViewModel = koinNeoViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val articleId = remember { mutableStateOf("") }

    val state by viewModel.articleListState.collectAsState()
    val bookmarked by viewModel.bookmarksState.collectAsState()

    var showBookmarks by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val showFAB by remember { derivedStateOf { listState.firstVisibleItemIndex > 4 } }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        listPane = {
            AnimatedPane {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    containerColor = Color.Transparent,
                    sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    sheetShape = MaterialTheme.shapes.extraSmall,
                    sheetContent = {
                        if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                            SortFilterSheet {
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            }
                        } else Spacer(modifier = Modifier.height(8.dp))
                    },
                ) {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        containerColor = Color.Transparent,
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                                ),
                                title = { Text(text = stringResource(id = R.string.app_name)) },
                                scrollBehavior = scrollBehavior,
                                actions = {
                                    IconButton(
                                        modifier = Modifier
                                            .size(size = 40.dp)
                                            .clip(CircleShape),
                                        onClick = {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (state.isFilterModified) Phosphor.Filter else Phosphor.Filtered,
                                            contentDescription = stringResource(id = R.string.sorting_order),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        color = if (showBookmarks) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent,
                                        shape = MaterialTheme.shapes.large,
                                        onClick = {
                                            showBookmarks = !showBookmarks
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.padding(8.dp),
                                            imageVector = Phosphor.Bookmarks,
                                            contentDescription = stringResource(id = R.string.title_bookmarks),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    OverflowMenu {
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = stringResource(id = R.string.action_reload))
                                            },
                                            onClick = {
                                                hideMenu()
                                                scope.launch {
                                                    syncClient.syncAllFeeds()
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Phosphor.ArrowCounterClockwise,
                                                    contentDescription = null,
                                                )
                                            }
                                        )
                                        HorizontalDivider()

                                        DropdownMenuItem(
                                            text = {
                                                Text(text = stringResource(id = R.string.action_restart))
                                            },
                                            onClick = {
                                                hideMenu()
                                                NeoApp.instance!!.restart(false)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Phosphor.Power,
                                                    contentDescription = null,
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            AnimatedVisibility(
                                visible = showFAB,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    onClick = {
                                        scope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Phosphor.CaretUp,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            when {
                                showBookmarks -> LazyColumn(
                                    state = listState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    items(bookmarked.bookmarkedArticles, key = { it.id }) { item ->
                                        BookmarkItem(
                                            article = item.article,
                                            feed = item.feed,
                                            onClickAction = { article ->
                                                if (prefs.openInBrowser.getValue()) {
                                                    context.launchView(article.link ?: "")
                                                } else {
                                                    scope.launch {
                                                        if (prefs.offlineReader.getValue()) {
                                                            scope.launch {
                                                                paneNavigator.navigateTo(
                                                                    ListDetailPaneScaffoldRole.Detail,
                                                                    article.uuid
                                                                )
                                                            }
                                                        } else {
                                                            openLinkInCustomTab(
                                                                context,
                                                                article.link!!
                                                            )
                                                        }
                                                    }
                                                }
                                                scope.launch {
                                                    viewModel.unpinArticle(article.uuid)
                                                }
                                            },
                                            onRemoveAction = {
                                                scope.launch {
                                                    viewModel.bookmarkArticle(it.uuid, false)
                                                }
                                            }
                                        )
                                    }
                                }

                                else          -> PullToRefreshLazyColumn(
                                    isRefreshing = state.isSyncing,
                                    onRefresh = syncClient::syncAllFeeds,
                                    listState = listState,
                                    content = {
                                        items(state.articles, key = { it.id }) { item ->
                                            ArticleItem(
                                                article = item,
                                                onBookmark = {
                                                    viewModel.bookmarkArticle(item.id, it)
                                                },
                                            ) {
                                                if (prefs.openInBrowser.getValue()) {
                                                    context.launchView(item.link)
                                                } else {
                                                    if (prefs.offlineReader.getValue()) {
                                                        scope.launch {
                                                            paneNavigator.navigateTo(
                                                                ListDetailPaneScaffoldRole.Detail,
                                                                item.id
                                                            )
                                                        }
                                                    } else {
                                                        openLinkInCustomTab(
                                                            context,
                                                            item.link
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            articleId.value = paneNavigator.currentDestination
                ?.takeIf { it.pane == this.paneRole }?.contentKey
                ?.toString().orEmpty()

            articleId.value.takeIf { it.isNotEmpty() }?.let { id ->
                AnimatedPane {
                    ArticlePage(id) {
                        scope.launch {
                            paneNavigator.navigateBack()
                        }
                    }
                }
            }
        }
    )
}
