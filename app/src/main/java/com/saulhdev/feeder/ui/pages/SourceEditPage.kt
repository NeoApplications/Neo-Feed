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

package com.saulhdev.feeder.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.entity.SourceEditViewState
import com.saulhdev.feeder.extensions.interceptKey
import com.saulhdev.feeder.extensions.koinNeoViewModel
import com.saulhdev.feeder.ui.components.ComposeSwitchView
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.viewmodels.SourceEditViewModel

@Composable
fun SourceEditPage(
    feedId: Long = -1,
    sourceEditViewModel: SourceEditViewModel = koinNeoViewModel(),
    onDismiss: (() -> Unit),
) {
    val title = stringResource(id = R.string.edit_rss)

    sourceEditViewModel.setFeedId(feedId)
    val viewState by sourceEditViewModel.viewState.collectAsState()

    ViewWithActionBar(
        title = title,
        showBackButton = true,
        onBackAction = onDismiss,
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 8.dp,
                end = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SourceEditView(
                viewState = viewState,
                updateFeed = sourceEditViewModel::updateFeed,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun SourceEditView(
    viewState: SourceEditViewState,
    updateFeed: (SourceEditViewState) -> Unit,
    onDismiss: (() -> Unit),
) {
    val (focusTitle, focusTag) = createRefs()
    val feedState = remember(viewState) {
        mutableStateOf(viewState)
    }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedTextField(
                value = feedState.value.url,
                onValueChange = {
                    feedState.value = feedState.value.copy(url = it)
                },
                label = {
                    Text(stringResource(id = R.string.add_input_hint))
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusTitle.requestFocus()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .interceptKey(Key.Enter) {
                        focusTitle.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
            )
        }
        item {
            OutlinedTextField(
                value = feedState.value.title,
                onValueChange = {
                    feedState.value = feedState.value.copy(title = it)
                },
                label = {
                    Text(stringResource(id = R.string.title))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusTag.requestFocus()
                    }
                ),
                modifier = Modifier
                    .focusRequester(focusTitle)
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .interceptKey(Key.Enter) {
                        focusTag.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
            )
        }
        item {
            OutlinedTextField(
                value = feedState.value.tag,
                onValueChange = {
                    feedState.value = feedState.value.copy(tag = it)
                },
                label = {
                    Text(stringResource(id = R.string.source_tags))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusTag.requestFocus()
                    }
                ),
                modifier = Modifier
                    .focusRequester(focusTitle)
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .interceptKey(Key.Enter) {
                        focusTag.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    }
            )
        }

        item {
            ComposeSwitchView(
                titleId = R.string.fetch_full_articles_by_default,
                isChecked = feedState.value.fullTextByDefault,
                onCheckedChange = {
                    feedState.value = feedState.value.copy(fullTextByDefault = it)
                },
                index = 0,
                groupSize = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            ComposeSwitchView(
                titleId = R.string.source_enabled,
                isChecked = feedState.value.isEnabled,
                onCheckedChange = {
                    feedState.value = feedState.value.copy(isEnabled = it)
                },
                index = 1,
                groupSize = 2
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(id = android.R.string.cancel)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        updateFeed(feedState.value)
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(id = android.R.string.ok)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SourceEditPagePreview() {
    SourceEditView(
        viewState = SourceEditViewState(
            url = "https://example.com/feed",
            title = "Example Feed",
            fullTextByDefault = true,
            isEnabled = true
        ),
        updateFeed = {},
        onDismiss = {}
    )
}