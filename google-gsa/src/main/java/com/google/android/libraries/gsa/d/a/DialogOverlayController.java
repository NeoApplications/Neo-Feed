package com.google.android.libraries.gsa.d.a;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class DialogOverlayController extends ContextThemeWrapper implements Window.Callback, DialogListeners {

    public WindowManager windowManager;
    public Window window;
    private final Set<DialogInterface> dialogs = Collections.synchronizedSet(new HashSet<>());
    public View windowView;

    DialogOverlayController(Context context, int theme, int dialogTheme) {
        super(context, theme);
        Dialog dialog = new Dialog(context, dialogTheme);
        this.window = dialog.getWindow();
        if (this.window != null) {
            this.window.setCallback(this);
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // Replaced magic number
        } else {
            throw new IllegalStateException("Dialog window cannot be null");
        }
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    void onBackPressed() {
        // Override in subclass or implement logic here
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
            onBackPressed();
            return true;
        }
        return window.superDispatchKeyEvent(event);
    }

    @Override public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return window.superDispatchKeyShortcutEvent(event);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        return window.superDispatchTouchEvent(event);
    }

    @Override public boolean dispatchTrackballEvent(MotionEvent event) {
        return window.superDispatchTrackballEvent(event);
    }

    @Override public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return window.superDispatchGenericMotionEvent(event);
    }

    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    @Override public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return false;
    }

    @Override public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return true;
    }

    @Override public boolean onMenuOpened(int featureId, Menu menu) {
        return true;
    }

    @Override public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (windowView != null && windowManager != null) {
            windowManager.updateViewLayout(windowView, params);
        }
    }

    @Override public void onContentChanged() {}
    @Override public void onWindowFocusChanged(boolean hasFocus) {}
    @Override public void onAttachedToWindow() {}
    @Override public void onDetachedFromWindow() {}
    @Override public void onPanelClosed(int featureId, Menu menu) {}

    @Override public boolean onSearchRequested() {
        return false;
    }

    @Override public boolean onSearchRequested(SearchEvent event) {
        return false;
    }

    @Override public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    @Override public void onActionModeStarted(ActionMode mode) {}
    @Override public void onActionModeFinished(ActionMode mode) {}

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        super.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), options);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        dialogs.add(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialogs.remove(dialog);
    }

    void dismissAllDialogs() {
        synchronized (dialogs) {
            for (DialogInterface dialog : dialogs.toArray(new DialogInterface[0])) {
                if (dialog instanceof Dialog) {
                    ((Dialog) dialog).dismiss();
                }
            }
            dialogs.clear();
        }
    }

    public void setWindowView(View view) {
        this.windowView = view;
    }

    public View getWindowView() {
        return windowView;
    }

    public Window getWindow() {
        return window;
    }
}
