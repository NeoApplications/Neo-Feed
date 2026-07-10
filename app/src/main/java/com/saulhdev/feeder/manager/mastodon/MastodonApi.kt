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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MastodonApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchHomeTimeline(
        instance: String,
        token: String,
        limit: Int,
    ): Result<List<MastodonStatus>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://$instance/api/v1/timelines/home?limit=${limit.coerceIn(1, 200)}")
                .header("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                throw IllegalStateException("Failed to fetch timeline: ${response.code} ${body ?: ""}")
            }
            json.decodeFromString(ListSerializer(MastodonStatus.serializer()), body)
        }
    }

    suspend fun verifyCredentials(instance: String, token: String): Result<MastodonAccount> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://$instance/api/v1/accounts/verify_credentials")
                .header("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrBlank()) {
                throw IllegalStateException("Failed to verify credentials: ${response.code} ${body ?: ""}")
            }
            json.decodeFromString(MastodonAccount.serializer(), body)
        }
    }
}
