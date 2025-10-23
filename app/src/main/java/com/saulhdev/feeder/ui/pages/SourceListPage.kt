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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.manager.models.exportOpml
import com.saulhdev.feeder.manager.models.importOpml
import com.saulhdev.feeder.ui.components.FeedItem
import com.saulhdev.feeder.ui.components.OverflowMenu
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.components.dialog.ActionsDialogUI
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowDown
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowUp
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
    val localTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        .format(FILE_DATETIME_FORMAT)
    // TODO reconsider
    val coroutineScope: ApplicationCoroutineScope by inject(ApplicationCoroutineScope::class.java)
    val showDialog = remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val removeItem: MutableState<Feed?> = remember {
        mutableStateOf(state.allSources.firstOrNull())
    }
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val sourceId = remember { mutableLongStateOf(-1L) }

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/opml")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                context.contentResolver.exportOpml(
                    uri,
                    state.tagsSourcesMap
                )
            }
        }
    }

    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                context.contentResolver.importOpml(uri)
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
                            shape = MaterialTheme.shapes.extraLarge
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
                                            "*/*"
                                        )
                                    )
                                },
                                text = { Text(text = stringResource(id = R.string.sources_import_opml)) }
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
                                    opmlExporter.launch("NF-${localTime}.opml")
                                },
                                text = { Text(text = stringResource(id = R.string.sources_export_opml)) }
                            )
                        }
                    }
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = paddingValues.calculateTopPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.allSources, key = { it.id }) { item ->
                            FeedItem(
                                feed = item,
                                onClickAction = {
                                    scope.launch {
                                        paneNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            item.id
                                        )
                                    }
                                },
                                onRemoveAction = {
                                    removeItem.value = item
                                    showDialog.value = true
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }

                    if (showDialog.value) {
                        Dialog(
                            onDismissRequest = { showDialog.value = false },
                            DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            ActionsDialogUI(
                                titleText = stringResource(id = R.string.remove_title),
                                messageText = stringResource(
                                    id = R.string.remove_desc,
                                    removeItem.value!!.title
                                ),
                                openDialogCustom = showDialog,
                                primaryText = stringResource(id = android.R.string.ok),
                                primaryAction = {
                                    viewModel.deleteFeed(removeItem.value!!)
                                }
                            )
                        }
                    }
                }
            }
        },
        detailPane = {
            sourceId.longValue = paneNavigator.currentDestination
                ?.takeIf { it.pane == this.paneRole }?.contentKey
                .toString().toLongOrDefault(-1L)

            sourceId.longValue.takeIf { it != -1L }?.let { id ->
                AnimatedPane {
                    SourceEditPage(id) {
                        scope.launch {
                            paneNavigator.navigateBack()
                        }
                    }
                }
            }
        }
    )
}
