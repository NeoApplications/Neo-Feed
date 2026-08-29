package com.google.android.libraries.gsa.d.a;

import android.util.Log;

final class OverlayControllerStateChanger implements PanelController {

    private final OverlayController overlayController;

    OverlayControllerStateChanger(OverlayController overlayController) {
        this.overlayController = overlayController;
    }

    @Override
    public void onPanelDragged() {
        updatePanelState(PanelState.DRAGGING);
        overlayController.setFocusable(true);
    }

    @Override
    public void startPanelDrag() {
        updatePanelState(PanelState.DRAGGING);
        overlayController.setFocusable(true);
    }

    @Override
    public void setPanelEnabled(boolean enabled) {
        if (enabled) {
            overlayController.Hn();
        }
        updatePanelState(PanelState.DRAGGING);
        overlayController.setVisible(false);
    }

    @Override
    public void openPanel() {
        Log.d("OverlayController", "Opening panel in drawer mode");
        updatePanelState(PanelState.OPEN_AS_DRAWER);
    }

    @Override
    public void setPanelPosition(float position) {
        if (overlayController.overlayCallback != null && !Float.isNaN(position)) {
            try {
                overlayController.overlayCallback.overlayScrollChanged(position);
                overlayController.onScroll(position);
                overlayController.setWindowAlpha(position);
            } catch (Throwable ignored) {
                // Optionally log the exception if needed
            }
        }
    }

    @Override
    public void closePanel() {
        overlayController.setVisible(false);
        updatePanelState(PanelState.CLOSED);
    }

    @Override
    public boolean canInterceptTouchEvents() {
        return overlayController.shouldHandleInput();
    }

    private void updatePanelState(PanelState newState) {
        if (overlayController.panelState != newState) {
            overlayController.panelState = newState;
            overlayController.setState(newState);
        }
    }
}
