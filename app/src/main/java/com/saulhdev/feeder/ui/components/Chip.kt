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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.CheckCircle
import com.saulhdev.feeder.ui.icons.phosphor.Circle
import com.saulhdev.feeder.utils.extensions.addIf

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
    val selectionCornerRadius by animateDpAsState(
        when {
            checked -> 4.dp
            else    -> 16.dp
        }
    )
    val icon by remember(checked) {
        mutableStateOf(
            if (checked) Phosphor.CheckCircle
            else Phosphor.Circle
        )
    }

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
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
fun ChipsSwitch(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth(),
        space = 24.dp,
    ) {
        SegmentedTabButton(
            text = stringResource(id = firstTextId),
            icon = firstIcon,
            selected = { firstSelected },
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            }
        )
        SegmentedTabButton(
            text = stringResource(id = secondTextId),
            icon = secondIcon,
            selected = { !firstSelected },
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
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
