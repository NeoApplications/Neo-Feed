package com.saulhdev.feeder.ui.views

import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.anim.Interpolators

class BaseBottomSheet @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : AbstractSlideInView(context, attrs, defStyleAttr){
    init {
        setWillNotDraw(false)
        mContent = this
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setTranslationShift(mTranslationShift)
    }

    fun show(view: View?, animate: Boolean) {
        (findViewById<View>(R.id.sheet_contents) as ViewGroup).addView(view)
        container.addView(this)
        mIsOpen = false
        animateOpen(animate)
    }

    override fun onCloseComplete() {
        super.onCloseComplete()
        clearNavBarColor()
    }

    override fun getScrimColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.botttom_sheet_scrim)
    }


    private fun animateOpen(animate: Boolean) {
        if (mIsOpen || mOpenCloseAnimator.isRunning) {
            return
        }
        mIsOpen = true
        setupNavBarColor()
        attachToContainer();
        mOpenCloseAnimator.setValues(
            PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED)
        )
        mOpenCloseAnimator.interpolator = Interpolators.FAST_OUT_SLOW_IN
        if (!animate) {
            mOpenCloseAnimator.duration = 0
        }
        mOpenCloseAnimator.start()
    }

    private fun clearNavBarColor() {
        //TODO reset to previous state instead of always light
    }

    private fun setupNavBarColor() {
        //TODO set to dark
    }

    override fun isOfType(type: Int): Boolean {
        return type and TYPE_FILTER_SHEET == 1
    }

    companion object{

        fun inflate(context: Context): BaseBottomSheet {
            return inflate(context, R.layout.base_bottom_sheet, null) as BaseBottomSheet
        }
    }
}