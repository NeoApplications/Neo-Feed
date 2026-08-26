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

package com.saulhdev.feeder.manager.models

import android.util.Log
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.saulhdev.feeder.data.entity.JsonFeed
import com.saulhdev.feeder.utils.JsonFeedParser
import com.saulhdev.feeder.utils.extensions.asFeed
import com.saulhdev.feeder.utils.relativeLinkIntoAbsolute
import com.saulhdev.feeder.utils.relativeLinkIntoAbsoluteOrThrow
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

class FeedParser(
    private val client: OkHttpClient,
) {
    private val jsonFeedParser: JsonFeedParser = JsonFeedParser()

    private suspend fun getFeedIconAtUrl(url: URL): String? =
        try {
            val html = curl(url)
            when {
                html != null -> getFeedIconInHtml(html, baseUrl = url)
                else -> null
            }
        } catch (t: Throwable) {
            Log.e("FeedParser", "Error when fetching feed icon", t)
            null
        }

    private fun getFeedIconInHtml(
        html: String,
        baseUrl: URL? = null,
    ): String? {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")

        return (
            doc.getElementsByAttributeValue("rel", "apple-touch-icon") +
                doc.getElementsByAttributeValue("rel", "icon") +
                doc.getElementsByAttributeValue("rel", "shortcut icon")
        ).filter { it.hasAttr("href") }
            .map {
                when {
                    baseUrl != null -> {
                        relativeLinkIntoAbsolute(
                            base = baseUrl,
                            link = it.attr("href"),
                        )
                    }

                    else -> {
                        sloppyLinkToStrictURL(it.attr("href")).toString()
                    }
                }
            }.firstOrNull()
    }

    /**
     * Returns all alternate links in the header of an HTML/XML document pointing to feeds.
     */
    suspend fun getAlternateFeedLinksAtUrl(url: URL): List<Pair<String, String>> =
        try {
            val html = curl(url)
            when {
                html != null -> getAlternateFeedLinksInHtml(html, baseUrl = url)
                else -> emptyList()
            }
        } catch (t: Throwable) {
            Log.e("FeedParser", "Error when fetching alternate links", t)
            emptyList()
        }

    /**
     * Returns all alternate links in the HTML/XML document pointing to feeds.
     */
    private fun getAlternateFeedLinksInHtml(
        html: String,
        baseUrl: URL? = null,
    ): List<Pair<String, String>> {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")

        val feeds =
            doc
                .getElementsByAttributeValue("rel", "alternate")
                .filter { element ->
                    element.hasAttr("href") && element.hasAttr("type")
                }.filter { element ->
                    val t = element.attr("type").lowercase(Locale.getDefault())
                    when {
                        t.contains("application/atom") -> true

                        t.contains("application/rss") -> true

                        // Youtube for example has alternate links with application/json+oembed type.
                        t == "application/json" -> true

                        else -> false
                    }
                }.filter { element ->
                    val l = element.attr("href").lowercase(Locale.getDefault())
                    try {
                        if (baseUrl != null) {
                            relativeLinkIntoAbsoluteOrThrow(base = baseUrl, link = l)
                        } else {
                            URL(l)
                        }
                        true
                    } catch (_: MalformedURLException) {
                        false
                    }
                }.map {
                    when {
                        baseUrl != null -> {
                            relativeLinkIntoAbsolute(
                                base = baseUrl,
                                link = it.attr("href"),
                            ) to it.attr("type")
                        }

                        else -> {
                            sloppyLinkToStrictURL(it.attr("href")).toString() to it.attr("type")
                        }
                    }
                }

        return when {
            feeds.isNotEmpty() -> {
                feeds
            }

            baseUrl?.host == "www.youtube.com" || baseUrl?.host == "youtube.com" -> {
                findFeedLinksForYoutube(
                    doc,
                )
            }

            else -> {
                emptyList()
            }
        }
    }

    private fun findFeedLinksForYoutube(doc: Document): List<Pair<String, String>> {
        val channelId: String? =
            doc
                .body()
                .getElementsByAttribute(YOUTUBE_CHANNEL_ID_ATTR)
                .firstOrNull()
                ?.attr(YOUTUBE_CHANNEL_ID_ATTR)

        return when (channelId) {
            null -> emptyList()
            else -> listOf("https://www.youtube.com/feeds/videos.xml?channel_id=$channelId" to "atom")
        }
    }

    /**
     * @throws IOException if request fails due to network issue for example
     */
    private suspend fun curl(url: URL) = client.curl(url)

    /**
     * @throws IOException if request fails due to network issue for example
     */
    private suspend fun curlAndOnResponse(
        url: URL,
        block: (suspend (Response) -> Unit),
    ) = client.curlAndOnResponse(url, block)

    @Throws(FeedParsingError::class)
    suspend fun parseFeedUrl(url: URL): JsonFeed? {
        try {
            var result: JsonFeed? = null
            curlAndOnResponse(url) {
                result = parseFeedResponse(it)
            }
            // Preserve original URL to maintain authentication data and/or tokens in query params
            return result?.copy(feed_url = url.toString())
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    @Throws(FeedParsingError::class)
    suspend fun parseFeedResponse(response: Response): JsonFeed =
        response.body.use {
            // OkHttp string method handles BOM and Content-Type header in request
            parseFeedResponse(
                response.request.url.toUrl(),
                it,
            )
        }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @Throws(FeedParsingError::class)
    suspend fun parseFeedResponse(
        url: URL,
        responseBody: ResponseBody,
    ): JsonFeed {
        try {
            val feed =
                when (responseBody.contentType()?.subtype?.contains("json")) {
                    true -> jsonFeedParser.parseJson(responseBody)
                    else -> parseRssAtom(url, responseBody)
                }

            return if (feed.feed_url == null) {
                // Nice to return non-null value here
                feed.copy(feed_url = url.toString())
            } else {
                feed
            }
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    @Throws(FeedParsingError::class)
    internal suspend fun parseRssAtom(
        baseUrl: URL,
        responseBody: ResponseBody,
    ): JsonFeed {
        try {
            responseBody.byteStream().use { bs ->
                val feed =
                    XmlReader(bs, true, responseBody.contentType()?.charset()?.name()).use {
                        SyndFeedInput()
                            .apply {
                                isPreserveWireFeed = true
                            }.build(it)
                    }
                return feed.asFeed(baseUrl = baseUrl) { siteUrl ->
                    getFeedIconAtUrl(siteUrl)
                }
            }
        } catch (t: Throwable) {
            throw FeedParsingError(baseUrl, t)
        }
    }

    class FeedParsingError(
        val url: URL,
        e: Throwable,
    ) : Exception(e.message, e)
}

suspend fun OkHttpClient.getResponse(
    url: URL,
    forceNetwork: Boolean = false,
): Response {
    val request =
        Request
            .Builder()
            .url(url)
            .cacheControl(
                CacheControl
                    .Builder()
                    .let {
                        if (forceNetwork) {
                            // Force a cache revalidation
                            it.maxAge(0, TimeUnit.SECONDS)
                        } else {
                            // Do a cache revalidation at most every minute
                            it.maxAge(1, TimeUnit.MINUTES)
                        }
                    }.build(),
            ).build()

    val clientToUse =
        if (url.userInfo?.isNotBlank() == true) {
            val parts = url.userInfo.split(':')
            val user = parts.first()
            val pass =
                if (parts.size > 1) {
                    parts[1]
                } else {
                    ""
                }
            val decodedUser =
                withContext(IO) {
                    URLDecoder.decode(user, "UTF-8")
                }
            val decodedPass =
                withContext(IO) {
                    URLDecoder.decode(pass, "UTF-8")
                }
            val credentials = Credentials.basic(decodedUser, decodedPass)
            newBuilder()
                .authenticator { _, response ->
                    when {
                        response.request.header("Authorization") != null -> {
                            null
                        }

                        else -> {
                            response.request
                                .newBuilder()
                                .header("Authorization", credentials)
                                .build()
                        }
                    }
                }.proxyAuthenticator { _, response ->
                    when {
                        response.request.header("Proxy-Authorization") != null -> {
                            null
                        }

                        else -> {
                            response.request
                                .newBuilder()
                                .header("Proxy-Authorization", credentials)
                                .build()
                        }
                    }
                }.build()
        } else {
            this
        }

    return withContext(IO) {
        clientToUse.newCall(request).execute()
    }
}

suspend fun OkHttpClient.curl(url: URL): String? {
    var result: String? = null
    curlAndOnResponse(url) {
        result = it.body.string()
    }
    return result
}

suspend fun OkHttpClient.curlAndOnResponse(
    url: URL,
    block: (suspend (Response) -> Unit),
) {
    val response = getResponse(url)

    if (!response.isSuccessful) {
        throw IOException("Unexpected code $response")
    }

    response.use {
        block(it)
    }
}
