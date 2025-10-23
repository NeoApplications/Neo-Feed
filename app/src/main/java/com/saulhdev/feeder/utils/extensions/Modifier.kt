package com.saulhdev.feeder.utils.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

/**
 * An object that is immutable means that ‘all publicly accessible properties and fields will not
 * change after the instance is constructed’. This characteristic means that Compose can detect
 * ‘changes’ between two instances very easily.
 *
 * Some types - such as List - can't be inferred as stable.
 * This class is useful to wrap them to improve performance.
 *
 * See https://chris.banes.dev/composable-metrics/
 */
@Stable
data class StableHolder<T>(val item: T) {
    override fun toString(): String {
        return item.toString()
    }
}

fun Modifier.safeSemantics(
    mergeDescendants: Boolean = false,
    properties: (SemanticsPropertyReceiver.() -> Unit),
): Modifier =
    semantics(mergeDescendants = mergeDescendants) {
        try {
            properties()
        } catch (_: Exception) {
            // Bug in framework? This can be null in any case
        }
    }

fun Modifier.blockBorder() = composed {
    this
        .clip(MaterialTheme.shapes.extraLarge)
        .border(
            2.dp,
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.shapes.extraLarge,
        )
}

fun Modifier.blockShadow() =
    composed {
        this
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    }