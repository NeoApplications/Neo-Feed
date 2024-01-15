/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceGroup(
    heading: String? = null,
    textAlignment: Alignment.Horizontal = Alignment.Start,
    prefs: List<Any>,
    onPrefDialog: (Any) -> Unit = {}
) {
    PreferenceGroup(heading = heading, textAlignment = textAlignment) {
        val size = prefs.size
        prefs.forEachIndexed { i, it ->
            PreferenceBuilder(it, onPrefDialog, i, size)
            if (i + 1 < size) Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
fun PreferenceGroup(
    heading: String? = null,
    textAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    PreferenceGroupHeading(heading, textAlignment)
    val columnModifier = Modifier
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.primary
    ) {

        Surface(color = Color.Transparent) {
            Column(modifier = columnModifier) {
                content()
            }
        }
    }
}

@Composable
fun PreferenceGroupHeading(
    heading: String? = null,
    textAlignment: Alignment.Horizontal = Alignment.Start
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = textAlignment
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}