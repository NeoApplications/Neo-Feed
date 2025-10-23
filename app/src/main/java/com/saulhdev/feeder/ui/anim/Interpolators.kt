package com.saulhdev.feeder.ui.anim

import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import kotlin.math.abs


class Interpolators {

    companion object{
        @JvmField
        val DEACCEL: Interpolator = DecelerateInterpolator()

        val SCROLL: Interpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1
        }

        @JvmField
        val FAST_OUT_SLOW_IN: Interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)

        @JvmField
        val SCROLL_CUBIC: Interpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t + 1
        }

        const val FAST_FLING_PX_MS: Float = 10f
        @JvmStatic
        fun scrollInterpolatorForVelocity(velocity: Float): Interpolator? {
            return if (abs(velocity) > FAST_FLING_PX_MS) SCROLL else SCROLL_CUBIC
        }

    }
}