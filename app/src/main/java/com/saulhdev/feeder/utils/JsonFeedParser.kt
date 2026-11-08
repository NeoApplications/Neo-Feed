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

package com.saulhdev.feeder.utils

import com.saulhdev.feeder.data.entity.JsonFeed
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun cachingHttpClient(
    cacheDirectory: File? = null,
    cacheSize: Long = 10L * 1024L * 1024L,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L
): OkHttpClient {
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    if (cacheDirectory != null) {
        builder.cache(Cache(cacheDirectory, cacheSize))
    }

    builder
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)

    return builder.build()
}

fun feedAdapter(): JsonAdapter<JsonFeed> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(JsonFeed::class.java)

/**
 * A parser for JSONFeeds. CacheDirectory and CacheSize are only relevant if feeds are downloaded. They are not used
 * for parsing JSON directly.
 */
class JsonFeedParser(
    private val httpClient: OkHttpClient,
    private val jsonJsonFeedAdapter: JsonAdapter<JsonFeed>
) {

    constructor(
        cacheDirectory: File? = null,
        cacheSize: Long = 10L * 1024L * 1024L,
        connectTimeoutSecs: Long = 5L,
        readTimeoutSecs: Long = 5L
    ) : this(
        cachingHttpClient(
            cacheDirectory = cacheDirectory,
            cacheSize = cacheSize,
            connectTimeoutSecs = connectTimeoutSecs,
            readTimeoutSecs = readTimeoutSecs
        ),
        feedAdapter()
    )

    /**
     * Parse a JSONFeed
     */
    fun parseJson(responseBody: ResponseBody): JsonFeed =
        parseJson(responseBody.string())

    /**
     * Parse a JSONFeed
     */
    fun parseJson(json: String): JsonFeed = jsonJsonFeedAdapter.fromJson(json)
        ?: throw IOException("Failed to parse JSONFeed")
}