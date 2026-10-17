/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

package com.saulhdev.feeder.ui.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.data.content.TwoStatePref
import com.saulhdev.feeder.ui.components.dialog.BaseDialog
import com.saulhdev.feeder.ui.components.dialog.DialogNegativeButton
import com.saulhdev.feeder.ui.components.dialog.DialogPositiveButton
import com.saulhdev.feeder.ui.components.dialog.ListItemWithRadioButton
import com.saulhdev.feeder.ui.theme.GroupItemShape
import com.saulhdev.feeder.utils.extensions.addIf
import com.saulhdev.feeder.utils.extensions.blockShadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TwoStatePreference(
    modifier: Modifier = Modifier,
    pref: TwoStatePref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val (checked, check) = remember(pref) { mutableStateOf(pref.getValue()) }
    val prefState by pref.getState()
    val selectedValue2 by pref.getState2()

    val openDialog = remember { mutableStateOf(false) }

    LaunchedEffect(prefState) {
        check(prefState)
    }

    val onToggle = { newValue: Boolean ->
        onCheckedChange(newValue)
        check(newValue)
        coroutineScope.launch { pref.setValue(newValue) }
    }

    val onClick = {
        if (isEnabled && pref.entries.isNotEmpty()) {
            openDialog.value = true
        }
    }

    val summaryText = pref.entries[selectedValue2]

    TwoStatePreference(
        title = pref.titleId,
        modifier = modifier,
        isEnabled = isEnabled,
        isChecked = checked,
        summary = pref.summaryId,
        summaryText = summaryText,
        icon = pref.icon,
        index = index,
        groupSize = groupSize,
        onclick = {
            onClick()
        },
        onValueChange = {
            onToggle(it)
        },
    )

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            TwoStatePrefDialogUI(
                titleId = pref.titleId,
                entries = pref.entries,
                selectedValue = selectedValue2,
                openDialogCustom = openDialog,
                onConfirm = { selectedKey ->
                    coroutineScope.launch {
                        pref.setValue2(selectedKey)
                    }
                },
            )
        }
    }
}

@Composable
fun TwoStatePrefDialogUI(
    titleId: Int,
    entries: Map<String, String>,
    selectedValue: String,
    openDialogCustom: MutableState<Boolean>,
    onConfirm: (String) -> Unit,
) {
    var selected by
        remember(selectedValue, entries) {
            mutableStateOf(
                if (entries.containsKey(selectedValue)) selectedValue
                else entries.keys.firstOrNull() ?: ""
            )
        }
    val entryPairs = remember(entries) { entries.toList() }

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
            Text(
                text = stringResource(id = titleId),
                style = MaterialTheme.typography.titleLarge,
            )
            LazyColumn(
                modifier =
                    Modifier.padding(top = 16.dp, bottom = 8.dp).weight(1f, false).blockShadow()
            ) {
                items(items = entryPairs, key = { it.first }) { item ->
                    ListItemWithRadioButton(
                        title = item.second,
                        selected = selected == item.first,
                        radioButton = true,
                        index = entryPairs.indexOf(item),
                        groupSize = entryPairs.size,
                        enabled = true,
                        onClick = {
                            selected = item.first
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                DialogNegativeButton(onClick = { openDialogCustom.value = false })
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        openDialogCustom.value = false
                        onConfirm(selected)
                    },
                )
            }
        }
    }
}

@Composable
fun TwoStatePreference(
    title: Int,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isChecked: Boolean,
    summary: Int = -1,
    summaryText: String? = null,
    icon: ImageVector? = null,
    iconId: Int = 0,
    index: Int = 0,
    groupSize: Int = 1,
    onclick: () -> Unit = {},
    onValueChange: (Boolean) -> Unit = {},
) {
    ListItem(
        leadingContent =
            when {
                icon != null -> {
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(id = title),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                iconId != 0 -> {
                    {
                        Icon(
                            painter = painterResource(id = iconId),
                            contentDescription = "",
                            modifier = Modifier.clip(CircleShape).size(32.dp),
                        )
                    }
                }

                else -> null
            },
        content = {
            Text(
                text = stringResource(id = title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            val displaySummary =
                summaryText ?: if (summary != -1) stringResource(id = summary) else null
            if (displaySummary != null) {
                Text(
                    modifier =
                        Modifier.addIf(!isEnabled) {
                            alpha(0.3f)
                        },
                    text = displaySummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(
                    modifier = Modifier.height(30.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = 1.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    modifier = Modifier.height(24.dp),
                    checked = isChecked,
                    onCheckedChange = {
                        onValueChange(it)
                    },
                    enabled = isEnabled,
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        onClick = onclick,
        shapes = ListItemDefaults.shapes(shape = GroupItemShape(index, groupSize - 1)),
        colors =
            ListItemDefaults.colors(
                containerColor =
                    if (isChecked) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
    )
}
