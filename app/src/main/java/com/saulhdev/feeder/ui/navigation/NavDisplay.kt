package com.saulhdev.feeder.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.saulhdev.feeder.ui.pages.AboutPage
import com.saulhdev.feeder.ui.pages.ArticlePage
import com.saulhdev.feeder.ui.pages.BlockedWordsPage
import com.saulhdev.feeder.ui.pages.ChangelogPage
import com.saulhdev.feeder.ui.pages.LicensePage
import com.saulhdev.feeder.ui.pages.MainPage
import com.saulhdev.feeder.ui.pages.MastodonCallbackPage
import com.saulhdev.feeder.ui.pages.OpenSourcePage
import com.saulhdev.feeder.ui.pages.PluginsPage
import com.saulhdev.feeder.ui.pages.SourceAddPage
import com.saulhdev.feeder.ui.views.ComposeWebView

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(tween(600)) { it } togetherWith
                slideOutHorizontally(tween(600)) { -it }
        },
        popTransitionSpec = {
            slideInHorizontally(tween(600)) { -it } togetherWith
                slideOutHorizontally(tween(600)) { it }
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(tween(600)) { -it } togetherWith
                slideOutHorizontally(tween(600)) { it }
        },
        entryProvider =
            entryProvider {
                entry<NavRoute.Main> { route ->
                    MainPage(route.page) { nav ->
                        backStack.navigateUnique(nav)
                    }
                }
                entry<NavRoute.About> {
                    AboutPage { nav ->
                        backStack.navigateUnique(nav)
                    }
                }
                entry<NavRoute.License> { LicensePage() }
                entry<NavRoute.OpenSource> { OpenSourcePage() }
                entry<NavRoute.Changelog> { ChangelogPage() }
                entry<NavRoute.SourceAdd> {
                    SourceAddPage {
                        backStack.removeLastOrNull()
                    }
                }
                entry<NavRoute.BlockedWords> {
                    BlockedWordsPage {
                        backStack.removeLastOrNull()
                    }
                }
                entry<NavRoute.Plugins> { PluginsPage() }
                entry<NavRoute.MastodonCallback> { route ->
                    MastodonCallbackPage(route.code, route.state) {
                        backStack.removeLastOrNull()
                    }
                }
                entry<NavRoute.WebView> { route ->
                    ComposeWebView(route.url)
                }
                entry<NavRoute.ArticleView> { route ->
                    ArticlePage(route.uuid)
                }
            },
    )
}

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.slideInEntry(
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata =
            metadata +
                NavDisplay.transitionSpec {
                    slideInHorizontally(tween(600)) { it } togetherWith
                        slideOutHorizontally(tween(600)) { -it }
                } +
                NavDisplay.popTransitionSpec {
                    slideInHorizontally(tween(600)) { -it } togetherWith
                        slideOutHorizontally(tween(600)) { it }
                } +
                NavDisplay.predictivePopTransitionSpec {
                    slideInHorizontally(tween(600)) { -it } togetherWith
                        slideOutHorizontally(tween(600)) { it }
                }
    ) {
        content(it)
    }
}

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.fadeInEntry(
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata =
            metadata +
                NavDisplay.transitionSpec {
                    fadeIn(tween(400), 0.3f) togetherWith fadeOut(tween(400), 0.3f)
                } +
                NavDisplay.popTransitionSpec {
                    fadeIn(tween(400), 0.3f) togetherWith fadeOut(tween(400), 0.3f)
                } +
                NavDisplay.predictivePopTransitionSpec {
                    fadeIn(tween(400), 0.3f) togetherWith fadeOut(tween(400), 0.3f)
                }
    ) {
        content(it)
    }
}

fun MutableList<NavKey>.navigateUnique(key: NavKey) {
    val lastKey = lastOrNull()
    if (lastKey != null && lastKey == key) return
    removeAll { existing -> existing::class == key::class }
    add(key)
}

fun MutableList<NavKey>.navigate(key: NavKey) {
    val lastKey = lastOrNull()
    if (lastKey != null && lastKey == key) return
    remove(key)
    add(key)
}
