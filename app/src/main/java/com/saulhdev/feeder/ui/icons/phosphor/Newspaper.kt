package com.saulhdev.feeder.ui.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.icons.Phosphor

val Phosphor.Newspaper: ImageVector
    get() {
        if (_newspaper != null) {
            return _newspaper!!
        }
        _newspaper =
            Builder(
                name = "Newspaper",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 256f,
                viewportHeight = 256f,
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero,
                ) {
                    moveTo(88f, 112f)
                    arcToRelative(8f, 8f, 0f, false, true, 8f, -8f)
                    horizontalLineToRelative(80f)
                    arcToRelative(8f, 8f, 0f, false, true, 0f, 16f)
                    lineTo(96f, 120f)
                    arcTo(8f, 8f, 0f, false, true, 88f, 112f)
                    close()
                    moveTo(96f, 152f)
                    horizontalLineToRelative(80f)
                    arcToRelative(8f, 8f, 0f, false, false, 0f, -16f)
                    lineTo(96f, 136f)
                    arcToRelative(8f, 8f, 0f, false, false, 0f, 16f)
                    close()
                    moveTo(232f, 64f)
                    lineTo(232f, 184f)
                    arcToRelative(24f, 24f, 0f, false, true, -24f, 24f)
                    lineTo(32f, 208f)
                    arcTo(24f, 24f, 0f, false, true, 8f, 184.11f)
                    lineTo(8f, 88f)
                    arcToRelative(8f, 8f, 0f, false, true, 16f, 0f)
                    verticalLineToRelative(96f)
                    arcToRelative(8f, 8f, 0f, false, false, 16f, 0f)
                    lineTo(40f, 64f)
                    arcTo(16f, 16f, 0f, false, true, 56f, 48f)
                    lineTo(216f, 48f)
                    arcTo(16f, 16f, 0f, false, true, 232f, 64f)
                    close()
                    moveTo(216f, 64f)
                    lineTo(56f, 64f)
                    lineTo(56f, 184f)
                    arcToRelative(23.84f, 23.84f, 0f, false, true, -1.37f, 8f)
                    lineTo(208f, 192f)
                    arcToRelative(8f, 8f, 0f, false, false, 8f, -8f)
                    close()
                }
            }.build()
        return _newspaper!!
    }

private var _newspaper: ImageVector? = null
