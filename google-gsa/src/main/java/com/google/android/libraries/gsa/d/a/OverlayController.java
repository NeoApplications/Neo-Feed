package com.google.android.libraries.gsa.d.a;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.libraries.launcherclient.LauncherOverlayCallback;

import java.io.PrintWriter;

public class OverlayController extends DialogOverlayController {

    public boolean isRtl;
    public long lastTouchTime = 0;
    public int windowShift;
    public String packageName;
    public SlidingPanelLayout slidingPanelLayout;
    public final PanelController panelController = new OverlayControllerStateChanger(this);
    public FrameLayout container;
    public int touchStartX = 0;
    public boolean acceptExternalMove = false;
    public boolean isVisible = true;
    public LauncherOverlayCallback overlayCallback;
    public PanelState panelState = PanelState.CLOSED;

    private int activityStateFlags = 0;

    public OverlayController(Context context, int theme, int dialogTheme) {
        super(context, theme, dialogTheme);
    }

    final void simulateMotionEvent(int action, int position, long eventTime) {
        float x = isRtl ? -position : position;
        MotionEvent event = MotionEvent.obtain(lastTouchTime, eventTime, action, x, 0.0f, 0);
        event.setSource(MotionEvent.ACTION_MOVE);
        slidingPanelLayout.dispatchTouchEvent(event);
        event.recycle();
    }

    public void onOptionsUpdated(Bundle options) {
    }

    final LauncherOverlayCallback closeOverlay() {
        updateActivityState(0);
        unregisterBackCallbackIfNeeded();
        try {
            windowManager.removeView(windowView);
        } catch (Throwable ignored) {
        }
        windowView = null;
        dismissAllDialogs();
        onDestroy();
        return overlayCallback;
    }

    final void updateActivityState(int newState) {
        if (activityStateFlags != newState) {
            boolean wasStarted = (activityStateFlags & 1) != 0;
            boolean wasResumed = (activityStateFlags & 2) != 0;
            boolean shouldStart = (newState & 1) != 0;
            boolean shouldResume = (newState & 2) != 0;
            boolean shouldBeVisible = shouldStart || shouldResume;

            activityStateFlags = (shouldBeVisible ? 1 : 0) | (shouldResume ? 2 : 0);

            if (!wasStarted && shouldBeVisible) onStart();
            if (!wasResumed && shouldResume) onResume();
            if (wasResumed && !shouldResume) onPause();
            if (wasStarted && !shouldBeVisible) onStop();
        }
    }

    public void closePanelIfNeeded(int flags) {
        if (isOpen()) {
            boolean shouldClose = (flags & 1) != 0;
            if (panelState == PanelState.OPEN_AS_LAYER) {
                shouldClose = false;
            }
            int duration = shouldClose ? 750 : 0;
            slidingPanelLayout.closePanel(duration);
            dismissAllDialogs();
        }
    }

    public final void openPanelIfNeeded(int flags) {
        if (panelState == PanelState.CLOSED) {
            boolean asDrawer = (flags & 1) != 0;
            boolean transparent = (flags & 2) != 0;

            if (transparent) {
                slidingPanelLayout.panelController = new TransparentOverlayController(this);
                asDrawer = false;
            }

            int duration = asDrawer ? 750 : 0;
            slidingPanelLayout.startSettlingTo(duration, 0);
        }
    }

    public void onBackPressed() {
        closePanelIfNeeded(1);
    }

    public void dump(PrintWriter out, String prefix) {
        out.println(prefix + "windowShift: " + windowShift);
        out.println(prefix + "acceptExternalMove: " + acceptExternalMove);
        out.println(prefix + "panelState: " + panelState);
        out.println(prefix + "activityStateFlags: " + activityStateFlags);
        out.println(prefix + "slidingPanelLayout: " + slidingPanelLayout);

        String subPrefix = prefix + "  ";
        out.println(subPrefix + "panelPositionRatio: " + slidingPanelLayout.panelPositionRatio);
        out.println(subPrefix + "downX: " + slidingPanelLayout.downX);
        out.println(subPrefix + "downY: " + slidingPanelLayout.downY);
        out.println(subPrefix + "activePointerId: " + slidingPanelLayout.activePointerId);
        out.println(subPrefix + "touchState: " + slidingPanelLayout.touchState);
        out.println(subPrefix + "isPanelOpen: " + slidingPanelLayout.isPanelOpen);
        out.println(subPrefix + "isPageMoving: " + slidingPanelLayout.isPageMoving);
        out.println(subPrefix + "settling: " + slidingPanelLayout.settling);
        out.println(subPrefix + "forceDrag: " + slidingPanelLayout.forceDrag);
    }
    public void Hn() {
    }

    public void onCreate(Bundle savedInstanceState) {}
    private void onPause() {}
    private void onStop() {}
    private void onStart() {}
    public void onResume() {}
    public void onDestroy() {}

    public void setTitle(CharSequence title) {
        window.setTitle(title);
    }

    public Object getSystemService(String name) {
        if ("window".equals(name) && windowManager != null) {
            return windowManager;
        }
        return super.getSystemService(name);
    }

    public boolean isOpen() {
        return panelState == PanelState.OPEN_AS_DRAWER || panelState == PanelState.OPEN_AS_LAYER;
    }

    public void setFocusable(boolean focusable) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        int flagsToChange = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        boolean changed = false;

        if (focusable) {
            if ((attrs.flags & flagsToChange) != 0) {
                attrs.flags &= ~flagsToChange;
                changed = true;
            }
        } else {
            if ((attrs.flags & flagsToChange) != flagsToChange) {
                attrs.flags |= flagsToChange;
                changed = true;
            }
        }

        if (changed) {
            window.setAttributes(attrs);
            if (focusable && windowView != null) {
                windowView.requestFocus();
            }
        }
    }

    public void setWindowAlpha(float alpha) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        float targetAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        if (attrs.alpha != targetAlpha) {
            attrs.alpha = targetAlpha;
            window.setAttributes(attrs);
        }
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        setFocusable(visible);
        setWindowAlpha(visible ? 1.0f : 0.0f);
    }

    public void setState(PanelState newState) {
        this.panelState = newState;
    }

    public boolean shouldHandleInput() {
        return false;
    }

    public void onScroll(float distance) {}

    public void applyByteBundle(ByteBundleHolder holder) {}

    public void configurePanel(boolean enable) {}
}
