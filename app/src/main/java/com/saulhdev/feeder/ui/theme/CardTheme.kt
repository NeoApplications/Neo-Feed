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

package com.saulhdev.feeder.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.util.SparseIntArray
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.NeoApp
import com.saulhdev.feeder.R

object CardTheme {
    val defaultLightThemeColors = createLightTheme()
    val defaultDarkThemeColors = createDarkTheme()
    val defaultBlackThemeColors = createBlackTheme()

    private fun createLightTheme(): SparseIntArray =
        SparseIntArray().apply {
            addBasicThings(this)
            put(
                Colors.CARD_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_surface_container,
                ),
            )
            put(
                Colors.TEXT_COLOR_PRIMARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_on_surface,
                ),
            )
            put(
                Colors.TEXT_COLOR_SECONDARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_on_surface_variant,
                ),
            )
            put(
                Colors.OVERLAY_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_background,
                ),
            )
            put(Colors.IS_LIGHT.ordinal, 1)
        }

    private fun createDarkTheme(): SparseIntArray =
        SparseIntArray().apply {
            addBasicThings(this)
            put(
                Colors.CARD_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface_container,
                ),
            )
            put(
                Colors.TEXT_COLOR_PRIMARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface,
                ),
            )
            put(
                Colors.TEXT_COLOR_SECONDARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface_variant,
                ),
            )
            put(
                Colors.OVERLAY_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_background,
                ),
            )
            put(Colors.IS_LIGHT.ordinal, 0)
        }

    private fun createBlackTheme(): SparseIntArray =
        SparseIntArray().apply {
            addBasicThings(this)
            put(
                Colors.CARD_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    android.R.color.black,
                ),
            )
            put(
                Colors.TEXT_COLOR_PRIMARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface,
                ),
            )
            put(
                Colors.TEXT_COLOR_SECONDARY.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface_variant,
                ),
            )
            put(
                Colors.OVERLAY_BG.ordinal,
                ContextCompat.getColor(
                    NeoApp.instance!!,
                    android.R.color.black,
                ),
            )
            put(Colors.IS_LIGHT.ordinal, 0)
        }

    private fun addBasicThings(array: SparseIntArray): SparseIntArray =
        array.apply {
            put(
                Colors.ACCENT_COLOR.ordinal,
                ContextCompat.getColor(NeoApp.instance!!, R.color.globalAccent),
            )
        }

    fun getThemeBySystem(
        ctx: Context,
        black: Boolean,
    ): SparseIntArray =
        when {
            Configuration.UI_MODE_NIGHT_YES ==
                ctx.resources.configuration.uiMode
                    .and(Configuration.UI_MODE_NIGHT_MASK) && black
            -> createBlackTheme()

            Configuration.UI_MODE_NIGHT_YES ==
                ctx.resources.configuration.uiMode
                    .and(Configuration.UI_MODE_NIGHT_MASK)
            -> createDarkTheme()

            else -> createLightTheme()
        }

    enum class Colors {
        CARD_BG,
        TEXT_COLOR_PRIMARY,
        TEXT_COLOR_SECONDARY,
        ACCENT_COLOR,
        OVERLAY_BG,
        IS_LIGHT,
    }
}