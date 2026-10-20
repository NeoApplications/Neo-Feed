/*
 * This file is part of Neo Feeder
 * Copyright (c) 2022   Neo Feeder Team
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

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saulhdev.feeder.data.content.StringSetPref

@Composable
fun StringSetPreference(
    modifier: Modifier = Modifier,
    pref: StringSetPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onNavigate: () -> Unit,
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        startWidget = {
            Icon(
                imageVector = pref.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        onClick = {
            pref.route?.let { onNavigate() } ?: pref.onClick?.invoke()
        },
    )
}
