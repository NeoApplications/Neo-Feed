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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.saulhdev.feeder.R
import com.saulhdev.feeder.utils.extensions.launchView
import com.saulhdev.feeder.ui.components.Table
import com.saulhdev.feeder.ui.components.TableCell
import com.saulhdev.feeder.ui.components.TextComposer
import com.saulhdev.feeder.ui.components.WithBidiDeterminedLayoutDirection
import com.saulhdev.feeder.ui.components.withAnnotation
import com.saulhdev.feeder.ui.components.withComposableStyle
import com.saulhdev.feeder.ui.components.withParagraph
import com.saulhdev.feeder.ui.components.withStyle
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Play
import com.saulhdev.feeder.ui.theme.BlockQuoteStyle
import com.saulhdev.feeder.ui.theme.CodeBlockBackground
import com.saulhdev.feeder.ui.theme.CodeBlockStyle
import com.saulhdev.feeder.ui.theme.CodeInlineStyle
import com.saulhdev.feeder.ui.theme.LinkTextStyle
import com.saulhdev.feeder.ui.theme.LocalDimens
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.roundToInt

fun LazyListScope.htmlFormattedText(
    inputStream: InputStream,
    baseUrl: String,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
) {
    Jsoup.parse(inputStream, null, baseUrl)
        .body()
        .let { body ->
            formatBody(
                element = body,
                imagePlaceholder = imagePlaceholder,
                onLinkClick = onLinkClick,
                baseUrl = baseUrl,
            )
        }
}

private fun LazyListScope.formatBody(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val dimens = LocalDimens.current
            val paragraph = paragraphBuilder.toComposableAnnotatedString()

            WithBidiDeterminedLayoutDirection(paragraph.text) {
                // ClickableText prevents taps from deselecting selected text
                // So use regular Text if possible
                if (
                    paragraph.getStringAnnotations("URL", 0, paragraph.length)
                        .isNotEmpty()
                ) {
                    ClickableText(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge
                            .merge(TextStyle(color = MaterialTheme.colorScheme.onBackground)),
                        modifier = Modifier.width(dimens.maxContentWidth)
                    ) { offset ->
                        paragraph.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()
                            ?.let {
                                onLinkClick(it.item)
                            }
                    }
                } else {
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge
                            .merge(TextStyle(color = MaterialTheme.colorScheme.onBackground)),
                        modifier = Modifier.width(dimens.maxContentWidth)
                    )
                }
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(),
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun LazyListScope.formatCodeBlock(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val dimens = LocalDimens.current
            val scrollState = rememberScrollState()
            Surface(
                color = CodeBlockBackground(),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .width(dimens.maxContentWidth)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .horizontalScroll(state = scrollState)
                        .padding(8.dp)
                ) {
                    Text(
                        text = paragraphBuilder.toComposableAnnotatedString(),
                        style = CodeBlockStyle(),
                        softWrap = false
                    )
                }
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(),
        preFormatted = true,
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun TextComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    lazyListScope: LazyListScope,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    var node = nodes.firstOrNull()
    while (node != null) {
        when (node) {
            is TextNode -> {
                if (preFormatted) {
                    append(node.wholeText)
                } else {
                    node.appendCorrectlyNormalizedWhiteSpace(
                        this,
                        stripLeading = endsWithWhitespace
                    )
                }
            }

            is Element  -> {
                val element = node
                when (element.tagName().lowercase()) {
                    "div"                    -> {
                        // Handle div based on its role/class
                        when {
                            element.hasClass("readability-styled") -> {
                                // Handle readability-styled divs similar to p tags
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }

                            element.hasClass("blockextract")       -> {
                                withParagraph {
                                    withComposableStyle(
                                        style = { BlockQuoteStyle() }
                                    ) {
                                        appendTextChildren(
                                            element.childNodes(),
                                            lazyListScope = lazyListScope,
                                            imagePlaceholder = imagePlaceholder,
                                            onLinkClick = onLinkClick,
                                            baseUrl = baseUrl,
                                        )
                                    }
                                }
                            }

                            element.attr("role") == "alert"        -> {
                                withParagraph {
                                    withComposableStyle(
                                        style = { SpanStyle(color = MaterialTheme.colorScheme.error) }
                                    ) {
                                        appendTextChildren(
                                            element.childNodes(),
                                            lazyListScope = lazyListScope,
                                            imagePlaceholder = imagePlaceholder,
                                            onLinkClick = onLinkClick,
                                            baseUrl = baseUrl,
                                        )
                                    }
                                }
                            }

                            else                                   -> {
                                // General div handling - treat as a block-level container
                                withParagraph {
                                    appendTextChildren(
                                        element.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                        }
                    }

                    "p"                      -> {
                        // Readability4j inserts p-tags in divs for algorithmic purposes.
                        // They screw up formatting.
                        if (node.hasClass("readability-styled")) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "br"                     -> append('\n')
                    "h1"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineMedium.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "h2"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "h3"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.titleLarge.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "h4"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.titleMedium.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "h5"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.titleSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "h6"                     -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.labelLarge.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }

                    "strong", "b"            -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "i", "em", "cite", "dfn" -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "tt"                     -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "u"                      -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "sup"                    -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "sub"                    -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "font"                   -> {
                        val fontFamily: FontFamily? = element.attr("face").asFontFamily()
                        withStyle(SpanStyle(fontFamily = fontFamily)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "pre"                    -> {
                        terminateCurrentText()
                        lazyListScope.formatCodeBlock(
                            element = element,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }

                    "code"                   -> {
                        if (element.parent()?.tagName() == "pre") {
                            terminateCurrentText()
                            lazyListScope.formatCodeBlock(
                                element = element,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            // inline code
                            withComposableStyle(
                                style = { CodeInlineStyle() }
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    preFormatted = preFormatted,
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "blockquote"             -> {
                        withParagraph {
                            withComposableStyle(
                                style = { BlockQuoteStyle() }
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "a"                      -> {
                        withComposableStyle(
                            style = { LinkTextStyle().toSpanStyle() }
                        ) {
                            withAnnotation("URL", element.attr("abs:href") ?: "") {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "img"                    -> handleImage(
                        element,
                        lazyListScope,
                        onLinkClick,
                        imagePlaceholder,
                        baseUrl,
                    )

                    "ul"                     -> {
                        element.children()
                            .filter { e -> e.tagName() == "li" }
                            .forEach { listItem ->
                                withParagraph {
                                    // no break space
                                    append("• ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }

                    "ol"                     -> {
                        element.children()
                            .filter { e -> e.tagName() == "li" }
                            .forEachIndexed { i, listItem ->
                                withParagraph {
                                    // no break space
                                    append("${i + 1}. ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }

                    "table"                  -> handleTable(
                        element,
                        preFormatted,
                        lazyListScope,
                        onLinkClick,
                        imagePlaceholder,
                        baseUrl,
                    )

                    "iframe"                 -> handleIFrame(element, lazyListScope, onLinkClick)

                    "rt"                     -> {
                        append(" (")
                        appendTextChildren(
                            element.childNodes(),
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                        append(")")
                    }

                    "rp"                     -> {
                        // Typically used to provide fallback parentheses for browsers that don't support ruby
                        // We can choose to render or ignore these depending on our needs
                        appendTextChildren(
                            element.childNodes(),
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }

                    "video"                  -> handleVideo(element, lazyListScope, onLinkClick)
                    "del", "s", "strike"     -> handleStrikethrough(
                        element,
                        lazyListScope,
                        imagePlaceholder,
                        onLinkClick,
                        baseUrl,
                    )

                    "mark"                   -> handleHighlight(
                        element,
                        lazyListScope,
                        imagePlaceholder,
                        onLinkClick,
                        baseUrl,
                    )

                    "q"                      -> handleInlineQuote(
                        element,
                        lazyListScope,
                        imagePlaceholder,
                        onLinkClick,
                        baseUrl,
                    )

                    "abbr"                   -> handleAbbreviation(
                        element,
                        lazyListScope,
                        imagePlaceholder,
                        onLinkClick,
                        baseUrl,
                    )

                    "hr"                     -> lazyListScope.handleHorizontalRule()
                    else                     -> {
                        appendTextChildren(
                            nodes = element.childNodes(),
                            preFormatted = preFormatted,
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}

private fun TextComposer.handleImage(
    element: Element,
    lazyListScope: LazyListScope,
    onLinkClick: (String) -> Unit,
    imagePlaceholder: Int,
    baseUrl: String,
) {
    val imageCandidates = getImageSource(baseUrl, element)
    if (imageCandidates.hasImage) {
        // Some sites are silly and insert formatting in alt text
        val alt = stripHtml(element.attr("alt") ?: "")
        appendImage(onLinkClick = onLinkClick) { onClick ->
            lazyListScope.item {
                val dimens = LocalDimens.current
                Column(
                    modifier = Modifier
                        .width(dimens.maxContentWidth)
                ) {
                    DisableSelection {
                        BoxWithConstraints(
                            modifier = Modifier
                                .clip(RectangleShape)
                                .clickable(
                                    enabled = onClick != null
                                ) {
                                    onClick?.invoke()
                                }
                                .fillMaxWidth()
                            // This makes scrolling a pain, find a way to solve that
//                                            .pointerInput("imgzoom") {
//                                                detectTransformGestures { centroid, pan, zoom, rotation ->
//                                                    val z = zoom * scale.value
//                                                    scale.value = when {
//                                                        z < 1f -> 1f
//                                                        z > 3f -> 3f
//                                                        else -> z
//                                                    }
//                                                }
//                                            }
                        ) {
                            val imageWidth = maxImageWidth()
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        imageCandidates.getBestImageForMaxSize(
                                            pixelDensity = pixelDensity(),
                                            maxWidth = imageWidth,
                                        )
                                    )
                                    .placeholder(imagePlaceholder)
                                    .error(imagePlaceholder)
                                    .scale(Scale.FIT)
                                    .size(imageWidth)
                                    .precision(Precision.INEXACT)
                                    .build(),
                                contentScale = ContentScale.FillWidth,
                                contentDescription = alt,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    if (alt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            alt,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun TextComposer.handleTable(
    element: Element,
    preFormatted: Boolean,
    lazyListScope: LazyListScope,
    onLinkClick: (String) -> Unit,
    imagePlaceholder: Int,
    baseUrl: String,
) {
    appendTable {
        lazyListScope.item {
            // Create a separate TextComposer for building cell contents
            val cellComposer = TextComposer { builder ->
                // This won't be called immediately, just storing the builder
                builder
            }

            element.children()
                .filter { e -> e.tagName() == "caption" }
                .forEach {
                    Text(
                        text = it.text(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

            val rows = element.children()
                .filter { e -> e.tagName() == "thead" || e.tagName() == "tbody" || e.tagName() == "tfoot" }
                .flatMap { section ->
                    section.children()
                        .filter { child -> child.tagName() == "tr" }
                }
                .map { row ->
                    row.children()
                        .filter { cell -> cell.tagName() == "td" || cell.tagName() == "th" }
                        .map { cell ->
                            // Build cell content using the cell composer
                            cellComposer.appendTextChildren(
                                cell.childNodes(),
                                preFormatted = preFormatted,
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl
                            )
                            // Get the built content and reset composer for next cell
                            val content = cellComposer.builder.toComposableAnnotatedString()
                            cellComposer.terminateCurrentText()

                            TableCell(
                                content = content,
                                colspan = cell.attr("colspan").toIntOrNull() ?: 1,
                                rowspan = cell.attr("rowspan").toIntOrNull() ?: 1,
                            )
                        }
                }
            Table(rows)
        }
    }
}

private fun TextComposer.handleIFrame(
    element: Element,
    lazyListScope: LazyListScope,
    onLinkClick: (String) -> Unit,
) {
    // TODO support code and others in iframe
    val video: Video? = getVideo(element.attr("abs:src"))

    if (video != null) {
        appendImage(onLinkClick = onLinkClick) {
            lazyListScope.item {
                val dimens = LocalDimens.current
                Column(
                    modifier = Modifier
                        .width(dimens.maxContentWidth)
                ) {
                    DisableSelection {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val imageWidth = maxImageWidth()
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .placeholder(R.drawable.ic_youtube)
                                    .error(R.drawable.ic_youtube)
                                    .scale(Scale.FIT)
                                    .size(imageWidth)
                                    .precision(Precision.INEXACT)
                                    .build(),
                                contentScale = ContentScale.FillWidth,
                                contentDescription = stringResource(R.string.touch_to_play_video),
                                modifier = Modifier
                                    .clickable {
                                        onLinkClick(video.link)
                                    }
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.touch_to_play_video),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun TextComposer.handleVideo(
    element: Element,
    lazyListScope: LazyListScope,
    onLinkClick: (String) -> Unit,
) {
    val videoSrc = element.attr("src")
    val posterSrc = element.attr("poster")

    appendImage(onLinkClick = onLinkClick) {
        lazyListScope.item {
            val dimens = LocalDimens.current
            Column(
                modifier = Modifier
                    .width(dimens.maxContentWidth)
            ) {
                DisableSelection {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val imageWidth = maxImageWidth()
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(posterSrc.ifEmpty { R.drawable.ic_youtube })
                                .placeholder(R.drawable.ic_youtube)
                                .error(R.drawable.ic_youtube)
                                .scale(Scale.FIT)
                                .size(imageWidth)
                                .precision(Precision.INEXACT)
                                .build(),
                            contentScale = ContentScale.FillWidth,
                            contentDescription = stringResource(R.string.touch_to_play_video),
                            modifier = Modifier
                                .clickable {
                                    onLinkClick(videoSrc)
                                }
                                .fillMaxWidth()
                        )

                        // Play button overlay
                        Box(
                            modifier = Modifier.matchParentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Phosphor.Play,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.touch_to_play_video),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun TextComposer.handleStrikethrough(
    element: Element,
    lazyListScope: LazyListScope,
    imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
        appendTextChildren(
            element.childNodes(),
            lazyListScope = lazyListScope,
            imagePlaceholder = imagePlaceholder,
            onLinkClick = onLinkClick,
            baseUrl = baseUrl,
        )
    }
}

private fun TextComposer.handleHighlight(
    element: Element,
    lazyListScope: LazyListScope,
    imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    withComposableStyle(
        style = { SpanStyle(background = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)) }
    ) {
        appendTextChildren(
            element.childNodes(),
            lazyListScope = lazyListScope,
            imagePlaceholder = imagePlaceholder,
            onLinkClick = onLinkClick,
            baseUrl = baseUrl,
        )
    }
}

private fun TextComposer.handleInlineQuote(
    element: Element,
    lazyListScope: LazyListScope,
    imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    append("\"\"\"")
    appendTextChildren(
        element.childNodes(),
        lazyListScope = lazyListScope,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )
    append("\"\"\"")
}

private fun TextComposer.handleAbbreviation(
    element: Element,
    lazyListScope: LazyListScope,
    imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val title = element.attr("title")
    withAnnotation("TOOLTIP", title) {
        appendTextChildren(
            element.childNodes(),
            lazyListScope = lazyListScope,
            imagePlaceholder = imagePlaceholder,
            onLinkClick = onLinkClick,
            baseUrl = baseUrl,
        )
    }
}

private fun LazyListScope.handleHorizontalRule() {
    item {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}

private fun String.asFontFamily(): FontFamily? = when (this.lowercase()) {
    "monospace"  -> FontFamily.Monospace
    "serif"      -> FontFamily.Serif
    "sans-serif" -> FontFamily.SansSerif
    else         -> null
}

@Preview
@Composable
private fun TestIt() {
    val context = LocalContext.current
    val html = """
        <p>In Gimp you go to <em>Image</em> in the top menu bar and select <em>Mode</em> followed by <em>Indexed</em>. Now you see a popup where you can select the number of colors for a generated optimum palette.</p> <p>You&rsquo;ll have to experiment a little because it will depend on your image.</p> <p>I used this approach to shrink the size of the cover image in <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">the_zopfli post</a> from a 37KB (JPG) to just 15KB (PNG, all PNG sizes listed include Zopfli compression btw).</p> <h2 id="straight-jpg-to-png-conversion-124kb">Straight JPG to PNG conversion: 124KB</h2> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things.png" alt="PNG version RGB colors" /></p> <p>First off, I exported the JPG file as a PNG file. This PNG file had a whopping 124KB! Clearly there was some bloat being stored.</p> <h2 id="256-colors-40kb">256 colors: 40KB</h2> <p>Reducing from RGB to only 256 colors has no visible effect to my eyes.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_256.png" alt="256 colors" /></p> <h2 id="128-colors-34kb">128 colors: 34KB</h2> <p>Still no difference.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_128.png" alt="128 colors" /></p> <h2 id="64-colors-25kb">64 colors: 25KB</h2> <p>You can start to see some artifacting in the shadow behind the text.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_64.png" alt="64 colors" /></p> <h2 id="32-colors-15kb">32 colors: 15KB</h2> <p>In my opinion this is the sweet spot. The shadow artifacting is barely noticable but the size is significantly reduced.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_32.png" alt="32 colors" /></p> <h2 id="16-colors-11kb">16 colors: 11KB</h2> <p>Clear artifacting in the text shadow and the yellow (fire?) in the background has developed an outline.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_16.png" alt="16 colors" /></p> <h2 id="8-colors-7-3kb">8 colors: 7.3KB</h2> <p>The broom has shifted in color from a clear brown to almost grey. Text shadow is just a grey blob at this point. Even clearer outline developed on the yellow background.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_8.png" alt="8 colors" /></p> <h2 id="4-colors-4-3kb">4 colors: 4.3KB</h2> <p>Interestingly enough, I think 4 colors looks better than 8 colors. The outline in the background has disappeared because there&rsquo;s not enough color spectrum to render it. The broom is now black and filled areas tend to get a white separator to the outlines.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_4.png" alt="4 colors" /></p> <h2 id="2-colors-2-4kb">2 colors: 2.4KB</h2> <p>Well, at least the silhouette is well defined at this point I guess.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_2.png" alt="2 colors" /></p> <hr/> <p>Other posts in the <b>Migrating from Ghost to Hugo</b> series:</p> <ul class="series"> <li>2016-10-21 &mdash; Reduce the size of images even further by reducing number of colors with Gimp </li> <li>2016-08-26 &mdash; <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">Compress all the images!</a> </li> <li>2016-07-25 &mdash; <a href="https://cowboyprogrammer.org/2016/07/migrating_from_ghost_to_hugo/">Migrating from Ghost to Hugo</a> </li> </ul>
    """.trimIndent()

    html.byteInputStream().use { stream ->
        LazyColumn {
            htmlFormattedText(
                inputStream = stream,
                baseUrl = "https://cowboyprogrammer.org",
                imagePlaceholder = R.drawable.placeholder_image_article_day,
                onLinkClick = context::launchView
            )
        }
    }
}

@Composable
private fun pixelDensity() = with(LocalDensity.current) {
    density
}

@Composable
fun BoxWithConstraintsScope.maxImageWidth() = with(LocalDensity.current) {
    maxWidth.toPx().roundToInt().coerceAtMost(2000)
}

/**
 * Gets the url to the image in the <img> tag - could be from srcset or from src
 */
internal fun getImageSource(baseUrl: String, element: Element) = ImageCandidates(
    baseUrl = baseUrl,
    srcSet = element.attr("srcset") ?: "",
    absSrc = element.attr("abs:src") ?: "",
)

internal class ImageCandidates(
    val baseUrl: String,
    val srcSet: String,
    val absSrc: String
) {
    val hasImage: Boolean = srcSet.isNotBlank() || absSrc.isNotBlank()

    /**
     * Might throw if hasImage returns false
     */
    fun getBestImageForMaxSize(maxWidth: Int, pixelDensity: Float): String {
        val setCandidate = srcSet.splitToSequence(",")
            .map { it.trim() }
            .map { it.split(SpaceRegex).take(2).map { x -> x.trim() } }
            .fold(100f to "") { acc, candidate ->
                val candidateSize = if (candidate.size == 1) {
                    // Assume it corresponds to 1x pixel density
                    1.0f / pixelDensity
                } else {
                    val descriptor = candidate.last()
                    when {
                        descriptor.endsWith("w", ignoreCase = true) -> {
                            descriptor.substringBefore("w").toFloat() / maxWidth
                                .toFloat()
                        }

                        descriptor.endsWith("x", ignoreCase = true) -> {
                            descriptor.substringBefore("x").toFloat() / pixelDensity
                        }

                        else                                        -> {
                            return@fold acc
                        }
                    }
                }

                if (abs(candidateSize - 1.0f) < abs(acc.first - 1.0f)) {
                    candidateSize to candidate.first()
                } else {
                    acc
                }
            }
            .second

        if (setCandidate.isNotBlank()) {
            return StringUtil.resolve(baseUrl, setCandidate)
        }
        return StringUtil.resolve(baseUrl, absSrc)
    }
}

private val SpaceRegex = Regex("\\s+")

/**
 * Can't use JSoup's text() method because that strips invisible characters
 * such as ZWNJ which are crucial for several languages.
 */
fun TextNode.appendCorrectlyNormalizedWhiteSpace(
    builder: TextComposer,
    stripLeading: Boolean
) {
    val string = wholeText

    var reachedNonWhite = false
    var lastWasWhite = false
    var i = 0
    while (i < string.length) {
        val code = string.codePointAt(i)

        // Unicode smileys are an example of where toChar() won't work. Needs to be String.
        val char = String(intArrayOf(code), 0, 1)
        i += Character.charCount(code)

        lastWasWhite = if (isCollapsableWhiteSpace(char)) {
            if (!(stripLeading && !reachedNonWhite || lastWasWhite)) {
                builder.append(' ')
            }
            true
        } else {
            reachedNonWhite = true
            builder.append(char)
            false
        }
    }
}

fun Element.appendCorrectlyNormalizedWhiteSpaceRecursively(
    builder: TextComposer,
    stripLeading: Boolean
) {
    for (child in childNodes()) {
        when (child) {
            is TextNode -> child.appendCorrectlyNormalizedWhiteSpace(builder, stripLeading)
            is Element  -> child.appendCorrectlyNormalizedWhiteSpaceRecursively(
                builder,
                stripLeading
            )
        }
    }
}

private const val space = ' '
private const val tab = '\t'
private const val linefeed = '\n'
private const val carriageReturn = '\r'

// 12 is form feed which as no escape in kotlin
private const val formFeed = 12.toChar()

// 160 is &nbsp; (non-breaking space). Not in the spec but expected.
private const val nonBreakableSpace = 160.toChar()

private fun isCollapsableWhiteSpace(c: String) =
    c.firstOrNull()?.let { isCollapsableWhiteSpace(it) } ?: false

private fun isCollapsableWhiteSpace(c: Char) =
    c == space || c == tab || c == linefeed || c == carriageReturn || c == formFeed || c == nonBreakableSpace

/**
 * Super basic function to strip html formatting from alt-texts.
 */
fun stripHtml(html: String): String {
    val result = StringBuilder()

    var skipping = false

    for (char in html) {
        if (!skipping) {
            if (char == '<') {
                skipping = true
            } else {
                result.append(char)
            }
        } else {
            if (char == '>') {
                skipping = false
            } else {
                // Skipping char
            }
        }
    }

    return result.toString()
}
