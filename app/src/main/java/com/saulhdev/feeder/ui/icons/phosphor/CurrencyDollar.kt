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

val Phosphor.CurrencyDollar: ImageVector
    get() {
        if (_currencyDollar != null) {
            return _currencyDollar!!
        }
        _currencyDollar =
            Builder(
                name = "CurrencyDollar",
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
                    moveTo(152.0f, 120.0f)
                    horizontalLineTo(136.0f)
                    verticalLineTo(56.0f)
                    horizontalLineToRelative(8.0f)
                    arcToRelative(32.0f, 32.0f, 0.0f, false, true, 32.0f, 32.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                    arcToRelative(48.05f, 48.05f, 0.0f, false, false, -48.0f, -48.0f)
                    horizontalLineToRelative(-8.0f)
                    verticalLineTo(24.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                    verticalLineTo(40.0f)
                    horizontalLineToRelative(-8.0f)
                    arcToRelative(48.0f, 48.0f, 0.0f, false, false, 0.0f, 96.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(64.0f)
                    horizontalLineTo(104.0f)
                    arcToRelative(32.0f, 32.0f, 0.0f, false, true, -32.0f, -32.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                    arcToRelative(48.05f, 48.05f, 0.0f, false, false, 48.0f, 48.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineToRelative(16.0f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                    verticalLineTo(216.0f)
                    horizontalLineToRelative(16.0f)
                    arcToRelative(48.0f, 48.0f, 0.0f, false, false, 0.0f, -96.0f)
                    close()
                    moveTo(112.0f, 120.0f)
                    arcToRelative(32.0f, 32.0f, 0.0f, false, true, 0.0f, -64.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(64.0f)
                    close()
                    moveTo(152.0f, 200.0f)
                    horizontalLineTo(136.0f)
                    verticalLineTo(136.0f)
                    horizontalLineToRelative(16.0f)
                    arcToRelative(32.0f, 32.0f, 0.0f, false, true, 0.0f, 64.0f)
                    close()
                }
            }.build()
        return _currencyDollar!!
    }

private var _currencyDollar: ImageVector? = null
