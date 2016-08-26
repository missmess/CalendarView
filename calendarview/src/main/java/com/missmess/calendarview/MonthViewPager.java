package com.missmess.calendarview;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * MonthViewPager should contains one MonthView child to config styles.
 * <p></p>
 *
 * @author wl
 * @since 2016/08/26 15:41
 */
public class MonthViewPager extends ViewGroup {
    private static final int VEL_THRESHOLD = 6000;
    private ViewDragHelper dragger;
    private MonthView childLeft;
    private MonthView childMiddle;
    private MonthView childRight;
    private int mWidth;
    private GestureDetectorCompat mGestureDetector;

    public MonthViewPager(Context context) {
        this(context, null);
    }

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        dragger = ViewDragHelper.create(this, 1f, new DragCallBack());
        mGestureDetector = new GestureDetectorCompat(getContext(), new XScrollDetector());
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        // call by system
        if(getChildCount() > 0 || !(child instanceof MonthView)) {
            throw new IllegalStateException("MonthViewPager can host only one MonthView child");
        }
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        childMiddle = (MonthView) child;
        childLeft = childMiddle.createCopy();
        childRight = childMiddle.createCopy();
        super.addView(childMiddle, params);
        super.addView(childLeft, params);
        super.addView(childRight, params);
        childLeft.setEnabled(false);
        childMiddle.setEnabled(false);
        childRight.setEnabled(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY);
        measureChild(childLeft, childWidthSpec, heightMeasureSpec);
        measureChild(childMiddle, childWidthSpec, heightMeasureSpec);
        measureChild(childRight, childWidthSpec, heightMeasureSpec);

        setMeasuredDimension(mWidth, childMiddle.getMaxHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = getMeasuredWidth();
        childMiddle.layout(0, 0, width, childMiddle.getMeasuredHeight());
        childLeft.layout(-width, 0, 0, childLeft.getMeasuredHeight());
        childRight.layout(width, 0, 2 * width, childRight.getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }

    @Override
    public void computeScroll() {
        if(dragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean yScroll = mGestureDetector.onTouchEvent(ev);
        boolean shouldIntercept = dragger.shouldInterceptTouchEvent(ev);
        return shouldIntercept && yScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragger.processTouchEvent(event);
        return true;
    }

    private class DragCallBack extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == childMiddle;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int DISTANCE_THRESHOLD = mWidth / 2;
            int finalLeft = 0;
            if (xvel < -VEL_THRESHOLD || releasedChild.getLeft() < -DISTANCE_THRESHOLD) {
                finalLeft = -mWidth;
            }
            if (xvel > VEL_THRESHOLD || releasedChild.getLeft() > DISTANCE_THRESHOLD) {
                finalLeft = mWidth;
            }

            if (dragger.settleCapturedViewAt(finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(changedView == childMiddle) {
                childLeft.setLeft(left - mWidth);
                childRight.setLeft(left + mWidth);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_SETTLING:
                    if(childMiddle.getLeft() == -mWidth) {
                        childMiddle.setYearAndMonth(childRight.getCurrentYear(), childRight.getCurrentMonth());
                        childMiddle.setLeft(0);
                    }
                    break;
            }
        }
    }

    private class XScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return Math.abs(dx) > Math.abs(dy);
        }
    }
}
