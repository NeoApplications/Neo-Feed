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

data class FeedLanguageGroup(
    val language: String,
    val categories: Map<String, List<SuggestedFeed>>,
)

object SuggestedFeedsData {
    val groups: List<FeedLanguageGroup> =
        listOf(
            FeedLanguageGroup(
                language = "Français",
                categories =
                    mapOf(
                        "Actualités" to
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
                                    title = "France Info",
                                    url = "https://www.francetvinfo.fr/titres.rss",
                                    description = "Info en continu",
                                ),
                                SuggestedFeed(
                                    title = "20 Minutes",
                                    url = "https://www.20minutes.fr/feeds/rss-une.xml",
                                    description = "Actualités en continu",
                                ),
                                SuggestedFeed(
                                    title = "Ouest-France",
                                    url = "https://www.ouest-france.fr/rss/une",
                                    description = "Premier quotidien français",
                                ),
                            ),
                        "Tech & Numérique" to
                            listOf(
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
                                SuggestedFeed(
                                    title = "Clubic",
                                    url = "https://www.clubic.com/feed/news.rss",
                                    description = "High-tech, logiciels et jeux vidéo",
                                ),
                                SuggestedFeed(
                                    title = "01net",
                                    url = "https://www.01net.com/feed/",
                                    description = "Actualités high-tech et tests",
                                ),
                                SuggestedFeed(
                                    title = "Journal du Geek",
                                    url = "https://www.journaldugeek.com/feed/",
                                    description = "Geek, tech et pop culture",
                                ),
                                SuggestedFeed(
                                    title = "PhonAndroid",
                                    url = "https://www.phonandroid.com/feed",
                                    description = "Android, smartphones et bons plans",
                                ),
                            ),
                        "Science & Espace" to
                            listOf(
                                SuggestedFeed(
                                    title = "Futura Sciences",
                                    url = "https://www.futura-sciences.com/rss/actualites.xml",
                                    description = "Sciences, environnement et high-tech",
                                ),
                                SuggestedFeed(
                                    title = "Cité des Sciences",
                                    url = "https://www.cite-sciences.fr/rss.xml",
                                    description = "Actualités scientifiques",
                                ),
                            ),
                        "Lifestyle & Féminin" to
                            listOf(
                                SuggestedFeed(
                                    title = "Madmoizelle",
                                    url = "https://www.madmoizelle.com/feed",
                                    description = "Culture, société et féminisme",
                                ),
                                SuggestedFeed(
                                    title = "Cosmopolitan FR",
                                    url = "https://www.cosmopolitan.fr/feed",
                                    description = "Mode, beauté et lifestyle",
                                ),
                                SuggestedFeed(
                                    title = "Marie Claire FR",
                                    url = "https://www.marieclaire.fr/feed",
                                    description = "Mode, beauté et culture",
                                ),
                                SuggestedFeed(
                                    title = "Elle FR",
                                    url = "https://www.elle.fr/rss",
                                    description = "Mode, beauté et société",
                                ),
                                SuggestedFeed(
                                    title = "Grazia FR",
                                    url = "https://www.grazia.fr/feed",
                                    description = "Mode, beauté et célébrités",
                                ),
                                SuggestedFeed(
                                    title = "Aufeminin",
                                    url = "https://www.aufeminin.com/rss",
                                    description = "Lifestyle, bien-être et société",
                                ),
                            ),
                    ),
            ),
            FeedLanguageGroup(
                language = "English",
                categories =
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
                    ),
            ),
            FeedLanguageGroup(
                language = "YouTube",
                categories =
                    mapOf(
                        "YouTube" to
                            listOf(
                                SuggestedFeed(
                                    title = "AlloCiné Bandes Annonces",
                                    url = "https://www.youtube.com/feeds/videos.xml?channel_id=UC5i9ji_nljs6-mp0jz_hOHg",
                                    description = "Bandes-annonces et extraits de films en français",
                                ),
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
                    ),
            ),
        )
}
