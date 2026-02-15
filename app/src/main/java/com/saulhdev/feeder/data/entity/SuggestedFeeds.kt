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

package com.saulhdev.feeder.data.entity

data class SuggestedFeed(
    val title: String,
    val url: String,
    val description: String,
)

object SuggestedFeedsData {
    val categories: Map<String, List<SuggestedFeed>> =
        mapOf(
            "Tech" to
                listOf(
                    SuggestedFeed(
                        title = "Ars Technica",
                        url = "https://feeds.arstechnica.com/arstechnica/index",
                        description = "Technology, science, and culture news",
                    ),
                    SuggestedFeed(
                        title = "The Verge",
                        url = "https://www.theverge.com/rss/index.xml",
                        description = "Technology, science, art, and culture",
                    ),
                    SuggestedFeed(
                        title = "TechCrunch",
                        url = "https://techcrunch.com/feed/",
                        description = "Startup and technology news",
                    ),
                    SuggestedFeed(
                        title = "Hacker News",
                        url = "https://hnrss.org/frontpage",
                        description = "Social news for programmers and entrepreneurs",
                    ),
                ),
            "Android" to
                listOf(
                    SuggestedFeed(
                        title = "Android Police",
                        url = "https://www.androidpolice.com/feed/",
                        description = "Android news, reviews, and how-to",
                    ),
                    SuggestedFeed(
                        title = "9to5Google",
                        url = "https://9to5google.com/feed/",
                        description = "Google and Android news and analysis",
                    ),
                    SuggestedFeed(
                        title = "XDA Developers",
                        url = "https://www.xda-developers.com/feed/",
                        description = "Android, computing, and tech enthusiast community",
                    ),
                ),
            "France" to
                listOf(
                    SuggestedFeed(
                        title = "Le Monde",
                        url = "https://www.lemonde.fr/rss/une.xml",
                        description = "Actualités internationales et françaises",
                    ),
                    SuggestedFeed(
                        title = "Libération",
                        url = "https://www.liberation.fr/arc/outboundfeeds/rss-all/collection/accueil-702702/",
                        description = "Actualités, opinions et culture",
                    ),
                    SuggestedFeed(
                        title = "France 24",
                        url = "https://www.france24.com/fr/rss",
                        description = "Actualités internationales en français",
                    ),
                    SuggestedFeed(
                        title = "Numerama",
                        url = "https://www.numerama.com/feed/",
                        description = "Tech, science et culture numérique",
                    ),
                    SuggestedFeed(
                        title = "Frandroid",
                        url = "https://www.frandroid.com/feed",
                        description = "Android, tech et objets connectés",
                    ),
                    SuggestedFeed(
                        title = "Next",
                        url = "https://next.ink/feed/",
                        description = "Actualités informatiques et numériques",
                    ),
                    SuggestedFeed(
                        title = "Korben",
                        url = "https://korben.info/feed",
                        description = "Tech, hacking et culture geek",
                    ),
                ),
            "News" to
                listOf(
                    SuggestedFeed(
                        title = "Reuters",
                        url = "https://www.reutersagency.com/feed/",
                        description = "International news agency",
                    ),
                    SuggestedFeed(
                        title = "BBC News",
                        url = "https://feeds.bbci.co.uk/news/rss.xml",
                        description = "World news from the BBC",
                    ),
                ),
            "Science" to
                listOf(
                    SuggestedFeed(
                        title = "NASA",
                        url = "https://www.nasa.gov/feed/",
                        description = "Space and aeronautics news from NASA",
                    ),
                    SuggestedFeed(
                        title = "Nature",
                        url = "https://www.nature.com/nature.rss",
                        description = "International journal of science",
                    ),
                    SuggestedFeed(
                        title = "Science Daily",
                        url = "https://www.sciencedaily.com/rss/all.xml",
                        description = "Latest science research news",
                    ),
                ),
            "Open Source" to
                listOf(
                    SuggestedFeed(
                        title = "It's FOSS",
                        url = "https://itsfoss.com/feed/",
                        description = "Linux and open source news and tutorials",
                    ),
                    SuggestedFeed(
                        title = "OMG! Ubuntu",
                        url = "https://www.omgubuntu.co.uk/feed",
                        description = "Ubuntu Linux news, apps, and reviews",
                    ),
                    SuggestedFeed(
                        title = "Linux Today",
                        url = "https://www.linuxtoday.com/feed/",
                        description = "Linux news and information",
                    ),
                ),
            "YouTube" to
                listOf(
                    SuggestedFeed(
                        title = "MovieClips Trailers",
                        url = "https://www.youtube.com/feeds/videos.xml?channel_id=UCi8e0iOVk1fEOogdfu4YgfA",
                        description = "Latest movie trailers and clips",
                    ),
                    SuggestedFeed(
                        title = "Marques Brownlee (MKBHD)",
                        url = "https://www.youtube.com/feeds/videos.xml?channel_id=UCBJycsmduvYEL83R_U4JriQ",
                        description = "Tech reviews and videos",
                    ),
                    SuggestedFeed(
                        title = "Linus Tech Tips",
                        url = "https://www.youtube.com/feeds/videos.xml?channel_id=UCXuqSBlHAE6Xw-yeJA0Tunw",
                        description = "Tech tips, reviews, and entertainment",
                    ),
                ),
        )
}
