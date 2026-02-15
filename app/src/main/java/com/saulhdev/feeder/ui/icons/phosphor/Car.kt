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

val Phosphor.Car: ImageVector
    get() {
        if (_car != null) {
            return _car!!
        }
        _car =
            Builder(
                name = "Car",
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
                    moveTo(240.0f, 104.0f)
                    horizontalLineTo(229.2f)
                    lineTo(201.42f, 41.5f)
                    arcTo(16.0f, 16.0f, 0.0f, false, false, 186.8f, 32.0f)
                    horizontalLineTo(69.2f)
                    arcToRelative(16.0f, 16.0f, 0.0f, false, false, -14.62f, 9.5f)
                    lineTo(26.8f, 104.0f)
                    horizontalLineTo(16.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(80.0f)
                    arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                    horizontalLineTo(64.0f)
                    arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                    verticalLineTo(184.0f)
                    horizontalLineToRelative(96.0f)
                    verticalLineToRelative(16.0f)
                    arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                    horizontalLineToRelative(24.0f)
                    arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                    verticalLineTo(120.0f)
                    horizontalLineToRelative(8.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                    close()
                    moveTo(69.2f, 48.0f)
                    horizontalLineTo(186.8f)
                    lineToRelative(24.89f, 56.0f)
                    horizontalLineTo(44.31f)
                    close()
                    moveTo(64.0f, 200.0f)
                    horizontalLineTo(40.0f)
                    verticalLineTo(184.0f)
                    horizontalLineTo(64.0f)
                    close()
                    moveTo(192.0f, 200.0f)
                    verticalLineTo(184.0f)
                    horizontalLineToRelative(24.0f)
                    verticalLineToRelative(16.0f)
                    close()
                    moveTo(216.0f, 168.0f)
                    horizontalLineTo(40.0f)
                    verticalLineTo(120.0f)
                    horizontalLineTo(216.0f)
                    close()
                    moveTo(56.0f, 144.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                    horizontalLineTo(80.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                    horizontalLineTo(64.0f)
                    arcTo(8.0f, 8.0f, 0.0f, false, true, 56.0f, 144.0f)
                    close()
                    moveTo(168.0f, 144.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                    horizontalLineToRelative(16.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                    horizontalLineTo(176.0f)
                    arcTo(8.0f, 8.0f, 0.0f, false, true, 168.0f, 144.0f)
                    close()
                }
            }.build()
        return _car!!
    }

private var _car: ImageVector? = null
