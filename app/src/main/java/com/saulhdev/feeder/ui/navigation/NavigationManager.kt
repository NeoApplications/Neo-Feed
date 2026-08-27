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

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.GearSix
import com.saulhdev.feeder.ui.icons.phosphor.Graph
import com.saulhdev.feeder.ui.icons.phosphor.Info
import com.saulhdev.feeder.ui.pages.AboutPage
import com.saulhdev.feeder.ui.pages.ArticleListPage
import com.saulhdev.feeder.ui.pages.ArticlePage
import com.saulhdev.feeder.ui.pages.ChangelogPage
import com.saulhdev.feeder.ui.pages.BlockedWordsPage
import com.saulhdev.feeder.ui.pages.LicensePage
import com.saulhdev.feeder.ui.pages.MainPage
import com.saulhdev.feeder.ui.pages.MastodonAddPage
import com.saulhdev.feeder.ui.pages.MastodonCallbackPage
import com.saulhdev.feeder.ui.pages.PreferencesPage
import com.saulhdev.feeder.ui.pages.SourceAddPage
import com.saulhdev.feeder.ui.pages.SourceListPage
import com.saulhdev.feeder.ui.views.ComposeWebView
import kotlinx.serialization.Serializable

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

const val NAV_BASE = "nf-navigation://androidx.navigation/"

@Composable
fun NavigationManager(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = NavRoute.Main(),
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 2 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 2 } },
        ) {

            composable<NavRoute.Main>(
                deepLinks = listOf(navDeepLink { uriPattern = "$NAV_BASE${Routes.MAIN}/{page}" })
            ) {
                val args = it.toRoute<NavRoute.Main>()
                MainPage(args.page)
            }
            composable<NavRoute.About> { AboutPage() }
            composable<NavRoute.License> { LicensePage() }
            composable<NavRoute.Changelog> { ChangelogPage() }
            composable<NavRoute.SourceAdd> { SourceAddPage() }
            composable<NavRoute.BlockedWords> { BlockedWordsPage() }
            composable<NavRoute.MastodonAdd> { MastodonAddPage() }
            composable<NavRoute.MastodonCallback>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "nf-mastodon://callback?code={code}&state={state}"
                    }
                )
            ) {
                val args = it.toRoute<NavRoute.MastodonCallback>()
                MastodonCallbackPage(args.code, args.state)
            }
            composable<NavRoute.WebView>(
                deepLinks = listOf(navDeepLink { uriPattern = "$NAV_BASE${Routes.WEB_VIEW}/{url}" })
            ) {
                val args = it.toRoute<NavRoute.WebView>()
                ComposeWebView(args.url)
            }
            composable<NavRoute.ArticleView>(
                deepLinks = listOf(navDeepLink {
                    uriPattern = "$NAV_BASE${Routes.ARTICLE_VIEW}/{uuid}"
                })
            ) {
                val args = it.toRoute<NavRoute.ArticleView>()
                ArticlePage(args.uuid)
            }
        }
    }
}

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val WEB_VIEW = "web_view"
    const val ARTICLE_VIEW = "article_page"
}

sealed class NavItem(
    val title: Int,
    val icon: ImageVector,
    val content: @Composable () -> Unit = {}
) {
    data object Feed :
        NavItem(R.string.home, Phosphor.Info, {
            ArticleListPage()
        })

    data object Settings :
        NavItem(R.string.title_settings, Phosphor.GearSix, {
            PreferencesPage()
        })

    data object Sources :
        NavItem(R.string.title_sources, Phosphor.Graph, {
            SourceListPage()
        })
}

@Serializable
open class NavRoute {
    @Serializable
    data object About : NavRoute()

    @Serializable
    data object SourceAdd : NavRoute()

    @Serializable
    data object BlockedWords : NavRoute()

    @Serializable
    data object MastodonAdd : NavRoute()

    @Serializable
    data class MastodonCallback(val code: String = "", val state: String = "") : NavRoute()

    @Serializable
    data class ArticleView(val uuid: String = "") : NavRoute()

    @Serializable
    data object Changelog : NavRoute()

    @Serializable
    data class Main(val page: Int = 0) : NavRoute()

    @Serializable
    data object License : NavRoute()

    @Serializable
    data class WebView(val url: String = "") : NavRoute()
}