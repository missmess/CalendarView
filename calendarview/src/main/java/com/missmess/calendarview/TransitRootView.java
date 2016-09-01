package com.missmess.calendarview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A TransitRootView should contains two direct children, the first child contains an YearView, and
 * the second contains a MonthView.
 * This view can help display Y and M transition animation and manage the animations of your other views.
 *
 * @author wl
 * @since 2016/08/25 11:33
 */
public class TransitRootView extends FrameLayout {
    View child1;
    View child2;
    boolean mReceiveEvent = true;
    private MonthView transitView;

    public TransitRootView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        transitView = new MonthView(getContext());
        transitView.showMonthTitle(false);
        transitView.showWeekLabel(false);
        transitView.setVisibility(View.GONE);
        super.addView(transitView);
    }

    @Override
    public void addView(View child) {
        addView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        cookChild(child);
        super.addView(child, params);
    }

    private void cookChild(View child) {
        if(getChildCount() > 3) {
            throw new IllegalStateException("TransitRootView can host only two direct children in xml");
        }
        if(child1 == null) {
            child1 = child;
        } else {
            child2 = child;
            changeChildrenVisibility();
        }
    }

    // just one child visible at once, another set to gone.
    private void changeChildrenVisibility() {
        if(child1.getVisibility() == View.VISIBLE) {
            child2.setVisibility(View.GONE);
            return;
        }
        if(child2.getVisibility() == View.VISIBLE) {
            child1.setVisibility(View.GONE);
            return;
        }
        // no child is visible
        child1.setVisibility(View.VISIBLE);
        child2.setVisibility(View.GONE);
    }

    /**
     * if view should receive touch event.
     * @param receive false - receive nothing.
     */
    public void setReceiveEvent(boolean receive) {
        mReceiveEvent = receive;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !mReceiveEvent || super.dispatchTouchEvent(ev);
    }

    /**
     * use a TransitView to show transition animation
     * @return a useable MonthView
     */
    public MonthView useTransitView() {
        transitView.setVisibility(View.VISIBLE);
        return transitView;
    }

    /**
     * should call this to recycle the TransitView
     */
    public void recycleTransitView() {
        transitView.setVisibility(View.GONE);
    }
}
