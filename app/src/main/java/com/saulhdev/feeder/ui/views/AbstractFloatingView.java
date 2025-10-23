package com.saulhdev.feeder.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;

import com.saulhdev.feeder.ui.touch.TouchController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class AbstractFloatingView extends LinearLayout implements TouchController {

    @IntDef(flag = true, value = {
            TYPE_FILTER_SHEET
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FloatingViewType {
    }

    public static final int TYPE_FILTER_SHEET = 1 << 0;

    public static final int TYPE_ALL = TYPE_FILTER_SHEET; // Add other types as needed;

    protected boolean mIsOpen;
    public static ViewGroup container;

    public AbstractFloatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractFloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public abstract void close(boolean animate);

    public boolean isOpen() {
        return mIsOpen;
    }

    public static boolean isAnyOpen() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof AbstractFloatingView) {
                AbstractFloatingView abs = (AbstractFloatingView) child;
                if (abs.isOpen()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract boolean isOfType(@FloatingViewType int type);

    /**
     * @return Whether the back is consumed. If false, Launcher will handle the back as well.
     */
    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        return false;
    }

    public static void closeOpenViews(Context context, boolean animate, @FloatingViewType int type) {
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child instanceof AbstractFloatingView) {
                AbstractFloatingView abs = (AbstractFloatingView) child;
                Log.d("AbstractBottomSheet", "Closing view of type: " + type);
                if (abs.isOfType(type)) {
                    Log.d("AbstractBottomSheet", "Closing view of type2: " + type);
                    abs.close(animate);
                }
            }
        }
    }

    public static void closeAllOpenViews(Context context, boolean animate) {
        closeOpenViews(context, animate, TYPE_ALL);
    }

    public static void closeAllOpenViews(Context context) {
        closeAllOpenViews(context, true);
    }
}