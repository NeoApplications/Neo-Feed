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

package com.saulhdev.feeder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.utils.extensions.addIf
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Asterisk
import com.saulhdev.feeder.ui.icons.phosphor.Circle
import com.saulhdev.feeder.ui.icons.phosphor.CheckCircle
import com.saulhdev.feeder.ui.icons.phosphor.SortDescending

@Composable
fun SelectChip(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean = false,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    alwaysShowIcon: Boolean = true,
    onSelected: () -> Unit = {},
) {
    val categoryChipTransitionState = selectableChipTransition(selected = checked)
    val icon by remember(checked) {
        mutableStateOf(
            if (checked) Phosphor.CheckCircle
            else Phosphor.Circle
        )
    }

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(categoryChipTransitionState.cornerRadius),
        border = null,
        selected = checked,
        leadingIcon = {
            if (alwaysShowIcon) Icon(
                imageVector = icon,
                contentDescription = null,
            )
            else AnimatedVisibility(
                visible = checked,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
        onClick = { onSelected() },
        label = {
            Text(text = text)
        }
    )
}

@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector?,
    positive: Boolean = true,
    fullWidth: Boolean = false,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        label = {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .addIf(fullWidth) {
                        fillMaxWidth()
                    },
                text = text,
                textAlign = TextAlign.Center,
            )
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = text
                )
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = null,
        onClick = onClick
    )
}

@Composable
fun SortFilterChip(
    notModified: Boolean,
    fullWidth: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopStart,
    ) {
        ActionChip(
            text = stringResource(id = R.string.sort_filter),
            icon = Phosphor.SortDescending,
            fullWidth = fullWidth,
            onClick = onClick
        )

        if (!notModified) {
            Icon(
                modifier = Modifier.align(Alignment.TopEnd),
                imageVector = Phosphor.Asterisk,
                contentDescription = stringResource(id = R.string.state_modified),
            )
        }
    }
}

@Composable
fun ChipsSwitch(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                MaterialTheme.shapes.medium
            )
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            },
            leadingIcon = {
                Icon(
                    imageVector = firstIcon,
                    contentDescription = stringResource(id = firstTextId)
                )
            },
            label = {
                Text(
                    text = stringResource(id = firstTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                )
            }
        )
        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = !firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            },
            label = {
                Text(
                    text = stringResource(id = secondTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = secondIcon,
                    contentDescription = stringResource(id = secondTextId)
                )
            }
        )
    }
}

@Composable
fun DeSelectAll(
    completeList: List<String>,
    selectedList: SnapshotStateList<String>,
) {
    if (completeList.size > 1) Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = selectedList.isNotEmpty(),
            enter = fadeIn()
                    + slideInHorizontally { w -> -w }
                    + expandHorizontally(expandFrom = Alignment.Start, clip = false),
            exit = fadeOut()
                    + slideOutHorizontally { w -> -w }
                    + shrinkHorizontally(shrinkTowards = Alignment.Start, clip = false),
        ) {
            FilledTonalButton(onClick = { selectedList.clear() }) {
                Text(text = stringResource(id = R.string.select_all))
            }
        }
        AnimatedVisibility(
            visible = selectedList.size != completeList.size,
            enter = fadeIn()
                    + slideInHorizontally { w -> w }
                    + expandHorizontally(expandFrom = Alignment.End, clip = false),
            exit = fadeOut()
                    + slideOutHorizontally { w -> w }
                    + shrinkHorizontally(shrinkTowards = Alignment.End, clip = false),
        ) {
            FilledTonalButton(onClick = { selectedList.addAll(completeList - selectedList) }) {
                Text(text = stringResource(id = R.string.deselect_all))
            }
        }
    }
}

private enum class SelectionState { Unselected, Selected }

class SelectableChipTransition constructor(
    cornerRadius: State<Dp>,
) {
    val cornerRadius by cornerRadius
}

@Composable
fun selectableChipTransition(selected: Boolean): SelectableChipTransition {
    val transition = updateTransition(
        targetState = if (selected) SelectionState.Selected else SelectionState.Unselected,
        label = "chip_transition"
    )
    val corerRadius = transition.animateDp(label = "chip_corner") { state ->
        when (state) {
            SelectionState.Unselected -> 8.dp
            SelectionState.Selected   -> 16.dp
        }
    }
    return remember(transition) {
        SelectableChipTransition(corerRadius)
    }
}
