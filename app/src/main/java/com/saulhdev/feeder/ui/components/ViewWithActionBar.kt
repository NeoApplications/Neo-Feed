/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.ui.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.ArrowLeft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWithActionBar(
    title: String,
    titleSize: TextUnit = 18.sp,
    subTitle: String = "",
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    showBackButton: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    onBackAction: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = titleSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subTitle.isNotEmpty()) {
                            Text(
                                text = subTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                },
                navigationIcon = {
                    if (showBackButton) {
                        val backDispatcher =
                            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                        IconButton(
                            onClick = {
                                onBackAction?.invoke()
                                    ?: backDispatcher?.onBackPressed()
                            }
                        ) {
                            Icon(
                                imageVector = Phosphor.ArrowLeft,
                                contentDescription = stringResource(id = R.string.go_back),
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.Center,
        content = content,
        modifier = modifier
    )
}

@Composable
fun RoundButton(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    description: String = "",
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier.size(size = size),
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
        )
    }
}
