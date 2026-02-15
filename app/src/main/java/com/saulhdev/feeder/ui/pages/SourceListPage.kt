/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team <saulhdev@hotmail.com>
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.manager.models.exportBookmarks
import com.saulhdev.feeder.manager.models.exportOpml
import com.saulhdev.feeder.manager.models.importBookmarks
import com.saulhdev.feeder.manager.models.importOpml
import com.saulhdev.feeder.ui.components.OverflowMenu
import com.saulhdev.feeder.ui.components.PreferenceGroupHeading
import com.saulhdev.feeder.ui.components.SourceItem
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.BookBookmark
import com.saulhdev.feeder.ui.icons.phosphor.Bookmarks
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowDown
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowUp
import com.saulhdev.feeder.ui.icons.phosphor.Megaphone
import com.saulhdev.feeder.ui.icons.phosphor.Plus
import com.saulhdev.feeder.ui.navigation.LocalNavController
import com.saulhdev.feeder.ui.navigation.NavRoute
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.FILE_DATETIME_FORMAT
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.SourceListViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import okhttp3.internal.toLongOrDefault
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SourceListPage(
    viewModel: SourceListViewModel = koinNeoViewModel(),
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val localTime =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(FILE_DATETIME_FORMAT)
    // TODO reconsider
    val coroutineScope: ApplicationCoroutineScope by inject(ApplicationCoroutineScope::class.java)
    val state by viewModel.state.collectAsState()
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val sourceId = remember { mutableLongStateOf(-1L) }

    val opmlExporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/opml"),
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.exportOpml(
                        uri,
                        state.tagsSourcesMap,
                    )
                }
            }
        }

    val opmlImporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.importOpml(uri)
                }
            }
        }

    val bookmarksExporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/opml"),
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.exportBookmarks(
                        context,
                        uri,
                        state.bookmarked,
                    )
                }
            }
        }

    val bookmarksImporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.importBookmarks(
                        context,
                        uri,
                    )
                }
            }
        }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        listPane = {
            AnimatedPane {
                ViewWithActionBar(
                    title = stringResource(id = R.string.title_sources),
                    showBackButton = false,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = {
                                navController.navigate(NavRoute.SourceAdd)
                            },
                            modifier = Modifier.padding(16.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                        ) {
                            Icon(
                                imageVector = Phosphor.Plus,
                                contentDescription = stringResource(id = R.string.add_feed),
                            )
                        }
                    },
                    actions = {
                        OverflowMenu {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Phosphor.CloudArrowDown,
                                        contentDescription = stringResource(id = R.string.sources_import_opml),
                                    )
                                },
                                onClick = {
                                    hideMenu()
                                    opmlImporter.launch(
                                        arrayOf(
                                            "text/plain",
                                            "text/xml",
                                            "text/opml",
                                            "*/*",
                                        ),
                                    )
                                },
                                text = { Text(text = stringResource(id = R.string.sources_import_opml)) },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Phosphor.CloudArrowUp,
                                        contentDescription = stringResource(id = R.string.sources_export_opml),
                                    )
                                },
                                onClick = {
                                    hideMenu()
                                    opmlExporter.launch("NF-$localTime.opml")
                                },
                                text = { Text(text = stringResource(id = R.string.sources_export_opml)) },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Phosphor.Bookmarks,
                                        contentDescription = stringResource(id = R.string.sources_import_bookmarks),
                                    )
                                },
                                onClick = {
                                    hideMenu()
                                    bookmarksImporter.launch(
                                        arrayOf(
                                            "text/plain",
                                            "text/xml",
                                            "text/bkm",
                                            "*/*",
                                        ),
                                    )
                                },
                                text = { Text(text = stringResource(id = R.string.sources_import_bookmarks)) },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Phosphor.BookBookmark,
                                        contentDescription = stringResource(id = R.string.sources_export_bookmarks),
                                    )
                                },
                                onClick = {
                                    hideMenu()
                                    bookmarksExporter.launch("NF-$localTime.bkm")
                                },
                                text = { Text(text = stringResource(id = R.string.sources_export_bookmarks)) },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Phosphor.Megaphone,
                                        contentDescription = stringResource(id = R.string.suggested_feeds),
                                    )
                                },
                                onClick = {
                                    hideMenu()
                                    navController.navigate(NavRoute.SuggestedFeeds)
                                },
                                text = { Text(text = stringResource(id = R.string.suggested_feeds)) },
                            )
                        }
                    },
                ) { paddingValues ->
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                top = paddingValues.calculateTopPadding(),
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            PreferenceGroupHeading(heading = stringResource(id = R.string.enabled))
                        }
                        items(state.enabledSources, key = { it.id }) { item ->
                            SourceItem(
                                modifier = Modifier.animateItem(),
                                source = item,
                                onClick = {
                                    scope.launch {
                                        paneNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            item.id,
                                        )
                                    }
                                },
                                onSwitch = {
                                    viewModel.updateFeed(
                                        it.copy(isEnabled = false),
                                        false,
                                    )
                                },
                            )
                        }
                        item {
                            PreferenceGroupHeading(heading = stringResource(id = R.string.disabled))
                        }
                        items(state.disabledSources, key = { it.id }) { item ->
                            SourceItem(
                                modifier = Modifier.animateItem(),
                                source = item,
                                onClick = {
                                    scope.launch {
                                        paneNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            item.id,
                                        )
                                    }
                                },
                                onSwitch = {
                                    viewModel.updateFeed(
                                        it.copy(isEnabled = true),
                                        true,
                                    )
                                },
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        },
        detailPane = {
            sourceId.longValue =
                paneNavigator.currentDestination
                    ?.takeIf { it.pane == this.paneRole }
                    ?.contentKey
                    .toString()
                    .toLongOrDefault(-1L)

            sourceId.longValue.takeIf { it != -1L }?.let { id ->
                AnimatedPane {
                    SourceEditPage(id) {
                        scope.launch {
                            paneNavigator.navigateBack()
                        }
                    }
                }
            }
        },
    )
}
