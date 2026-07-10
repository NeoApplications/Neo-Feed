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

package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.mastodon.MASTODON_REDIRECT_URI
import com.saulhdev.feeder.manager.mastodon.MastodonApi
import com.saulhdev.feeder.manager.mastodon.MastodonAuth
import com.saulhdev.feeder.manager.mastodon.MastodonStorage
import com.saulhdev.feeder.manager.mastodon.buildMastodonFeedUrl
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MastodonAuthViewModel(
    private val storage: MastodonStorage,
    private val auth: MastodonAuth,
    private val api: MastodonApi,
    private val sourcesRepo: SourcesRepository,
) : NeoViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val launchUrl: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    fun onLaunchHandled() {
        _uiState.update { it.copy(launchUrl = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun startAuth(instanceInput: String) {
        val instance = normalizeInstance(instanceInput)
        if (instance.isBlank()) {
            _uiState.update { it.copy(error = "Instance cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, launchUrl = null) }
            auth.registerApp(instance)
                .onSuccess { credentials ->
                    storage.saveAppCredentials(instance, credentials.clientId, credentials.clientSecret)
                    val state = UUID.randomUUID().toString()
                    storage.savePendingState(state, instance)
                    val url = auth.buildAuthorizationUrl(
                        instance = instance,
                        clientId = credentials.clientId,
                        redirectUri = MASTODON_REDIRECT_URI,
                        state = state,
                    )
                    _uiState.update { it.copy(isLoading = false, launchUrl = url) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun completeAuth(code: String, state: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val instance = storage.getPendingInstance(state)
            if (instance == null) {
                _uiState.update { it.copy(isLoading = false, error = "Invalid or expired authorization state") }
                return@launch
            }
            storage.clearPendingState()

            val (clientId, clientSecret) = storage.getAppCredentials(instance)
                ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "App credentials not found") }
                    return@launch
                }

            auth.exchangeCode(
                instance = instance,
                clientId = clientId,
                clientSecret = clientSecret,
                code = code,
                redirectUri = MASTODON_REDIRECT_URI,
            )
                .onSuccess { token ->
                    api.verifyCredentials(instance, token)
                        .onSuccess { account ->
                            val username = account.acct.substringBefore("@").removePrefix("@")
                            storage.saveAccessToken(instance, username, token)
                            val feed = Feed(
                                title = "$username@$instance",
                                description = "Mastodon home timeline",
                                url = sloppyLinkToStrictURL(buildMastodonFeedUrl(instance, username)),
                                feedImage = sloppyLinkToStrictURLNoThrows(""),
                                sourceType = "mastodon",
                            )
                            sourcesRepo.insertSource(feed)
                            _uiState.update { it.copy(isLoading = false) }
                            _completed.value = true
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun normalizeInstance(input: String): String {
        return input.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore("?")
    }
}
