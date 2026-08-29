package com.google.android.libraries.gsa.d.a;

final class TransparentOverlayController implements PanelController {

    private final OverlayController overlayController;

    TransparentOverlayController(OverlayController overlayControllerVar) {
        this.overlayController = overlayControllerVar;
    }

    @Override
    public void onPanelDragged() {

    }

    @Override
    public void startPanelDrag() {
    }

    @Override
    public void setPanelEnabled(boolean enabled) {
    }

    @Override
    public void openPanel() {
        this.overlayController.setFocusable(true);
        this.overlayController.setWindowAlpha(1.0f);
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.OPEN_AS_LAYER;//Todo: PanelState.uoh was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
    }

    @Override
    public void closePanel() {
        this.overlayController.setVisible(false);
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.CLOSED;//Todo: PanelState.uoe was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
        this.overlayController.slidingPanelLayout.panelController = this.overlayController.panelController;
    }

    @Override
    public void setPanelPosition(float position) {
    }

    @Override
    public boolean canInterceptTouchEvents() {
        return true;
    }
}
