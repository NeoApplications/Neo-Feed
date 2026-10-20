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

package com.saulhdev.feeder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Compass
import com.saulhdev.feeder.ui.icons.phosphor.GearSix
import com.saulhdev.feeder.ui.icons.phosphor.Graph
import com.saulhdev.feeder.ui.pages.ArticleListPage
import com.saulhdev.feeder.ui.pages.PreferencesPage
import com.saulhdev.feeder.ui.pages.SourcesPluginsPage
import kotlinx.serialization.Serializable

const val NAV_BASE = "nf-navigation://androidx.navigation/"

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val WEB_VIEW = "web_view"
    const val ARTICLE_VIEW = "article_page"
}

sealed class NavItem(
    val title: Int,
    val icon: ImageVector,
    val content: @Composable () -> Unit = {},
) {
    data object Feed :
        NavItem(
            R.string.home,
            Phosphor.Compass,
            {
                ArticleListPage()
            },
        )

    class Settings(onNavigate: (NavKey) -> Unit) :
        NavItem(
            R.string.title_settings,
            Phosphor.GearSix,
            {
                PreferencesPage(onNavigate = onNavigate)
            },
        )

    class Sources(onNavigate: (NavKey) -> Unit) :
        NavItem(
            R.string.title_sources,
            Phosphor.Graph,
            {
                SourcesPluginsPage(onNavigate = onNavigate)
            },
        )
}

@Serializable
open class NavRoute : NavKey {
    @Serializable data object About : NavRoute()

    @Serializable data object SourceAdd : NavRoute()

    @Serializable data object BlockedWords : NavRoute()

    @Serializable data object Plugins : NavRoute()

    @Serializable data object MastodonAdd : NavRoute()

    @Serializable
    data class MastodonCallback(val code: String = "", val state: String = "") : NavRoute()

    @Serializable data class ArticleView(val uuid: String = "") : NavRoute()

    @Serializable data object Changelog : NavRoute()

    @Serializable data class Main(val page: Int = 0) : NavRoute()

    @Serializable data object License : NavRoute()

    @Serializable data object OpenSource : NavRoute()

    @Serializable data class WebView(val url: String = "") : NavRoute()
}
