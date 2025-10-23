/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team <saulhdev@hotmail.com>
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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.utils.extensions.StableHolder
import com.saulhdev.feeder.utils.extensions.interceptKey
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.utils.extensions.safeSemantics
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.navigation.LocalNavController
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import com.saulhdev.feeder.viewmodels.SearchFeedViewModel
import com.saulhdev.feeder.viewmodels.SearchResult
import com.saulhdev.feeder.viewmodels.SourceListViewModel
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

@Composable
fun SourceAddPage(
    searchFeedViewModel: SearchFeedViewModel = koinNeoViewModel(),
    sourcesViewModel: SourceListViewModel = koinNeoViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val title = stringResource(id = R.string.add_rss)

    var results by rememberSaveable {
        mutableStateOf(listOf<SearchResult>())
    }

    BackHandler {
        sourcesViewModel.saveFeed(results)
        navController.popBackStack()
    }

    ViewWithActionBar(
        title = title,
        onBackAction = {
            sourcesViewModel.saveFeed(results)
            navController.popBackStack()
        }
    ) { paddingValues ->
        var currentlySearching by rememberSaveable {
            mutableStateOf(false)
        }
        var errors by rememberSaveable {
            mutableStateOf(listOf<SearchResult>())
        }
        var feedUrl by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddFeedView(
                feedUrl = feedUrl,
                onUrlChanged = {
                    feedUrl = it
                },
                onSearch = { url ->
                    results = emptyList()
                    errors = emptyList()
                    currentlySearching = true
                    coroutineScope.launch {
                        searchFeedViewModel.searchForFeeds(url)
                            .onCompletion {
                                currentlySearching = false
                            }
                            .collect {
                                if (it.isError) {
                                    errors = errors + it
                                } else {
                                    results = results + it
                                }
                            }
                    }
                },
                results = StableHolder(results),
                errors = if (currentlySearching) StableHolder(emptyList()) else StableHolder(errors),
                currentlySearching = currentlySearching,
                onClick = {
                    //saveFeed(results, repository)
                    //TODO: enable click event when the rss is added
                }
            )
        }
    }
}

@Composable
fun AddFeedView(
    feedUrl: String = "",
    onUrlChanged: (String) -> Unit,
    onSearch: (URL) -> Unit,
    results: StableHolder<List<SearchResult>>,
    errors: StableHolder<List<SearchResult>>,
    currentlySearching: Boolean,
    onClick: (SearchResult) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
        ) {
            SearchFeedUI(
                feedUrl = feedUrl,
                onUrlChanged = onUrlChanged,
                onSearch = onSearch,
                focusManager = focusManager,
                keyboardController = keyboardController
            )
            SearchResult(
                results = results,
                errors = errors,
                currentlySearching = currentlySearching,
                onClick = onClick
            )
        }
    }
}

@Composable
fun SearchFeedUI(
    feedUrl: String,
    onUrlChanged: (String) -> Unit,
    onSearch: (URL) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    val isNotValidUrl by remember(feedUrl) {
        derivedStateOf {
            feedUrl.isNotEmpty() && isNotValidUrl(feedUrl)
        }
    }
    val isValidUrl by remember(feedUrl) {
        derivedStateOf {
            isValidUrl(feedUrl)
        }
    }
    OutlinedTextField(
        value = feedUrl,
        onValueChange = onUrlChanged,
        modifier = Modifier
            .fillMaxWidth()
            .interceptKey(Key.Enter) {
                if (isValidUrl(feedUrl)) {
                    onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                    keyboardController?.hide()
                }
            }
            .interceptKey(Key.Escape) {
                focusManager.clearFocus()
            },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
        ),

        isError = isNotValidUrl,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (isValidUrl) {
                    onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                    keyboardController?.hide()
                }
            }
        ),
        shape = MaterialTheme.shapes.medium,
        label = { Text(text = stringResource(id = R.string.add_feed_search_hint)) }
    )

    OutlinedButton(
        enabled = isValidUrl,
        onClick = {
            if (isValidUrl) {
                onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                focusManager.clearFocus()
            }
        }
    ) {
        Text(
            stringResource(android.R.string.search_go)
        )
    }
}

@Composable
fun SearchResult(
    results: StableHolder<List<SearchResult>>,
    errors: StableHolder<List<SearchResult>>,
    currentlySearching: Boolean,
    onClick: (SearchResult) -> Unit,
) {
    if (results.item.isEmpty()) {
        for (error in errors.item) {
            val title = stringResource(
                R.string.failed_to_parse,
                error.url
            )
            ErrorResultView(
                title = title,
                description = error.description
            )
        }
    }
    for (result in results.item) {
        SearchResultView(
            title = result.title,
            url = result.url,
            description = result.description
        ) {
            onClick(result)
        }
    }
    AnimatedVisibility(visible = currentlySearching) {
        SearchingIndicator()
    }
}

@Composable
fun SearchingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .safeSemantics {
                testTag = "searchingIndicator"
            }
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SearchResultView(
    title: String,
    url: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .safeSemantics {
                testTag = "searchResult"
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                url,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorResultView(
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .safeSemantics {
                testTag = "errorResult"
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall
                    .copy(color = MaterialTheme.colorScheme.error)
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun isValidUrl(url: String): Boolean {
    if (url.isBlank()) {
        return false
    }
    return try {
        try {
            URL(url)
            true
        } catch (_: MalformedURLException) {
            URL("http://$url")
            true
        }
    } catch (e: Exception) {
        false
    }
}

private fun isNotValidUrl(url: String) = !isValidUrl(url)

@Preview
@Composable
fun SourceAddPagePreview() {
    SourceAddPage()
}