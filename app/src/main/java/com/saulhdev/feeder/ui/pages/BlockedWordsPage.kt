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

package com.saulhdev.feeder.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.TrashSimple
import com.saulhdev.feeder.ui.navigation.LocalNavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BlockedWordsPage(
    prefs: FeedPreferences = koinInject(),
    articleRepository: ArticleRepository = koinInject(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val words by prefs.blockedWords.get().collectAsState(initial = emptySet())
    var newWord by remember { mutableStateOf("") }

    fun save(updated: Set<String>) {
        scope.launch {
            prefs.blockedWords.setValue(updated)
            articleRepository.deleteArticlesMatchingWords(updated, context.filesDir)
        }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.blocked_words_title),
        onBackAction = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newWord,
                    onValueChange = { newWord = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.blocked_words_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = newWord.trim().lowercase()
                            if (trimmed.isNotBlank()) {
                                save(words + trimmed)
                                newWord = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )
                Button(
                    onClick = {
                        val trimmed = newWord.trim().lowercase()
                        if (trimmed.isNotBlank()) {
                            save(words + trimmed)
                            newWord = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = newWord.trim().isNotBlank()
                ) {
                    Text(text = stringResource(id = R.string.blocked_words_add))
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (words.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.blocked_words_empty),
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(words.sorted(), key = { it }) { word ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(
                                onClick = { save(words - word) }
                            ) {
                                Icon(
                                    imageVector = Phosphor.TrashSimple,
                                    contentDescription = stringResource(id = R.string.action_delete)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
