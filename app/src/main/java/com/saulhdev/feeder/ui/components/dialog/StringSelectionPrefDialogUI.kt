/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saulhdev.feeder.ui.components.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.data.content.StringSelectionPref
import com.saulhdev.feeder.ui.theme.GroupItemShape
import com.saulhdev.feeder.utils.extensions.blockShadow
import kotlinx.coroutines.launch

@Composable
fun StringSelectionPrefDialogUI(
    pref: StringSelectionPref,
    openDialogCustom: MutableState<Boolean>,
) {
    var selected by remember { mutableStateOf(pref.getValue()) }
    val entryPairs = pref.entries.toList()

    val scope = rememberCoroutineScope()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier.blockShadow().padding(all = 8.dp).weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(items = entryPairs, key = { it.first }) {
                    ListItemWithRadioButton(
                        title = it.second,
                        selected = selected == it.first,
                        radioButton = true,
                        index = entryPairs.indexOf(it),
                        groupSize = entryPairs.size,
                        enabled = true,
                        onClick = {
                            selected = it.first
                        },
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                DialogNegativeButton(onClick = { openDialogCustom.value = false })
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        scope.launch {
                            openDialogCustom.value = false
                            pref.setValue(selected)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun ListItemWithRadioButton(
    modifier: Modifier = Modifier,
    title: String,
    summary: String = "",
    index: Int = 0,
    groupSize: Int = 1,
    radioButton: Boolean = true,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    startIcon: (@Composable () -> Unit)? = null,
) {
    val actualContainerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    containerColor
                },
            label = "containerColor",
        )

    ListItem(
        modifier = modifier.fillMaxWidth(),
        leadingContent = startIcon,
        enabled = enabled,
        onClick = onClick,
        shapes = ListItemDefaults.shapes(shape = GroupItemShape(index, groupSize - 1)),
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        },
        supportingContent =
            if (summary.isNotEmpty()) {
                {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (!enabled) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            } else null,
        trailingContent =
            if (radioButton) {
                {
                    RadioButton(
                        selected = selected,
                        enabled = enabled,
                        onClick = null,
                        modifier = Modifier.size(24.dp),
                        colors =
                            RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledSelectedColor =
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledUnselectedColor =
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            ),
                    )
                }
            } else null,
        colors =
            ListItemDefaults.colors(
                containerColor = actualContainerColor,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
}
