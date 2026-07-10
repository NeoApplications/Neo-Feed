package com.google.android.libraries.gsa.d.a;

import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.libraries.launcherclient.LauncherOverlayCallback;

import java.io.PrintWriter;

abstract class OverlayControllerCallback extends BaseCallback {

    final OverlayControllerBinder binder;
    private final int overlayId;
    OverlayController overlayController;

    abstract OverlayController createController(Configuration configuration);

    OverlayControllerCallback(OverlayControllerBinder binder, int overlayId) {
        this.binder = binder;
        this.overlayId = overlayId;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d("OverlayControllerCallback", "handleMessage: " + msg.what);
        return switch (msg.what) {
            case 0 -> handleInit(msg);
            case 1 -> handleSetState(msg);
            case 2 -> handleDestroy(msg);
            case 6 -> handleCommand(msg);
            case 7 -> handleVisibility(msg);
            case 8 -> handleByteBundle(msg);
            default -> false;
        };
    }

    private boolean handleInit(Message msg) {
        if (msg.arg1 == 0) return true;

        Bundle stateBundle = null;

        if (overlayController != null) {
            stateBundle = saveOverlayState(overlayController);
            overlayController.closeOverlay();
            overlayController = null;
        }

        Pair<?, ?> pair = (Pair<?, ?>) msg.obj;
        Bundle args = (Bundle) pair.first;
        LayoutParams layoutParams = args.getParcelable("layout_params");

        overlayController = createController(args.getParcelable("configuration"));

        try {
            setupOverlayController(overlayController, args, layoutParams);
            restoreOverlayStateIfNeeded(stateBundle);

            overlayController.overlayCallback = (LauncherOverlayCallback) pair.second;
            overlayController.configurePanel(true);
            binder.a((LauncherOverlayCallback) pair.second, overlayId);
            overlayController.onOptionsUpdated(args);
        } catch (Throwable e) {
            // fallback in case of error
            Message fallback = Message.obtain();
            fallback.what = 2;
            handleMessage(fallback);
            fallback.recycle();
        }

        return true;
    }

    private boolean handleSetState(Message msg) {
        if (overlayController == null) return true;
        overlayController.updateActivityState((Integer) msg.obj);
        return true;
    }

    private boolean handleDestroy(Message msg) {
        if (overlayController == null) return true;
        LauncherOverlayCallback callback = overlayController.closeOverlay();
        overlayController = null;
        if (msg.arg1 == 0) {
            binder.a(callback, 0);
        }
        return true;
    }

    private boolean handleCommand(Message msg) {
        if (overlayController == null) return true;
        int param = msg.arg2 & 1;
        if (msg.arg1 == 1) {
            overlayController.openPanelIfNeeded(param);
        } else {
            overlayController.closePanelIfNeeded(param);
        }
        return true;
    }

    private boolean handleVisibility(Message msg) {
        if (overlayController == null) return true;
        overlayController.configurePanel(msg.arg1 == 1);
        return true;
    }

    private boolean handleByteBundle(Message msg) {
        if (overlayController == null) return true;
        overlayController.applyByteBundle((ByteBundleHolder) msg.obj);
        return true;
    }

    private Bundle saveOverlayState(OverlayController controller) {
        Bundle state = new Bundle();
        if (controller.panelState == PanelState.OPEN_AS_DRAWER) {
            state.putBoolean("open", true);
        }
        state.putParcelable("view_state", controller.window.saveHierarchyState());
        return state;
    }

    private void restoreOverlayStateIfNeeded(Bundle state) {
        if (state == null) return;

        overlayController.window.restoreHierarchyState(state.getBundle("view_state"));

        if (state.getBoolean("open")) {
            SlidingPanelLayout panel = overlayController.slidingPanelLayout;
            panel.panelPositionRatio = 1.0f;
            panel.panelOffsetPx = panel.getMeasuredWidth();
            float translationX = panel.isRtl ? -panel.panelOffsetPx : panel.panelOffsetPx;
            panel.foregroundPanel.setTranslationX(translationX);
            panel.startPanelDrag();
            panel.onPanelFullyOpened();
        }
    }

    private void setupOverlayController(OverlayController controller, Bundle args, LayoutParams layoutParams) {
        controller.isRtl = SlidingPanelLayout.isRtl(controller.getResources());
        controller.packageName = binder.getPackageName();

        controller.window.setWindowManager(null, layoutParams.token,
                new ComponentName(controller, controller.getBaseContext().getClass()).flattenToShortString(), true);

        controller.windowManager = controller.window.getWindowManager();

        Point size = new Point();
        controller.windowManager.getDefaultDisplay().getRealSize(size);
        controller.windowShift = -Math.max(size.x, size.y);

        controller.slidingPanelLayout = new OverlayControllerSlidingPanelLayout(controller);
        controller.container = new FrameLayout(controller);
        controller.slidingPanelLayout.setForegroundPanel(controller.container);
        controller.slidingPanelLayout.panelController = controller.panelController;

        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        // Start fully transparent, non-focusable and non-touchable so the overlay
        // never briefly covers the launcher while it is being attached.
        layoutParams.flags |= 8650752
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.alpha = 0f;
        layoutParams.dimAmount = 0f;
        layoutParams.gravity = 3;
        layoutParams.type = 4;
        // Don't override the launcher window's soft-input behaviour; otherwise
        // the app-drawer search box can't show its own keyboard.
        layoutParams.softInputMode = LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;

        controller.window.setAttributes(layoutParams);
        controller.window.clearFlags(1048576);
        controller.onCreate(args);
        controller.window.setContentView(controller.slidingPanelLayout);

        controller.windowView = controller.window.getDecorView();
        controller.windowManager.addView(controller.windowView, controller.window.getAttributes());
        controller.registerBackCallbackIfNeeded();

        controller.slidingPanelLayout.setSystemUiVisibility(1792);
        controller.setVisible(false);
        controller.windowView.addOnLayoutChangeListener(new OverlayControllerLayoutChangeListener(controller));
    }

    @Override
    public void dump(PrintWriter writer, @NonNull String prefix) {
        writer.println(prefix + " mView: " + overlayController);
        if (overlayController != null) {
            overlayController.dump(writer, prefix + "  ");
        }
    }
}
