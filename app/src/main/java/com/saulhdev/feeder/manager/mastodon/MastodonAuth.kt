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

package com.saulhdev.feeder.manager.mastodon

import com.saulhdev.feeder.utils.urlEncode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

const val MASTODON_REDIRECT_URI = "nf-mastodon://callback"
const val MASTODON_SCOPES = "read"
const val MASTODON_CLIENT_NAME = "Neo Feed"
const val MASTODON_WEBSITE = "https://github.com/NeoApplications/Neo-Feed"

class MastodonAuth {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun registerApp(instance: String): Result<MastodonAppCredentials> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://$instance/api/v1/apps")
                .post(
                    FormBody.Builder()
                        .add("client_name", MASTODON_CLIENT_NAME)
                        .add("redirect_uris", MASTODON_REDIRECT_URI)
                        .add("scopes", MASTODON_SCOPES)
                        .add("website", MASTODON_WEBSITE)
                        .build()
                )
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                throw IllegalStateException("Failed to register app: ${response.code} ${body ?: ""}")
            }
            json.decodeFromString(MastodonAppCredentials.serializer(), body)
        }
    }

    fun buildAuthorizationUrl(
        instance: String,
        clientId: String,
        redirectUri: String = MASTODON_REDIRECT_URI,
        state: String,
        scope: String = MASTODON_SCOPES,
    ): String {
        return "https://$instance/oauth/authorize" +
                "?client_id=${clientId.urlEncode()}" +
                "&redirect_uri=${redirectUri.urlEncode()}" +
                "&response_type=code" +
                "&scope=${scope.urlEncode()}" +
                "&state=${state.urlEncode()}"
    }

    suspend fun exchangeCode(
        instance: String,
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String = MASTODON_REDIRECT_URI,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://$instance/oauth/token")
                .post(
                    FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("code", code)
                        .add("client_id", clientId)
                        .add("client_secret", clientSecret)
                        .add("redirect_uri", redirectUri)
                        .add("scope", MASTODON_SCOPES)
                        .build()
                )
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                throw IllegalStateException("Failed to exchange token: ${response.code} ${body ?: ""}")
            }
            val token = json.decodeFromString(MastodonToken.serializer(), body)
            token.accessToken
        }
    }
}
