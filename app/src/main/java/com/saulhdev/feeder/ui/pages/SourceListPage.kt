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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.components.SourceItem
import com.saulhdev.feeder.ui.components.preferences.PreferenceGroupHeading
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Plus
import com.saulhdev.feeder.ui.navigation.LocalNavController
import com.saulhdev.feeder.ui.navigation.NavRoute
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.SourceListViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SourceListPage(viewModel: SourceListViewModel = koinNeoViewModel()) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val sourceId = remember { mutableLongStateOf(-1L) }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        listPane = {
            AnimatedPane {
                Scaffold(
                    floatingActionButtonPosition = FabPosition.Center,
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
                ) { _ ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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

            sourceId.longValue
                .takeIf { it != -1L }
                ?.let { id ->
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
