/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.saulhdev.feeder.data.entity

import androidx.compose.runtime.Immutable

@Immutable
data class SourceEditViewState(
    val title: String = "",
    val url: String = "",
    val tag: String = "",
    val fullTextByDefault: Boolean = true,
    val isEnabled: Boolean = true,
    val sourceType: String = "rss",
    val requireLink: Boolean = false,
    val requireImage: Boolean = false,
    val excludeReplies: Boolean = true,
)