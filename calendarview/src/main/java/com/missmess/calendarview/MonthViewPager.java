package com.missmess.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * MonthViewPager should contains one MonthView child to config their styles, the MonthView child will only
 * obtain attributes, so do not find this child by findViewById, use MonthViewPager
 * to do something (e.g.: add listener) instead.
 *
 * <p>MonthViewPager is similar to a ViewPager, it is easy to be used to display a sort of MonthView with same styles
 * and a sequential month.</p>
 *
 * @author wl
 * @since 2016/08/26 15:41
 */
public class MonthViewPager extends ViewGroup {
    private static final int VEL_THRESHOLD = 3000;
    private ViewDragHelper dragger;
    private MonthView childLeft;
    private MonthView childMiddle;
    private MonthView childRight;
    private int mWidth;
    private Drawable ic_previous;
    private Drawable ic_next;
    private ImageView indicator_left;
    private ImageView indicator_right;
    private int indicate_margin;
    private BtnClicker clicker;
    private CalendarMonth leftEdge;
    private CalendarMonth rightEdge;
    private CalendarMonth currentMonth;
    private boolean leftAble = true;
    private boolean rightAble = true;
    private OnDragListener mDragListener;
    private OnMonthChangeListener mChangeListener;
    private boolean mShowIndicator;
    private DayDecor mDecors;
    private int month_marginTop;

    public MonthViewPager(Context context) {
        this(context, null);
    }

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MonthViewPager);
        mShowIndicator = typedArray.getBoolean(R.styleable.MonthViewPager_show_indicator, true);
        ic_previous = typedArray.getDrawable(R.styleable.MonthViewPager_ic_previous_month);
        ic_next = typedArray.getDrawable(R.styleable.MonthViewPager_ic_next_month);
        month_marginTop = typedArray.getDimensionPixelSize(R.styleable.MonthViewPager_month_marginTop, 0);
        if(ic_previous == null) {
            ic_previous = context.getResources().getDrawable(R.mipmap.ic_previous);
        }
        if(ic_next == null) {
            ic_next = context.getResources().getDrawable(R.mipmap.ic_next);
        }

        indicate_margin = context.getResources().getDimensionPixelSize(R.dimen.icon_margin);
        typedArray.recycle();

        init();
    }

    private void init() {
        dragger = ViewDragHelper.create(this, 1f, new DragCallBack());
        clicker = new BtnClicker();
        leftEdge = new CalendarMonth(1900, 2);
        rightEdge = new CalendarMonth(2049, 12);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        // called by system
        if(getChildCount() > 0 || !(child instanceof MonthView)) {
            throw new IllegalStateException("MonthViewPager can host only one MonthView child");
        }
        childMiddle = (MonthView) child;
        childLeft = childMiddle.staticCopy();
        childLeft.setYearAndMonth(childMiddle.getCurrentMonth().previous());
        childRight = childMiddle.staticCopy();
        childRight.setYearAndMonth(childMiddle.getCurrentMonth().next());

        // add three child
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        super.addView(childLeft, params);
        super.addView(childMiddle, params);
        super.addView(childRight, params);

        // add indicators
        if(mShowIndicator) {
            indicator_left = createIndicator(ic_previous);
            super.addView(indicator_left);
            indicator_right = createIndicator(ic_next);
            super.addView(indicator_right);
        }
    }

    private ImageView createIndicator(Drawable icon) {
        ImageView imageBtn = new ImageView(getContext());
        imageBtn.setScaleType(ImageView.ScaleType.CENTER);
        imageBtn.setImageDrawable(icon);
        imageBtn.setBackgroundResource(getThemeSelectableBackgroundId(getContext()));
        imageBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        imageBtn.setOnClickListener(clicker);
        return imageBtn;
    }

    private int getThemeSelectableBackgroundId(Context context) {
        //Get selectableItemBackgroundBorderless defined for AppCompat
        int colorAttr = context.getResources().getIdentifier(
                "selectableItemBackgroundBorderless", "attr", context.getPackageName());

        if (colorAttr == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorAttr = android.R.attr.selectableItemBackgroundBorderless;
            } else {
                colorAttr = android.R.attr.selectableItemBackground;
            }
        }

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.resourceId;
    }

    /**
     * set MonthViewPager to show this month
     * @param calendarMonth month
     */
    public void setCurrentMonth(CalendarMonth calendarMonth) {
        childMiddle.setYearAndMonth(calendarMonth);
        if(!childMiddle.getCurrentMonth().equals(currentMonth))
            monthChanged(childMiddle);
    }

    public void setToday(CalendarDay today) {
        childLeft.setToday(today);
        childMiddle.setToday(today);
        childRight.setToday(today);
    }

    public MonthView getCurrentChild() {
        return childMiddle;
    }

    /**
     * set range of month showing in MonthViewPager.
     * @param start start month
     * @param end end month
     */
    public void setMonthRange(CalendarMonth start, CalendarMonth end) {
        if(start.compareTo(end) > 0) {
            throw new IllegalArgumentException("start month cannot larger than end month");
        }
        leftEdge = start;
        rightEdge = end;
        checkEdge();
    }


    /**
     * <p>it'll be called at these situation:</p>
     * <li>current middle view changed to another view</li>
     * <li>current middle view changed its showing month</li>
     *
     * @param oldMiddle oldMiddle
     */
    private void monthChanged(MonthView oldMiddle) {
        CalendarMonth old = currentMonth;
        currentMonth = childMiddle.getCurrentMonth();
        if(currentMonth.equals(old))
            return;

        // lookup range
        checkEdge();

        // setup left right view
        MonthView left = null;
        MonthView right = null;
        if (leftAble) {
            childLeft.setYearAndMonth(currentMonth.previous());
            left = childLeft;
        }
        if (rightAble) {
            childRight.setYearAndMonth(currentMonth.next());
            right = childRight;
        }
        // call listener
        if(mChangeListener != null) {
            mChangeListener.onMonthChanged(this, left, childMiddle, right, currentMonth, old);
        }
        requestLayout();
    }

    private void checkEdge() {
        CalendarMonth cm = childMiddle.getCurrentMonth();
        // when edge is not containing current, correct current month.
        if(cm.compareTo(leftEdge) < 0) {
            setCurrentMonth(leftEdge);
        }
        if(cm.compareTo(rightEdge) > 0) {
            setCurrentMonth(rightEdge);
        }

        if(cm.equals(leftEdge)) {
            if(indicator_left != null)
                indicator_left.setVisibility(View.GONE);
            leftAble = false;
        } else {
            if(indicator_left != null)
                indicator_left.setVisibility(View.VISIBLE);
            leftAble = true;
        }
        if(cm.equals(rightEdge)) {
            if(indicator_right != null)
                indicator_right.setVisibility(View.GONE);
            rightAble = false;
        } else {
            if(indicator_right != null)
                indicator_right.setVisibility(View.VISIBLE);
            rightAble = true;
        }
    }

    public void setDecors(DayDecor decors) {
        this.mDecors = decors;
        if(childMiddle != null) {
            childLeft.setDecors(decors);
            childMiddle.setDecors(decors);
            childRight.setDecors(decors);
        }
    }

    public DayDecor getDecors() {
        return mDecors;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        if(childLeft == null || childMiddle == null || childRight == null) {
            throw new IllegalStateException("MonthViewPager should host a MonthView child");
        }
        int height = childMiddle.getMaxHeight() + month_marginTop;

        //measure MonthView children
        int childWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.AT_MOST);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        childLeft.measure(childWidthSpec, childHeightSpec);
        childMiddle.measure(childWidthSpec, childHeightSpec);
        childRight.measure(childWidthSpec, childHeightSpec);

        // measure indicator ImageView
        if(mShowIndicator) {
            int indWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.AT_MOST);
            int indHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
            indicator_left.measure(indWidthSpec, indHeightSpec);
            indicator_right.measure(indWidthSpec, indHeightSpec);
        }

        setMeasuredDimension(mWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingTop = month_marginTop;
        childMiddle.layout(0, paddingTop, mWidth, childMiddle.getShouldHeight() + paddingTop);
        childLeft.layout(-mWidth, paddingTop, 0, childLeft.getShouldHeight() + paddingTop);
        childRight.layout(mWidth, paddingTop, 2 * mWidth, childRight.getShouldHeight() + paddingTop);

        if(mShowIndicator) {
            int month_header_height = childMiddle.MONTH_HEADER_HEIGHT;
            int left_height = indicator_left.getMeasuredHeight();
            int right_height = indicator_right.getMeasuredHeight();
            int top1 = (month_header_height + paddingTop - left_height) / 2;
            int top2 = (month_header_height + paddingTop - right_height) / 2;
            if(top1 < 0)
                top1 = 0;
            if(top2 < 0)
                top2 = 0;
            indicator_left.layout(indicate_margin, top1, indicator_left.getMeasuredWidth() + indicate_margin, top1 + left_height);
            indicator_right.layout(mWidth - indicate_margin - indicator_right.getMeasuredWidth(), top2, mWidth - indicate_margin, top2 + right_height);
        }
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
        return dragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragger.processTouchEvent(event);
        return true;
    }

    private float calcuIndicatorAlphaAtDistance(float d, float w, float i, float m) {
        float min = 0.2f;
        float a;
        float point1 = w / 2f - m / 2f - i;
        float point2 = w / 2f + m / 2f + i;
        if (d < point1) {
            a = 1f - d * (1f - min) / point1;
        } else if (d <= point2) {
            a = min;
        } else {
            a = min + (d - point2) * (1f - min) / point1;
        }
        return a;
    }

    private class DragCallBack extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == childMiddle;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return month_marginTop;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(left > 0 && !leftAble) {
                // can't move left
                return 0;
            }
            if(left < 0 && !rightAble) {
                // can't move right
                return 0;
            }
            return left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int DISTANCE_THRESHOLD = mWidth / 3;
            int finalLeft = 0;
            if (xvel < -VEL_THRESHOLD || releasedChild.getLeft() < -DISTANCE_THRESHOLD) {
                finalLeft = -mWidth;
                onScrollToRight();
            }
            if (xvel > VEL_THRESHOLD || releasedChild.getLeft() > DISTANCE_THRESHOLD) {
                finalLeft = mWidth;
                onScrollToLeft();
            }
//            Log.e("dragHelper", String.format("xvel=%f;left=%d;finalLeft=%d", xvel, releasedChild.getLeft(), finalLeft));

            if (dragger.settleCapturedViewAt(finalLeft, month_marginTop)) {
                ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(changedView == childMiddle) { // childMiddle is scrolling
                // offset left and right children
                childLeft.offsetLeftAndRight(dx);
                childRight.offsetLeftAndRight(dx);
                // if close to edge, alpha to 0;
                // alpha indicators
                if(mShowIndicator) {
                    int monthTitleWidth = childMiddle.getMonthTitleWidth();
                    int ind_width = indicator_left.getRight();
                    int distance = Math.abs(left);
                    float alpha = calcuIndicatorAlphaAtDistance(distance, mWidth, ind_width, monthTitleWidth);
                    indicator_left.setAlpha(alpha);
                    indicator_right.setAlpha(alpha);
                }
                ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
                if(mDragListener != null) {
                    mDragListener.onDrag(childMiddle, left, dx);
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_IDLE:
                    MonthView old = childMiddle;
                    // swap their position to make childMiddle still in middle
                    if(childMiddle.getLeft() == -mWidth) {
                        MonthView temp = childLeft;
                        childLeft = childMiddle;
                        childMiddle = childRight;
                        childRight = temp;
                        childRight.offsetLeftAndRight(3 * mWidth);
                    } else if(childMiddle.getLeft() == mWidth) {
                        MonthView temp = childRight;
                        childRight = childMiddle;
                        childMiddle = childLeft;
                        childLeft = temp;
                        childLeft.offsetLeftAndRight(-3 * mWidth);
                    }

                    if(childMiddle != old) {
                        monthChanged(old);
                        // swap listener to current middle
                        childMiddle.setOnMonthTitleClickListener(old.getOnMonthTitleClickListener());
                        childMiddle.setOnDayClickListener(old.getOnDayClickListener());
                        old.setOnMonthTitleClickListener(null);
                        old.setOnDayClickListener(null);
                    }
                    break;
            }
        }
    }

    // when MonthViewPager start to scroll to left.
    private void onScrollToLeft() {
        // destination is edge, hide left indicator
        if (childLeft.getCurrentMonth().equals(leftEdge))
            if (mShowIndicator) {
                indicator_left.setVisibility(View.GONE);
            }
    }

    // when MonthViewPager start to scroll to right.
    private void onScrollToRight() {
        // destination is edge, hide right indicator
        if (childRight.getCurrentMonth().equals(rightEdge))
            if (mShowIndicator) {
                indicator_right.setVisibility(View.GONE);
            }
    }

    private class BtnClicker implements OnClickListener {
        @Override
        public void onClick(View v) {
            if(v == indicator_left) {
                onScrollToLeft();
                dragger.smoothSlideViewTo(childMiddle, mWidth, month_marginTop);
            } else if(v == indicator_right) {
                onScrollToRight();
                dragger.smoothSlideViewTo(childMiddle, -mWidth, month_marginTop);
            }
            ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
        }
    }

    /**
     * set day click listener
     * @param onDayClickListener listener
     */
    public void setOnDayClickListener(MonthView.OnDayClickListener onDayClickListener) {
        childMiddle.setOnDayClickListener(onDayClickListener);
    }

    /**
     * set month label click listener
     * @param onMonthTitleClickListener listener
     */
    public void setOnMonthTitleClickListener(MonthView.OnMonthTitleClickListener onMonthTitleClickListener) {
        childMiddle.setOnMonthTitleClickListener(onMonthTitleClickListener);
    }

    /**
     * set a listener to listen current showing month changed event in MonthViewPager
     * @param listener listener
     */
    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        this.mChangeListener = listener;
    }

    /**
     * set a listener to listen MonthViewPager drag event;
     * @param onDragListener listener
     */
    public void setOnDragListener(OnDragListener onDragListener) {
        this.mDragListener = onDragListener;
    }

    public interface OnMonthChangeListener {
        /**
         * current month has changed
         * @param monthViewPager MonthViewPager
         * @param previous left MonthView (may be null when at the left edge)
         * @param current current MonthView
         * @param next right MonthView (may be null when at the right edge)
         * @param currentMonth new month
         * @param old old month
         */
        void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old);
    }

    public interface OnDragListener {
        /**
         * drag callback
         * @param middle current middle
         * @param left left
         * @param dx x offset
         */
        void onDrag(MonthView middle, int left, int dx);
    }
}
