/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.data.db.models.Feed

@Composable
fun SourceItem(
    source: Feed,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSwitch: (Feed) -> Unit = {},
    onClick: (Feed) -> Unit = {},
) {
    val (isEnabled, enable) =
        remember(source.isEnabled) {
            mutableStateOf(source.isEnabled)
        }
    val backgroundColor by
        animateColorAsState(
            targetValue =
                if (isEnabled) MaterialTheme.colorScheme.surfaceContainerHighest
                else MaterialTheme.colorScheme.surfaceContainerLowest,
            label = "backgroundColor",
        )

    ListItem(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(source) },
        shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium),
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        content = {
            Text(text = source.title)
        },
        supportingContent = {
            Text(text = source.url.toString())
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
                    checked = isEnabled,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
                    onCheckedChange = {
                        enable(!isEnabled)
                        onSwitch(source)
                    },
                )
            }
        },
    )
}
