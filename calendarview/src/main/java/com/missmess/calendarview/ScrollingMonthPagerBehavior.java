package com.missmess.calendarview;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Used in view which below {@link MonthViewPager} to behave this view overlap on the {@link MonthViewPager},
 * and when MonthViewPager collapsed, it changed to week mode.
 *
 * @author wl
 * @since 2017/08/04 13:47
 */
public class ScrollingMonthPagerBehavior extends CoordinatorLayout.Behavior<View> {
    private MonthViewPager monthViewPager;
    private Scroller scroller;
    private Scroller scrollerOfM;
    private Handler handler = new Handler();
    private boolean isCollapsed = false;
    private OnStateChangeListener li;
    private boolean isLayouted = false;

    public ScrollingMonthPagerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        scrollerOfM = new Scroller(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency instanceof MonthViewPager) {
            monthViewPager = (MonthViewPager) dependency;
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if (!isLayouted) {
            // always locate target view below to the monthViewPager.
            int bottom = monthViewPager.getBottom();
            int minHeight = monthViewPager.getShouldHeightInWeekMode();
            child.layout(0, bottom, child.getMeasuredWidth(), bottom + child.getMeasuredHeight() - minHeight);
            isLayouted = true;
        }
        Log.i("month_behavior", "onLayoutChild: top==" + monthViewPager.getBottom() + ";transY==" + child.getTranslationY());
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, final View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("month_behavior", "onStartNestedScroll");
        scroller.abortAnimation();
        scrollerOfM.abortAnimation();
        handler.removeCallbacksAndMessages(null);

        if (!monthViewPager.isDraggerIdle())
            // if dragger is not idle, not handle nested scroll event
            return false;

        if (!monthViewPager.isMonthMode()) {
            // when start scroll, set to month mode if previous mode is week-mode.
            final int translate = -monthViewPager.getMaximumScrollRange();
            monthViewPager.setMonthMode();
            // change to month mode, must set a proper translationY.
            ViewCompat.setTranslationY(monthViewPager, translate);
        }

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if (dy == 0)
            return;

        int currentY = (int) ViewCompat.getTranslationY(child);
        int destY = currentY - dy;

        boolean upward = dy > 0;
        int minY = getMinTransY();
        int maxY = getMaxTransY();
        if (upward) {
            if (destY < minY) {
                consumed[1] = currentY - minY;
            } else {
                consumed[1] = dy;
            }
        } else {
            if (destY <= maxY) {
                consumed[1] = dy;
            } else {
                consumed[1] = currentY - maxY;
            }
        }

        ViewCompat.setTranslationY(child, currentY - consumed[1]);
        Log.i("month_behavior", "onNestedPreScroll: dy==" + dy + ";destY==" + (currentY - consumed[1]) + ";top==" + child.getTop() + ";transY==" + child.getTranslationY());
    }

    private int getMaxTransY() {
        return 0;
    }

    /**
     * The maximum translationY target child can translate top.
     *
     * @return maximum translationY
     */
    private int getMinTransY() {
        return -(monthViewPager.getShouldHeightInMonthMode() - monthViewPager.getShouldHeightInWeekMode());
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        Log.d("month_behavior", "onStopNestedScroll");
        int minimumY = getMinTransY();
        float translationY = ViewCompat.getTranslationY(child);

        int minimumYOfM = -monthViewPager.getMaximumScrollRange();
        float translationYOfM = ViewCompat.getTranslationY(monthViewPager);

        if ((!isCollapsed && translationY < (minimumY / 3))
                || (isCollapsed && translationY < (2 * minimumY / 3))) {
            int dy = (int) (minimumY - translationY);
            int duration = calculateDuration(Math.abs(dy), Math.abs(minimumY));
            scroller.startScroll(0, (int) translationY, 0, dy, duration);
            scrollerOfM.startScroll(0, (int) translationYOfM, 0, (int) (minimumYOfM - translationYOfM), duration);
        } else {
            int dy = (int) (-translationY);
            int duration = calculateDuration(Math.abs(dy), Math.abs(minimumY));
            scroller.startScroll(0, (int) translationY, 0, dy, duration);
            scrollerOfM.startScroll(0, (int) translationYOfM, 0, (int) (-translationYOfM), duration);
        }

        handler.post(new Running(child));
    }

    private int calculateDuration(int distance, int max) {
        int maxDuration = 600;
        float scale = (float) distance / max;
        return (int) (maxDuration * scale);
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.li = listener;
    }

    private class Running implements Runnable {
        private View target;

        Running(View target) {
            this.target = target;
        }

        @Override
        public void run() {
            boolean process;
            boolean processOfM;
            if (process = scroller.computeScrollOffset()) {
                ViewCompat.setTranslationY(target, scroller.getCurrY());
            }
            if (processOfM = scrollerOfM.computeScrollOffset()) {
                ViewCompat.setTranslationY(monthViewPager, scrollerOfM.getCurrY());
            }

            if (process || processOfM) {
                handler.post(this);
            } else {
                boolean oldCollapseState = isCollapsed;
                boolean newCollapseState = ViewCompat.getTranslationY(target) == getMinTransY();

                // always change to week mode when collapsed
                if (newCollapseState) {
                    monthViewPager.setWeekMode();
                    ViewCompat.setTranslationY(monthViewPager, 0);
                }

                if (newCollapseState == oldCollapseState) {
                    // state not changed.
                    return;
                }

                if (newCollapseState) {
                    if (li != null)
                        li.onCollapsed();
                    isCollapsed = true;
                } else {
                    if (li != null)
                        li.onExpanded();
                    isCollapsed = false;
                }
            }
        }
    }

    public interface OnStateChangeListener {
        void onExpanded();

        void onCollapsed();
    }

    public static <V extends View> ScrollingMonthPagerBehavior from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof ScrollingMonthPagerBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomSheetBehavior");
        }
        return (ScrollingMonthPagerBehavior) behavior;
    }
}
