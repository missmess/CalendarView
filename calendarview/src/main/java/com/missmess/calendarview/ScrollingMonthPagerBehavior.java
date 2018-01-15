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
public class ScrollingMonthPagerBehavior extends ViewOffsetBehavior<View> {
    private MonthViewPager monthViewPager;
    private Scroller scroller;
    private Scroller scrollerOfM;
    private Handler handler = new Handler();
    private boolean isCollapsed = false;
    private OnStateChangeListener li;
    private MonthBehavior monthBehavior;
    private boolean loaded;

    public ScrollingMonthPagerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        scrollerOfM = new Scroller(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency instanceof MonthViewPager) {
            monthViewPager = (MonthViewPager) dependency;
            CoordinatorLayout.LayoutParams lps = (CoordinatorLayout.LayoutParams) monthViewPager.getLayoutParams();
            monthBehavior = (MonthBehavior) lps.getBehavior();
            return true;
        }
        return false;
    }

//    @Override
//    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
//        setTopAndBottomOffset(child.getBottom());
//        Log.i("month_behavior", "onDependentViewChanged: top==" + child.getBottom() + ";transY==" + child.getTranslationY());
//        return false;
//    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        super.onLayoutChild(parent, child, layoutDirection);
        // always locate target view below to the monthViewPager.
        int bottom = monthViewPager.getBottom();
        if (loaded) {
            setTopAndBottomOffset(bottom);
            loaded = true;
        }
//        child.layout(0, bottom, child.getMeasuredWidth(), bottom + child.getMeasuredHeight());
        Log.i("month_behavior", "onLayoutChild: top==" + bottom + ";transY==" + child.getTranslationY());
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, final View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("month_behavior", "onStartNestedScroll");
        scroller.abortAnimation();
        scrollerOfM.abortAnimation();
        if (!monthViewPager.isDraggerIdle()) {
            // if dragger is not idle, not handle nested scroll event
            Log.w("month_behavior", "not handle nested scroll");
            return false;
        }

        if (!monthViewPager.isMonthMode()) {
            // when start scroll, set to month mode if previous mode is week-mode.
            final int translate = -monthViewPager.getMaximumScrollRange();
            monthViewPager.setMonthMode();
            monthViewPager.post(new Runnable() {
                @Override
                public void run() {
                    // change to month mode, must set a proper translationY.
                    monthBehavior.setTopAndBottomOffset(translate);
                }
            });
        }

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if (dy == 0)
            return;

        int currentY = getTopAndBottomOffset();
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

       setTopAndBottomOffset(currentY - consumed[1]);
        Log.i("month_behavior", "onNestedPreScroll: dy==" + dy + ";destY==" + destY + ";top==" + child.getTop() + ";transY==" + child.getTranslationY());
    }

//    @Override
//    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        if (dyUnconsumed == 0)
//            return;
//
//        int currentY = (int) ViewCompat.getTranslationY(child);
//        int destY = currentY - dyUnconsumed;
//        Log.d("month_behavior", "onNestedScroll: currentY==" + currentY);
//
//        boolean upward = dyUnconsumed > 0;
//        int maxY = 0;
//        if (!upward) {
//            // ScrollingView not consume downward scrolling
//            if (destY > maxY) {
//                ViewCompat.setTranslationY(child, maxY);
//            } else {
//                ViewCompat.setTranslationY(child, destY);
//            }
//        }
//    }

    private int getMinTransY() {
        return monthViewPager.getShouldHeightInWeekMode();
    }

    /**
     * The maximum translationY target child can translate top.
     *
     * @return maximum translationY
     */
    private int getMaxTransY() {
        return monthViewPager.getShouldHeightInMonthMode();
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        int minimumY = getMinTransY();
        int maximumY = getMaxTransY();
        int currentY = getTopAndBottomOffset();

        int minimumYOfM = -monthViewPager.getMaximumScrollRange();
        int currentYOfM = monthBehavior.getTopAndBottomOffset();

        if ((!isCollapsed && currentY < (2 * maximumY / 3))
                || (isCollapsed && currentY < maximumY / 3)) {
            // about to collapse
            int dy = minimumY - currentY;
            int duration = calculateDuration(Math.abs(dy), Math.abs(minimumY));
            scroller.startScroll(0, currentY, 0, dy, duration);
            scrollerOfM.startScroll(0, currentYOfM, 0, minimumYOfM - currentYOfM, duration);
        } else {
            // about to expand
            int dy = maximumY - currentY;
            int duration = calculateDuration(Math.abs(dy), Math.abs(minimumY));
            scroller.startScroll(0, currentY, 0, dy, duration);
            scrollerOfM.startScroll(0, currentYOfM, 0, -currentYOfM, duration);
        }

        handler.post(new Running());
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

        @Override
        public void run() {
            boolean process;
            boolean processOfM;
            if (process = scroller.computeScrollOffset()) {
                setTopAndBottomOffset(scroller.getCurrY());
            }
            if (processOfM = scrollerOfM.computeScrollOffset()) {
                monthBehavior.setTopAndBottomOffset(scrollerOfM.getCurrY());
            }

            if (process || processOfM) {
                handler.post(this);
            } else {
                boolean oldCollapseState = isCollapsed;
                boolean newCollapseState = getTopAndBottomOffset() == getMinTransY();

                // always change to week mode when collapsed
                if (newCollapseState) {
                    monthViewPager.setWeekMode();
                    // clear offset of MonthViewPager
                    monthBehavior.setTopAndBottomOffset(0);
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
