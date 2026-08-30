package com.google.android.libraries.gsa.d.a;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;

final class SlidingPanelLayoutInterpolator extends AnimatorListenerAdapter implements Interpolator {

    private ObjectAnimator animator;
    public int finalX;
    private final SlidingPanelLayout layout;

    public SlidingPanelLayoutInterpolator(SlidingPanelLayout layout) {
        this.layout = layout;
    }

    public void cancelAnimation() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
    }

    public void animateTo(int targetX, int durationMs) {
        cancelAnimation();
        this.finalX = targetX;

        if (durationMs > 0) {
            animator = ObjectAnimator.ofInt(layout, SlidingPanelLayout.PANEL_X, targetX)
                    .setDuration(durationMs);
            animator.setInterpolator(this);
            animator.addListener(this);
            animator.start();
        } else {
            // Animation skipped, invoke end manually
            onAnimationEnd(null);
        }
    }

    public boolean isFinished() {
        return animator == null;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        animator = null;
        layout.updatePanelOffset(finalX);

        if (!layout.settling) return;

        layout.settling = false;

        if (layout.panelOffsetPx == 0) {
            layout.updateLayer();
            layout.isPanelOpen = false;
            layout.isPageMoving = false;
            if (layout.panelController != null) {
                layout.panelController.closePanel();
            }
        } else if (layout.panelOffsetPx == layout.getMeasuredWidth()) {
            layout.onPanelFullyOpened();
        }
    }

    @Override
    public float getInterpolation(float input) {
        float t = input - 1.0f;
        return (t * t * t * t * t) + 1.0f;
    }
}
