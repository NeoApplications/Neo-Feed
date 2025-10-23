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


package com.saulhdev.feeder.ui.pages


import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import com.saulhdev.feeder.BuildConfig
import com.saulhdev.feeder.R
import com.saulhdev.feeder.utils.extensions.launchView
import com.saulhdev.feeder.ui.navigation.PageItem
import com.saulhdev.feeder.ui.components.ContributorRow
import com.saulhdev.feeder.ui.components.LinkItem
import com.saulhdev.feeder.ui.components.PagePreference
import com.saulhdev.feeder.ui.components.PreferenceGroupHeading
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.BracketsSquare
import com.saulhdev.feeder.ui.icons.phosphor.GithubLogo
import com.saulhdev.feeder.ui.icons.phosphor.Megaphone
import com.saulhdev.feeder.ui.icons.phosphor.TelegramLogo
import com.saulhdev.feeder.ui.theme.kingthingsPrintingkit
import com.saulhdev.feeder.utils.urlDecode
import java.io.InputStream

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AboutPage() {
    val title = stringResource(id = R.string.title_about)
    ViewWithActionBar(
        title = title,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        leadingContent = {
                            ResourcesCompat.getDrawable(
                                LocalContext.current.resources,
                                R.mipmap.ic_launcher,
                                LocalContext.current.theme
                            )?.let { drawable ->
                                val bitmap = Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .requiredSize(84.dp)
                                        .clip(MaterialTheme.shapes.large)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = kingthingsPrintingkit,
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_version) + ": "
                                            + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BuildConfig.APPLICATION_ID,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(links) { link ->
                            LinkItem(
                                icon = link.icon,
                                label = stringResource(id = link.labelResId),
                                url = link.url,
                            )
                        }
                    }
                }
            }
            item {
                PreferenceGroupHeading(heading = stringResource(id = R.string.about_team))
            }
            itemsIndexed(contributors) { i, it ->
                ContributorRow(
                    nameId = it.name,
                    roleId = it.descriptionRes,
                    photoUrl = it.photoUrl,
                    url = it.webpage,
                    index = i,
                    groupSize = contributors.size
                )
            }
            item {
                PreferenceGroupHeading(heading = stringResource(id = R.string.about_build_information))
            }
            itemsIndexed(listOf(PageItem.AboutLicense, PageItem.AboutChangelog)) { i, it ->
                PagePreference(
                    titleId = it.titleId,
                    icon = it.icon,
                    route = it.route,
                    index = i,
                    groupSize = 2
                )
            }
        }
    }
}


private data class Link(
    val icon: ImageVector,
    @StringRes val labelResId: Int,
    val url: String
)

private data class TeamMember(
    @StringRes val name: Int,
    @StringRes val descriptionRes: Int,
    val photoUrl: String,
    val webpage: String
)

private val links = listOf(
    Link(
        icon = Phosphor.GithubLogo,
        labelResId = R.string.about_source_code,
        url = "https://github.com/NeoApplications/Neo-Feed"
    ),
    Link(
        icon = Phosphor.Megaphone,
        labelResId = R.string.about_channel,
        url = "https://t.me/neo_applications"
    ),
    Link(
        icon = Phosphor.TelegramLogo,
        labelResId = R.string.about_community_telegram,
        url = "https://t.me/neo_launcher"
    ),
    Link(
        icon = Phosphor.BracketsSquare,
        labelResId = R.string.about_community_matrix,
        url = "https://matrix.to/#/#neo-launcher:matrix.org"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.about_developer,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/saulhdev"
    ),
    TeamMember(
        name = R.string.about_developer2,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)

@Composable
fun LicensePage() {
    ViewWithActionBar(
        title = stringResource(R.string.about_licenses),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 32.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            item {
                PreferencesWebView(url = "file:///android_asset/license.htm")

            }
        }
    }
}

@Composable
fun ChangelogPage() {
    ViewWithActionBar(
        title = stringResource(R.string.about_changelog),
    ) {
        PreferencesWebView(url = "file:///android_asset/changelog.htm")
        Spacer(modifier = Modifier.requiredHeight(50.dp))
    }
}

@Composable
fun PreferencesWebView(url: String) {

    val cssFile = "light.css"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (url.startsWith("file:///android_asset")) {
                            try {
                                settings.javaScriptEnabled = true
                                val inputStream: InputStream = context.assets.open(cssFile)
                                val buffer = ByteArray(inputStream.available())
                                inputStream.read(buffer)
                                inputStream.close()
                                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                                loadUrl(
                                    "javascript:(function() { " +
                                            "var head  = document.getElementsByTagName('head')[0];" +
                                            "var style = document.createElement('style');" +
                                            "style.type = 'text/css';" +
                                            "style.innerHTML =  window.atob('" + encoded + "');" +
                                            "head.appendChild(style);" +
                                            "})()"
                                )
                                settings.javaScriptEnabled = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        super.onPageFinished(view, url.urlDecode())
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        if (url.contains("file://")) {
                            view.loadUrl(url)
                        } else {
                            try {
                                context.launchView(url)
                            } catch (e: ActivityNotFoundException) {
                                view.loadUrl(url)
                            }
                        }
                        return true
                    }
                }
            }
        },
        update = { webView -> webView.loadUrl(url.urlDecode()) }
    )
}