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

val Phosphor.Trophy: ImageVector
    get() {
        if (_trophy != null) {
            return _trophy!!
        }
        _trophy =
            Builder(
                name = "Trophy",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 256.0f,
                viewportHeight = 256.0f,
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
                    moveTo(232.0f, 64.0f)
                    horizontalLineTo(208.0f)
                    verticalLineTo(48.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                    horizontalLineTo(56.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                    verticalLineTo(64.0f)
                    horizontalLineTo(24.0f)
                    arcTo(16.0f, 16.0f, 0.0f, false, false, 8.0f, 80.0f)
                    verticalLineTo(96.0f)
                    arcToRelative(40.0f, 40.0f, 0.0f, false, false, 40.0f, 40.0f)
                    horizontalLineToRelative(3.65f)
                    arcTo(80.13f, 80.13f, 0.0f, false, false, 120.0f, 191.61f)
                    verticalLineTo(216.0f)
                    horizontalLineTo(96.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                    horizontalLineToRelative(64.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                    horizontalLineTo(136.0f)
                    verticalLineTo(191.58f)
                    curveToRelative(31.94f, -3.23f, 58.44f, -25.64f, 68.08f, -55.58f)
                    horizontalLineTo(208.0f)
                    arcToRelative(40.0f, 40.0f, 0.0f, false, false, 40.0f, -40.0f)
                    verticalLineTo(80.0f)
                    arcTo(16.0f, 16.0f, 0.0f, false, false, 232.0f, 64.0f)
                    close()
                    moveTo(48.0f, 120.0f)
                    arcTo(24.0f, 24.0f, 0.0f, false, true, 24.0f, 96.0f)
                    verticalLineTo(80.0f)
                    horizontalLineTo(48.0f)
                    verticalLineToRelative(32.0f)
                    quadToRelative(0.0f, 4.0f, 0.39f, 8.0f)
                    close()
                    moveTo(192.0f, 111.1f)
                    curveToRelative(0.0f, 35.52f, -29.0f, 64.64f, -64.0f, 64.9f)
                    arcToRelative(64.0f, 64.0f, 0.0f, false, true, -64.0f, -64.0f)
                    verticalLineTo(56.0f)
                    horizontalLineTo(192.0f)
                    close()
                    moveTo(232.0f, 96.0f)
                    arcToRelative(24.0f, 24.0f, 0.0f, false, true, -24.0f, 24.0f)
                    horizontalLineToRelative(-0.5f)
                    arcToRelative(81.81f, 81.81f, 0.0f, false, false, 0.5f, -8.9f)
                    verticalLineTo(80.0f)
                    horizontalLineToRelative(24.0f)
                    close()
                }
            }.build()
        return _trophy!!
    }

private var _trophy: ImageVector? = null
