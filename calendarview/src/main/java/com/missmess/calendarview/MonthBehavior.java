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
public class MonthBehavior extends CoordinatorLayout.Behavior<MonthViewPager> {

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
        if (!child.isMonthMode())
            return;

        int currentY = (int) ViewCompat.getTranslationY(child);
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
        ViewCompat.setTranslationY(child, currentY - consumed[1]);
        Log.d("month_behavior2", "onNestedPreScroll: dy==" + dy + ";destY==" + (currentY - consumed[1]) + ";top==" + target.getTop() + ";transY==" + target.getTranslationY());
    }
}
