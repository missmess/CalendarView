package com.missmess.calendarview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Used in {@link MonthViewPager}, to support nested scrolling.
 *
 * @author wl
 * @since 2017/08/03 15:14
 */
public class MonthBehavior extends ViewOffsetBehavior<MonthViewPager> {

    public MonthBehavior() {
    }

    public MonthBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, MonthViewPager child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, MonthViewPager child, View target, int dx, int dy, int[] consumed) {
        if (dy == 0)
            return;

        int currentY = getTopAndBottomOffset();
        int destY = currentY - dy;

        boolean upward = dy > 0;
        int minY = -child.getMaximumScrollRange();
        int maxY = 0;
        if (upward) { // we just take interest on upward scrolling
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
        Log.d("month_behavior2", "onNestedPreScroll: dy==" + dy + ";destY==" + destY + ";top==" + target.getTop() + ";transY==" + target.getTranslationY());
    }

//    @Override
//    public void onNestedScroll(CoordinatorLayout coordinatorLayout, MonthViewPager child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        if (dyUnconsumed == 0)
//            return;
//
//        int currentY = (int) ViewCompat.getTranslationY(child);
//        int destY = currentY - dyUnconsumed;
//        Log.v("month_behavior2", "onNestedScroll: currentY==" + currentY);
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
}
