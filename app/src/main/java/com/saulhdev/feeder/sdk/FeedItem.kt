package com.saulhdev.feeder.sdk

import android.graphics.Color
import android.os.Parcelable
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.db.models.FeedArticle
import com.saulhdev.feeder.models.StoryCardContent
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@Parcelize
data class FeedItem(
    val id: Long,
    val title: String,
    val type: FeedItemType,
    val content: StoryCardContent,
    val bookmarked: Boolean,
    val time: Long
) : Parcelable {
    constructor(
        article: FeedArticle,
        feed: Feed,
    ) : this(
        id = article.id,
        title = "${feed.title} [RSS]",
        type = FeedItemType.STORY_CARD,
        content = StoryCardContent(
            title = article.title,
            text = article.description,
            background_url = article.imageUrl ?: "",
            link = article.link ?: "",
            source = FeedCategory(
                feed.url.toString(),
                feed.title,
                Color.GREEN,
                feed.feedImage.toString()
            )
        ),
        bookmarked = article.bookmarked,
        time = Date.from(
            ZonedDateTime.parse(
                article.pubDate.toString(),
                DateTimeFormatter.ISO_ZONED_DATE_TIME
            ).toInstant()
        ).time
    )

    override fun describeContents(): Int {
        return 0
    }
}