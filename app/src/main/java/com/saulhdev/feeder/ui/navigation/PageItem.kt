/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team <saulhdev@hotmail.com>
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

package com.saulhdev.feeder.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Copyleft
import com.saulhdev.feeder.ui.icons.phosphor.ListDashes

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector,
    val route: NavRoute,
) {
    companion object {
        val AboutLicense = PageItem(
            titleId = R.string.about_licenses,
            icon = Phosphor.Copyleft,
            route = NavRoute.License,
        )
        val OpenSource = PageItem(
            titleId = R.string.about_open_source,
            icon = Phosphor.Copyleft,
            route = NavRoute.OpenSource,
        )
        val AboutChangelog = PageItem(
            titleId = R.string.about_changelog,
            icon = Phosphor.ListDashes,
            route = NavRoute.Changelog,
        )
    }
}