package com.saulhdev.feeder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    // TODO add neutral using ENUM
    positive: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            contentColor = when {
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else     -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            containerColor = when {
                positive -> MaterialTheme.colorScheme.primaryContainer
                else     -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (icon != null) Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun OutlinedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    // TODO add neutral using ENUM
    positive: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = when {
                positive -> MaterialTheme.colorScheme.primary
                else     -> MaterialTheme.colorScheme.tertiary
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                positive -> MaterialTheme.colorScheme.primary
                else     -> MaterialTheme.colorScheme.tertiary
            },
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (icon != null) Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun SingleChoiceSegmentedButtonRowScope.SegmentedTabButton(
    text: String,
    icon: ImageVector,
    selected: () -> Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedButton(
        modifier = modifier,
        selected = selected(),
        onClick = onClick,
        border = BorderStroke(0.dp, Color.Transparent),
        colors = SegmentedButtonDefaults.colors(
            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            inactiveContainerColor = Color.Transparent,
            inactiveContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Icon(imageVector = icon, contentDescription = text)
        }
    ) {
        Text(text = text)
    }
}

@Composable
fun itemShape(index: Int, count: Int, selected: () -> Boolean): Shape {
    if (count == 1 || selected()) return MaterialTheme.shapes.extraLarge

    return when (index) {
        0 -> MaterialTheme.shapes.extraLarge.copy(
            topEnd = CornerSize(4.dp),
            bottomEnd = CornerSize(4.dp)
        )

        count - 1 -> MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(4.dp),
            bottomStart = CornerSize(4.dp)
        )

        else -> MaterialTheme.shapes.extraSmall
    }
}