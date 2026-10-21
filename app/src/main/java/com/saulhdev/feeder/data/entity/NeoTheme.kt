package com.saulhdev.feeder.data.entity

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.saulhdev.feeder.R
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed class NeoTheme {
    abstract val resId: Int
    abstract val nightMode: Int
    abstract val dynamicColor: Boolean
    abstract val seedColor: Long
    abstract val paletteStyle: Int
    abstract val blackOnDark: Boolean
    abstract val contrast: Double

    @Serializable
    data class CustomTheme(
        override val resId: Int,
        override val nightMode: Int,
        override val dynamicColor: Boolean,
        override val seedColor: Long,
        override val paletteStyle: Int,
        override val blackOnDark: Boolean,
        override val contrast: Double,
    ) : NeoTheme()

    @Serializable
    data object DynamicSystem : NeoTheme() {
        override val resId: Int
            get() = -1
        override val nightMode: Int
            get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        override val dynamicColor: Boolean
            get() = true
        override val seedColor: Long
            get() = 0xFFBBAADD
        override val paletteStyle: Int
            get() = PaletteStyle.TonalSpot.ordinal
        override val blackOnDark: Boolean
            get() = false
        override val contrast: Double
            get() = Contrast.Default.value
    }

    @Serializable
    data object SystemBlack : NeoTheme() {
        override val resId: Int
            get() = R.style.AppTheme
        override val nightMode: Int
            get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        override val dynamicColor: Boolean
            get() = false
        override val seedColor: Long
            get() = 0xFFBBAADD
        override val paletteStyle: Int
            get() = PaletteStyle.TonalSpot.ordinal
        override val blackOnDark: Boolean
            get() = true
        override val contrast: Double
            get() = Contrast.Default.value
    }

    @Serializable
    data object Light : NeoTheme() {
        override val resId: Int
            get() = R.style.AppTheme
        override val nightMode: Int
            get() = AppCompatDelegate.MODE_NIGHT_NO
        override val dynamicColor: Boolean
            get() = false
        override val seedColor: Long
            get() = 0xFFBBAADD
        override val paletteStyle: Int
            get() = PaletteStyle.TonalSpot.ordinal
        override val blackOnDark: Boolean
            get() = false
        override val contrast: Double
            get() = Contrast.Default.value
    }
}