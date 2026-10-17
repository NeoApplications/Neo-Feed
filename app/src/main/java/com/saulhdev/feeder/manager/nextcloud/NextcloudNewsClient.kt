/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

package com.saulhdev.feeder.manager.nextcloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class NextcloudNewsClient(private val okHttpClient: OkHttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val workingBaseUrlCache = ConcurrentHashMap<String, String>()

    fun getBaseApiUrls(serverUrl: String): List<String> {
        var base = serverUrl.trim().trimEnd('/')
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "https://$base"
        }

        val cached = workingBaseUrlCache[serverUrl]
        val candidates = mutableListOf<String>()
        if (cached != null) {
            candidates.add(cached)
        }

        if (base.endsWith("/index.php/apps/news/api/v1-3") || base.endsWith("/apps/news/api/v1-3") ||
            base.endsWith("/index.php/apps/news/api/v1-2") || base.endsWith("/apps/news/api/v1-2")
        ) {
            if (!candidates.contains(base)) candidates.add(base)
            return candidates
        }

        val baseWithoutIndex = base.removeSuffix("/index.php").trimEnd('/')

        val withIndex = "$baseWithoutIndex/index.php/apps/news/api/v1-3"
        val withoutIndex = "$baseWithoutIndex/apps/news/api/v1-3"

        if (base.endsWith("/index.php")) {
            if (!candidates.contains(withIndex)) candidates.add(withIndex)
            if (!candidates.contains(withoutIndex)) candidates.add(withoutIndex)
        } else {
            if (!candidates.contains(withIndex)) candidates.add(withIndex)
            if (!candidates.contains(withoutIndex)) candidates.add(withoutIndex)
        }

        return candidates
    }

    suspend fun checkConnection(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<NextcloudUser> = withContext(Dispatchers.IO) {
        val candidates = getBaseApiUrls(serverUrl)
        var lastException: Throwable? = null

        for (base in candidates) {
            val authHeader = Credentials.basic(username, passwordOrToken)

            val versionResult = tryEndpoint(
                url = "$base/version",
                authHeader = authHeader
            ) { body ->
                val ver = json.decodeFromString<NextcloudVersion>(body)
                val display =
                    if (ver.version.isNotBlank()) "$username (v${ver.version})" else username
                NextcloudUser(userId = display)
            }
            if (versionResult.isSuccess) {
                workingBaseUrlCache[serverUrl] = base
                return@withContext versionResult
            }

            val statusResult = tryEndpoint(
                url = "$base/status",
                authHeader = authHeader
            ) { body ->
                val status = json.decodeFromString<NextcloudStatus>(body)
                val display =
                    if (status.version.isNotBlank()) "$username (v${status.version})" else username
                NextcloudUser(userId = display)
            }
            if (statusResult.isSuccess) {
                workingBaseUrlCache[serverUrl] = base
                return@withContext statusResult
            }

            val feedsResult = tryEndpoint(
                url = "$base/feeds",
                authHeader = authHeader
            ) {
                NextcloudUser(userId = username)
            }
            if (feedsResult.isSuccess) {
                workingBaseUrlCache[serverUrl] = base
                return@withContext feedsResult
            }

            val userResult = tryEndpoint(
                url = "$base/user",
                authHeader = authHeader
            ) { body ->
                json.decodeFromString<NextcloudUser>(body)
            }
            if (userResult.isSuccess) {
                workingBaseUrlCache[serverUrl] = base
                return@withContext userResult
            }

            val failure = versionResult.exceptionOrNull()
            if (failure?.message?.contains("401") == true) {
                return@withContext Result.failure(failure)
            }
            lastException = failure ?: statusResult.exceptionOrNull()
        }

        Result.failure(lastException ?: IOException("Failed to connect to Nextcloud News"))
    }

    private fun <T> tryEndpoint(
        url: String,
        authHeader: String,
        transform: (String) -> T,
    ): Result<T> {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader)
            .header("Content-Type", "application/json")
            .get()
            .build()

        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    Result.success(transform(body))
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> executeGetWithFallback(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        endpointPath: String,
        transform: (responseBody: String) -> T,
    ): Result<T> = withContext(Dispatchers.IO) {
        val candidates = getBaseApiUrls(serverUrl)
        var lastException: Exception? = null

        for (base in candidates) {
            val url = "$base/$endpointPath"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(username, passwordOrToken))
                .get()
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        workingBaseUrlCache[serverUrl] = base
                        val body = response.body.string()
                        return@withContext Result.success(transform(body))
                    } else if (response.code == 404 || response.code == 405) {
                        lastException = IOException("HTTP ${response.code}: ${response.message}")
                    } else {
                        return@withContext Result.failure(
                            IOException("HTTP ${response.code}: ${response.message}")
                        )
                    }
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: IOException("Failed to connect to Nextcloud News"))
    }

    suspend fun fetchFolders(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<List<NextcloudFolder>> = executeGetWithFallback(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "folders"
    ) { body ->
        json.decodeFromString<NextcloudFoldersResponse>(body).folders
    }

    suspend fun fetchFeeds(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<List<NextcloudFeed>> = executeGetWithFallback(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "feeds"
    ) { body ->
        json.decodeFromString<NextcloudFeedsResponse>(body).feeds
    }

    suspend fun fetchItems(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        batchSize: Int = 100,
        getRead: Boolean = true,
        lastModified: Long = 0L,
    ): Result<List<NextcloudItem>> = executeGetWithFallback(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "items?batchSize=$batchSize&getRead=$getRead&lastModified=$lastModified"
    ) { body ->
        json.decodeFromString<NextcloudItemsResponse>(body).items
    }

    suspend fun markItemRead(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeItemAction(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "items/$itemId/read",
    )

    suspend fun markItemUnread(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeItemAction(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "items/$itemId/unread",
    )

    suspend fun starItem(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeItemAction(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "items/$itemId/star",
    )

    suspend fun unstarItem(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeItemAction(
        serverUrl = serverUrl,
        username = username,
        passwordOrToken = passwordOrToken,
        endpointPath = "items/$itemId/unstar",
    )

    private suspend fun executeItemAction(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        endpointPath: String,
        payloadJson: String = "{}",
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val candidates = getBaseApiUrls(serverUrl)
        var lastException: Exception? = null

        for (base in candidates) {
            val fullUrl = "$base/$endpointPath"
            val authHeader = Credentials.basic(username, passwordOrToken)

            val postRequest = Request.Builder()
                .url(fullUrl)
                .header("Authorization", authHeader)
                .post(payloadJson.toRequestBody(jsonMediaType))
                .build()

            try {
                okHttpClient.newCall(postRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        workingBaseUrlCache[serverUrl] = base
                        return@withContext Result.success(Unit)
                    }

                    if (response.code == 405) {
                        val putRequest = Request.Builder()
                            .url(fullUrl)
                            .header("Authorization", authHeader)
                            .put(payloadJson.toRequestBody(jsonMediaType))
                            .build()

                        okHttpClient.newCall(putRequest).execute().use { putResponse ->
                            if (putResponse.isSuccessful) {
                                workingBaseUrlCache[serverUrl] = base
                                return@withContext Result.success(Unit)
                            } else if (putResponse.code == 404 || putResponse.code == 405) {
                                lastException =
                                    IOException("HTTP ${putResponse.code}: ${putResponse.message}")
                            } else {
                                return@withContext Result.failure(
                                    IOException("HTTP ${putResponse.code}: ${putResponse.message}")
                                )
                            }
                        }
                    } else if (response.code == 404) {
                        lastException = IOException("HTTP ${response.code}: ${response.message}")
                    } else {
                        return@withContext Result.failure(
                            IOException("HTTP ${response.code}: ${response.message}")
                        )
                    }
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: IOException("Failed to execute item action"))
    }
}
