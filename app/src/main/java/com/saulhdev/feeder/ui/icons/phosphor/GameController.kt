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

val Phosphor.GameController: ImageVector
    get() {
        if (_gameController != null) {
            return _gameController!!
        }
        _gameController =
            Builder(
                name = "GameController",
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
                    moveTo(176.0f, 112.0f)
                    horizontalLineTo(152.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                    horizontalLineToRelative(24.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                    close()
                    moveTo(104.0f, 96.0f)
                    horizontalLineTo(96.0f)
                    verticalLineTo(88.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                    verticalLineToRelative(8.0f)
                    horizontalLineTo(72.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(8.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                    verticalLineToRelative(-8.0f)
                    horizontalLineToRelative(8.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                    close()
                    moveTo(241.48f, 200.65f)
                    arcToRelative(36.0f, 36.0f, 0.0f, false, true, -54.94f, 4.81f)
                    curveToRelative(-0.12f, -0.12f, -0.24f, -0.24f, -0.35f, -0.37f)
                    lineTo(146.48f, 160.0f)
                    horizontalLineToRelative(-37.0f)
                    lineTo(69.81f, 205.09f)
                    lineToRelative(-0.35f, 0.37f)
                    arcTo(36.08f, 36.08f, 0.0f, false, true, 44.0f, 216.0f)
                    arcTo(36.0f, 36.0f, 0.0f, false, true, 8.56f, 173.75f)
                    arcToRelative(0.68f, 0.68f, 0.0f, false, true, 0.0f, -0.14f)
                    lineTo(24.93f, 89.52f)
                    arcTo(59.88f, 59.88f, 0.0f, false, true, 83.89f, 40.0f)
                    horizontalLineTo(172.0f)
                    arcToRelative(60.08f, 60.08f, 0.0f, false, true, 59.0f, 49.25f)
                    curveToRelative(0.0f, 0.06f, 0.0f, 0.12f, 0.0f, 0.18f)
                    lineToRelative(16.37f, 84.17f)
                    arcToRelative(0.68f, 0.68f, 0.0f, false, true, 0.0f, 0.14f)
                    arcTo(35.74f, 35.74f, 0.0f, false, true, 241.48f, 200.65f)
                    close()
                    moveTo(172.0f, 144.0f)
                    arcToRelative(44.0f, 44.0f, 0.0f, false, false, 0.0f, -88.0f)
                    horizontalLineTo(83.89f)
                    arcTo(43.9f, 43.9f, 0.0f, false, false, 40.68f, 92.37f)
                    lineToRelative(0.0f, 0.13f)
                    lineTo(24.3f, 176.59f)
                    arcTo(20.0f, 20.0f, 0.0f, false, false, 58.0f, 194.3f)
                    lineToRelative(41.92f, -47.59f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, 6.0f, -2.71f)
                    close()
                    moveTo(231.7f, 176.59f)
                    lineToRelative(-8.74f, -45.0f)
                    arcTo(60.0f, 60.0f, 0.0f, false, true, 172.0f, 160.0f)
                    horizontalLineToRelative(-4.2f)
                    lineTo(198.0f, 194.31f)
                    arcToRelative(20.09f, 20.09f, 0.0f, false, false, 17.46f, 5.39f)
                    arcToRelative(20.0f, 20.0f, 0.0f, false, false, 16.23f, -23.11f)
                    close()
                }
            }.build()
        return _gameController!!
    }

private var _gameController: ImageVector? = null
