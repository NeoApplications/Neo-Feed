package com.saulhdev.feeder.ui.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.saulhdev.feeder.ui.anim.Interpolators.scrollInterpolatorForVelocity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.saulhdev.feeder.ui.anim.Interpolators;
import com.saulhdev.feeder.ui.touch.BaseSwipeDetector;
import com.saulhdev.feeder.ui.touch.SingleAxisSwipeDetector;
import com.saulhdev.feeder.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSlideInView extends AbstractFloatingView implements SingleAxisSwipeDetector.Listener {
    protected static final Property<AbstractSlideInView, Float> TRANSLATION_SHIFT =
            new Property<>(Float.class, "translationShift") {

                @Override
                public Float get(AbstractSlideInView view) {
                    return view.mTranslationShift;
                }

                @Override
                public void set(AbstractSlideInView view, Float value) {
                    view.setTranslationShift(value);
                }
            };

    protected static final float TRANSLATION_SHIFT_CLOSED = 1f;
    protected static final float TRANSLATION_SHIFT_OPENED = 0f;
    protected static final int DEFAULT_DURATION = 300;
    protected final SingleAxisSwipeDetector mSwipeDetector;
    protected final ObjectAnimator mOpenCloseAnimator;
    protected float mTranslationShift = TRANSLATION_SHIFT_CLOSED;
    protected View mContent;
    protected final View mColorScrim;
    protected Interpolator mScrollInterpolator;
    private long mScrollDuration;
    protected boolean mNoIntercept;
    protected List<OnCloseListener> mOnCloseListeners = new ArrayList<>();
    private Rect mHitRect =new Rect();
    private float[] mTmpXY = new float[2];

    public AbstractSlideInView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollInterpolator = Interpolators.SCROLL_CUBIC;
        mScrollDuration = DEFAULT_DURATION;
        mSwipeDetector = new SingleAxisSwipeDetector(context, this, SingleAxisSwipeDetector.VERTICAL);

        mOpenCloseAnimator = ObjectAnimator.ofPropertyValuesHolder(this);
        mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSwipeDetector.finishedScrolling();
            }
        });
        int scrimColor = getScrimColor(context);
        mColorScrim = scrimColor != -1 ? createColorScrim(context, scrimColor) : null;
    }

    protected void attachToContainer() {
        if (mColorScrim != null && mColorScrim.getParent() == null) {
            container.addView(mColorScrim);
        }

        if (this.getParent() != null) {
            android.view.ViewGroup.LayoutParams params = this.getLayoutParams();
            if (params instanceof CoordinatorLayout.LayoutParams lp) {
                lp.gravity = android.view.Gravity.BOTTOM;
                this.setLayoutParams(lp);
            } else if (params instanceof FrameLayout.LayoutParams lp) {
                lp.gravity = android.view.Gravity.BOTTOM;
                this.setLayoutParams(lp);
            }
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            lp.gravity = android.view.Gravity.BOTTOM;
            this.setLayoutParams(lp);
            container.addView(this);
        }
    }

    protected int getScrimColor(Context context) {
        return -1;
    }

    protected void setTranslationShift(float translationShift) {
        mTranslationShift = translationShift;
        mContent.setTranslationY(mTranslationShift * mContent.getHeight());
        if (mColorScrim != null) {
            mColorScrim.setAlpha(1 - mTranslationShift);
        }
    }

    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        return mSwipeDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (mNoIntercept) {
            return false;
        }

        int directionsToDetectScroll = mSwipeDetector.isIdleState()
                ? SingleAxisSwipeDetector.DIRECTION_NEGATIVE : 0;
        mSwipeDetector.setDetectableScrollConditions(
                directionsToDetectScroll, false);
        mSwipeDetector.onTouchEvent(ev);
        return mSwipeDetector.isDraggingOrSettling()
                || !isEventOverView(mContent, ev);
    }


    private boolean isEventOverView(View view, MotionEvent ev) {
        getDescendantRectRelativeToSelf(view, mHitRect);
        return mHitRect.contains((int)ev.getX(), (int)ev.getY());
    }
    public float getDescendantRectRelativeToSelf(View descendant, Rect r) {
        mTmpXY[0] = 0f;
        mTmpXY[1] = 0f;
        float scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY);

        r.set((int) mTmpXY[0], (int) mTmpXY[1],
                (int) (mTmpXY[0] + scale * descendant.getMeasuredWidth()),
                (int) (mTmpXY[1] + scale * descendant.getMeasuredHeight()));
        return scale;
    }

    private float getDescendantCoordRelativeToSelf(View descendant, float[] coord) {
        return getDescendantCoordRelativeToSelf(descendant, coord, false);
    }

    public float getDescendantCoordRelativeToSelf(View descendant, float[] coord,
                                                  boolean includeRootScroll) {
        return Utilities.getDescendantCoordRelativeToAncestor(descendant, this,
                coord, includeRootScroll);
    }

    @Override
    public void onDragStart(boolean start, float startDisplacement) {
    }

    @Override
    public boolean onDrag(float displacement) {
        float range = mContent.getHeight();
        displacement = Utilities.boundToRange(displacement, 0, range);
        setTranslationShift(displacement / range);
        return true;
    }


    @Override
    public void onDragEnd(float velocity) {
        if ((mSwipeDetector.isFling(velocity) && velocity > 0) || mTranslationShift > 0.5f) {
            mScrollInterpolator = scrollInterpolatorForVelocity(velocity);
            mOpenCloseAnimator.setDuration(BaseSwipeDetector.calculateDuration(
                    velocity, TRANSLATION_SHIFT_CLOSED - mTranslationShift));
            mScrollDuration = BaseSwipeDetector.calculateDuration(
                    velocity, TRANSLATION_SHIFT_CLOSED - mTranslationShift);
            close(true);
        } else {
            mOpenCloseAnimator.setValues(PropertyValuesHolder.ofFloat(
                    TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED));
            mOpenCloseAnimator.setDuration(
                            BaseSwipeDetector.calculateDuration(velocity, mTranslationShift))
                    .setInterpolator(Interpolators.DEACCEL);
            mOpenCloseAnimator.start();
        }

    }

    public void close(boolean animate) {
        handleClose(animate, mScrollDuration);
    }

    protected void handleClose(boolean animate, long defaultDuration) {
        if (!mIsOpen) {
            return;
        }
        if (!animate) {
            mOpenCloseAnimator.cancel();
            setTranslationShift(TRANSLATION_SHIFT_CLOSED);
            onCloseComplete();
            return;
        }
        mOpenCloseAnimator.setValues(
                PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_CLOSED));
        mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseComplete();
            }
        });

        mOpenCloseAnimator.setInterpolator(mScrollInterpolator);
        mOpenCloseAnimator.setDuration(mScrollDuration);
        mOpenCloseAnimator.start();
    }

    protected void onCloseComplete() {
        mIsOpen = false;
        container.removeView(this);
        if (mColorScrim != null) {
            container.removeView(mColorScrim);
        }
        mOnCloseListeners.forEach(OnCloseListener::onSlideInViewClosed);
    }

    protected View createColorScrim(Context context, int bgColor) {
        View view = new View(context);
        view.forceHasOverlappingRendering(false);
        view.setBackgroundColor(bgColor);
        view.setClickable(true);
        view.setFocusable(true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(lp);

        return view;
    }

    /**
     * Interface to report that the {@link AbstractSlideInView} has closed.
     */
    public interface OnCloseListener {

        /**
         * Called when {@link AbstractSlideInView} closes.
         */
        void onSlideInViewClosed();
    }
}
