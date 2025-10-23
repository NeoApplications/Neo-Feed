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

package com.saulhdev.feeder.utils.extensions

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

const val LIGHT_BORDER = 0.5f

fun Int.isLight() = ColorUtils.calculateLuminance(this) > LIGHT_BORDER
fun Int.isDark() = ColorUtils.calculateLuminance(this) < LIGHT_BORDER

fun Bundle.dump(tag: String) {
    keySet().forEach {
        val item = get(it)
        item ?: return@forEach
        Log.d(tag, "[$it] $item")
    }
}

fun Window.setLightFlags() {
    val controller = WindowCompat.getInsetsController(this, this.decorView)
    controller.isAppearanceLightStatusBars = true
    controller.isAppearanceLightNavigationBars = true
}

fun Window.clearLightFlags() {
    val controller = WindowCompat.getInsetsController(this, this.decorView)
    controller.isAppearanceLightStatusBars = false
    controller.isAppearanceLightNavigationBars = false
}

@ColorInt
fun Color?.toInt(): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return Color.BLACK
    this ?: Color.BLACK
    return Color.rgb(this!!.red(), this.green(), this.blue())
}
