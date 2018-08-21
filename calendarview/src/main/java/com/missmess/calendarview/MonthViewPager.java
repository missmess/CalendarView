package com.missmess.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * MonthViewPager should contains one MonthView child to config their styles and attributes, the child will only
 * remain its attributes, so do not find this child by findViewById, use MonthViewPager
 * to do everything (e.g.: add listener, set showing month) instead.
 * <p>
 * <p>MonthViewPager is similar to a ViewPager, it is adapt to display a sort of MonthView with a same styles
 * and a sequential month.</p>
 *
 * @author wl
 * @since 2016/08/26 15:41
 */
@CoordinatorLayout.DefaultBehavior(MonthBehavior.class)
public class MonthViewPager extends ViewGroup {
    private static final int VEL_THRESHOLD = 3000;
    private ViewDragHelper dragger;
    private MonthView childLeft;
    private MonthView childMiddle;
    private MonthView childRight;
    private int mWidth;
    private Drawable ic_previous;
    private Drawable ic_next;
    protected ImageView indicator_left;
    protected ImageView indicator_right;
    private int indicate_margin;
    private EnhancedSelectionListener mSelectListener;
    private CalendarDay leftEdge;
    private CalendarDay rightEdge;
    private boolean leftAble = true;
    private boolean rightAble = true;
    private OnDragListener mDragListener;
    private List<OnMonthChangeListener> mChangeListeners;
    private boolean mShowIndicator;
    private DayDecor mDecors;
    private int month_marginTop;
    private boolean mShowOtherMonth;
    private int mOtherMonthColor;
    private boolean mMonthMode;
    private CalendarDay lastInteractDay;

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MonthViewPager);
        mShowIndicator = typedArray.getBoolean(R.styleable.MonthViewPager_show_indicator, true);
        mShowOtherMonth = typedArray.getBoolean(R.styleable.MonthViewPager_showOtherMonth, false);
        mOtherMonthColor = typedArray.getColor(R.styleable.MonthViewPager_otherMonthTextColor, context.getResources().getColor(R.color.day_other_month_text_color));
        ic_previous = typedArray.getDrawable(R.styleable.MonthViewPager_ic_previous_month);
        ic_next = typedArray.getDrawable(R.styleable.MonthViewPager_ic_next_month);
        month_marginTop = typedArray.getDimensionPixelSize(R.styleable.MonthViewPager_month_marginTop, 0);
        if (ic_previous == null) {
            ic_previous = context.getResources().getDrawable(R.mipmap.ic_previous);
        }
        if (ic_next == null) {
            ic_next = context.getResources().getDrawable(R.mipmap.ic_next);
        }

        indicate_margin = context.getResources().getDimensionPixelSize(R.dimen.icon_margin);
        typedArray.recycle();

        init();
    }

    private void init() {
        dragger = ViewDragHelper.create(this, 1f, new DragCallBack());
        mSelectListener = new EnhancedSelectionListener();
        leftEdge = new CalendarDay(1900, 2, 1);
        rightEdge = new CalendarDay(2049, 12, 31);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        // called by system
        if (getChildCount() > 0 || !(child instanceof MonthView)) {
            throw new IllegalStateException("MonthViewPager can host only one MonthView child");
        }
        childMiddle = (MonthView) child;
        childLeft = childMiddle.staticCopy();
        childLeft.setYearAndMonth(childMiddle.getCurrentMonth().previous());
        childRight = childMiddle.staticCopy();
        childRight.setYearAndMonth(childMiddle.getCurrentMonth().next());

        childMiddle.setOnSelectionChangeListener(mSelectListener);
        applyEdge2Children();
        // add param
        addChildAttrs();

        // add three child
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        super.addView(childLeft, params);
        super.addView(childMiddle, params);
        super.addView(childRight, params);

        // add indicators
        if (mShowIndicator) {
            indicator_left = createIndicator(ic_previous);
            indicator_left.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    smoothScrollToLeft();
                }
            });
            super.addView(indicator_left);
            indicator_right = createIndicator(ic_next);
            indicator_right.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    smoothScrollToRight();
                }
            });
            super.addView(indicator_right);
        }
    }

    private void addChildAttrs() {
        setShowOtherMonthInternal(mShowOtherMonth);
        setOtherMonthColorInternal(mOtherMonthColor);
        if (childMiddle.isMonthMode()) {
            mMonthMode = true;
            setMonthModeInternal();
        } else {
            mMonthMode = false;
            setWeekModeInternal();
        }
    }

    private ImageView createIndicator(Drawable icon) {
        ImageView imageBtn = new ImageView(getContext());
        imageBtn.setScaleType(ImageView.ScaleType.CENTER);
        imageBtn.setImageDrawable(icon);
        imageBtn.setBackgroundResource(getThemeSelectableBackgroundId(getContext()));
        imageBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
     *
     * @param calendarMonth month
     */
    public void setCurrentMonth(@NonNull CalendarMonth calendarMonth) {
        CalendarMonth oldMonth = childMiddle.getCurrentMonth();
        if (calendarMonth.equals(oldMonth))
            return;

        int code = isInRange(calendarMonth);
        if (code == -1) {
            calendarMonth = leftEdge.getCalendarMonth();
        }

        if (code == 1) {
            calendarMonth = rightEdge.getCalendarMonth();
        }

        // set current month
        childMiddle.setYearAndMonth(calendarMonth);
        // month changed
        onMiddleChildChanged(oldMonth);
    }

    public void setToday(CalendarDay today) {
        childLeft.setToday(today);
        childMiddle.setToday(today);
        childRight.setToday(today);
    }

    public void setDayLabel(CalendarDay day, String label) {
        childLeft.setDayLabel(day, label);
        childMiddle.setDayLabel(day, label);
        childRight.setDayLabel(day, label);
    }

    public CalendarDay getToday() {
        return childMiddle.getToday();
    }

    public CalendarMonth getCurrentMonth() {
        return childMiddle.getCurrentMonth();
    }

    /**
     * Get current showing MonthView child, you can use this to call get method, NOTIFY that NOT to
     * call any set method upon this return value directly, use set method in MonthViewPager instead.
     * @return current showing child
     */
    public MonthView getCurrentChild() {
        return childMiddle;
    }

    /**
     * set range of month showing in MonthViewPager.
     *
     * @param start start month
     * @param end   end month
     */
    public void setMonthRange(CalendarMonth start, CalendarMonth end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("start month cannot larger than end month");
        }
        leftEdge = new CalendarDay(start, 1);
        rightEdge = new CalendarDay(end, CalendarUtils.getDaysInMonth(end));

        int code = isInRange(childMiddle.getCurrentMonth());
        if (code == -1) {
            setCurrentMonth(leftEdge.getCalendarMonth());
        }
        if (code == 1) {
            setCurrentMonth(rightEdge.getCalendarMonth());
        }

        applyEdge2Children();
        checkIfInEdge();
    }

    private void applyEdge2Children() {
        if (childMiddle != null) {
            childLeft.leftEdgeDay(leftEdge);
            childLeft.rightEdgeDay(rightEdge);
            childMiddle.leftEdgeDay(leftEdge);
            childMiddle.rightEdgeDay(rightEdge);
            childRight.leftEdgeDay(leftEdge);
            childRight.rightEdgeDay(rightEdge);
        }
    }

    /**
     * Do works that check if in edge, and setup left and right child,
     * call this when middle child changed, this means:
     * <p>
     * <li>after scrolling, middle child has changed to another view.</li>
     * <li>middle child changes its showing month.</li>
     * <li>middle child changed its display mode or week index</li>
     *
     * @param oldMonth old month
     */
    private void onMiddleChildChanged(CalendarMonth oldMonth) {
        checkIfInEdge();

        MonthView middle = childMiddle;
        CalendarMonth currentMonth = middle.getCurrentMonth();

        // code below do works that setup left and right child.
        MonthView left = childLeft;
        MonthView right = childRight;

        if (mMonthMode) {
            setSiblingInMonthMode(currentMonth);
        } else {
            setSiblingInWeekMode(middle.getWeekIndex());
        }

        // call listeners
        boolean monthChanged = !oldMonth.equals(currentMonth);
        if (monthChanged && mChangeListeners != null) {
            for (OnMonthChangeListener listener : mChangeListeners) {
                if (listener != null)
                    listener.onMonthChanged(this, left, middle, right, currentMonth, oldMonth);
            }
        }
    }

    // setup left and right MonthView when in month mode
    private void setSiblingInMonthMode(CalendarMonth currentMonth) {
        if (!mMonthMode)
            return;

        if (leftAble) {
            childLeft.setYearAndMonth(currentMonth.previous());
            childLeft.setWeekIndex(0);
        }
        if (rightAble) {
            childRight.setYearAndMonth(currentMonth.next());
            childRight.setWeekIndex(0);
        }
    }

    // setup left and right MonthView when in week mode
    private void setSiblingInWeekMode(int middleWeekIndex) {
        if (mMonthMode)
            return;

        MonthView middle = childMiddle;
        CalendarMonth currentMonth = middle.getCurrentMonth();
        if (leftAble) {
            MonthView left = childLeft;
            int week = middleWeekIndex - 1;
            if (week < 0) {
                left.setYearAndMonth(currentMonth.previous());
                if (middle.findDayOffset() == 0)
                    left.setWeekIndex(left.getWeekRows() - 1);
                else
                    left.setWeekIndex(left.getWeekRows() - 2);
            } else {
                left.setYearAndMonth(currentMonth);
                left.setWeekIndex(week);
            }
        }
        if (rightAble) {
            MonthView right = childRight;
            int week = middleWeekIndex + 1;
            if (week > middle.getWeekRows() - 1) {
                right.setYearAndMonth(currentMonth.next());
                if (right.findDayOffset() == 0)
                    right.setWeekIndex(0);
                else
                    right.setWeekIndex(1);
            } else {
                right.setYearAndMonth(currentMonth);
                right.setWeekIndex(week);
            }
        }
    }

    /**
     * Check if the month is in range.
     *
     * @param cm month
     * @return -1 - smaller than the minimum month, 1 - larger than the maximum month, 0 - in range.
     */
    private int isInRange(CalendarMonth cm) {
        if (cm.compareTo(leftEdge.getCalendarMonth()) < 0)
            return -1;

        if (cm.compareTo(rightEdge.getCalendarMonth()) > 0)
            return 1;

        return 0;
    }

    /**
     * Check if current month on the left or right edge.
     */
    private void checkIfInEdge() {
        CalendarMonth cm = childMiddle.getCurrentMonth();

        // In month mode, if current month equals edge month, we are at edge.
        // In week mode, if current month equals edge month and week index equals 0, we are at edge.
        if ((mMonthMode && cm.equals(leftEdge.getCalendarMonth()))
                || (!mMonthMode && cm.equals(leftEdge.getCalendarMonth()) && childMiddle.getWeekIndex() == 0)) {
            if (indicator_left != null)
                indicator_left.setVisibility(View.GONE);
            leftAble = false;
        } else {
            if (indicator_left != null)
                indicator_left.setVisibility(View.VISIBLE);
            leftAble = true;
        }
        if ((mMonthMode && cm.equals(rightEdge.getCalendarMonth()))
                || (!mMonthMode && cm.equals(rightEdge.getCalendarMonth()) && childMiddle.getWeekIndex() == childMiddle.getWeekRows() - 1)) {
            if (indicator_right != null)
                indicator_right.setVisibility(View.GONE);
            rightAble = false;
        } else {
            if (indicator_right != null)
                indicator_right.setVisibility(View.VISIBLE);
            rightAble = true;
        }
    }

    public void setDecors(DayDecor decors) {
        this.mDecors = decors;
        if (childMiddle != null) {
            childLeft.setDecors(decors);
            childMiddle.setDecors(decors);
            childRight.setDecors(decors);
        }
    }

    public void setDayDisable(@NonNull CalendarDay disable) {
        if (childMiddle != null) {
            childLeft.setDayDisable(disable);
            childMiddle.setDayDisable(disable);
            childRight.setDayDisable(disable);
        }
    }

    public void clearDisable() {
        if (childMiddle != null) {
            childLeft.clearDisable();
            childMiddle.clearDisable();
            childRight.clearDisable();
        }
    }

    public DayDecor getDecors() {
        return mDecors;
    }

    public boolean isShowingIndicator() {
        return mShowIndicator;
    }

    public void setShowOtherMonth(boolean show) {
        this.mShowOtherMonth = show;
        setShowOtherMonthInternal(show);
    }

    public boolean isShowingOtherMonth() {
        return childMiddle.isOtherMonthShowing();
    }

    public void setOtherMonthColor(@ColorInt int color) {
        this.mOtherMonthColor = color;
        setOtherMonthColorInternal(color);
    }

    private void setOtherMonthColorInternal(int color) {
        if (childMiddle != null) {
            childLeft.setOtherMonthTextColor(color);
            childMiddle.setOtherMonthTextColor(color);
            childRight.setOtherMonthTextColor(color);
        }
    }

    private void setShowOtherMonthInternal(boolean show) {
        if (childMiddle != null) {
            childLeft.setShowOtherMonth(show);
            childMiddle.setShowOtherMonth(show);
            childRight.setShowOtherMonth(show);
        }
    }

    /**
     * select specified calendar day.
     *
     * @param calendarDay calendarDay; null to clear selection.
     */
    public void setSelection(CalendarDay calendarDay) {
        if (calendarDay.compareTo(leftEdge) < 0)
            return;
        if (calendarDay.compareTo(rightEdge) > 0)
            return;
        if (childMiddle != null) {
            childMiddle.setSelection(calendarDay);
        }
    }

    /**
     * set style of selected day
     *
     * @param selectionStyle Style
     */
    public void setSelectionStyle(DayDecor.Style selectionStyle) {
        if (childMiddle != null) {
            childLeft.setSelectionStyle(selectionStyle);
            childMiddle.setSelectionStyle(selectionStyle);
            childRight.setSelectionStyle(selectionStyle);
        }
    }

    /**
     * Smooth slide to left page
     *
     * @return false - in left edge we can not able to slide left again.
     */
    public boolean smoothScrollToLeft() {
        if (!leftAble)
            return false;

        onScrollToLeft();
        dragger.smoothSlideViewTo(childMiddle, mWidth, month_marginTop);
        ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
        return true;
    }

    /**
     * Smooth slide to right page
     *
     * @return false - in right edge we can not able to slide right again.
     */
    public boolean smoothScrollToRight() {
        if (!rightAble)
            return false;

        onScrollToRight();
        dragger.smoothSlideViewTo(childMiddle, -mWidth, month_marginTop);
        ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
        return true;
    }

    int getShouldHeightInMonthMode() {
        // month mode, height should be 6 rows height
        return childMiddle.getHeightWithRows(6) + month_marginTop;
    }

    int getShouldHeightInWeekMode() {
        // week mode, height should be 1 row height
        return childMiddle.getHeightWithRows(1) + month_marginTop;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (childLeft == null || childMiddle == null || childRight == null) {
            throw new IllegalStateException("MonthViewPager should host a MonthView child");
        }
        int height;
        if(mMonthMode) {
            height = getShouldHeightInMonthMode();
        } else {
            height = getShouldHeightInWeekMode();
        }

        //measure MonthView children
        int childWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.AT_MOST);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        childLeft.measure(childWidthSpec, childHeightSpec);
        childMiddle.measure(childWidthSpec, childHeightSpec);
        childRight.measure(childWidthSpec, childHeightSpec);

        // measure indicator ImageView
        if (mShowIndicator) {
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
        childMiddle.layout(0, paddingTop, mWidth, childMiddle.getMeasuredHeight() + paddingTop);
        childLeft.layout(-mWidth, paddingTop, 0, childLeft.getMeasuredHeight() + paddingTop);
        childRight.layout(mWidth, paddingTop, 2 * mWidth, childRight.getMeasuredHeight() + paddingTop);

        if (mShowIndicator) {
            int month_header_height = childMiddle.MONTH_HEADER_HEIGHT;
            int left_height = indicator_left.getMeasuredHeight();
            int right_height = indicator_right.getMeasuredHeight();
            int top1 = (month_header_height + paddingTop - left_height) / 2;
            int top2 = (month_header_height + paddingTop - right_height) / 2;
            if (top1 < 0)
                top1 = 0;
            if (top2 < 0)
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
        if (dragger.continueSettling(true)) {
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

    boolean isDraggerIdle() {
        return dragger.getViewDragState() == ViewDragHelper.STATE_IDLE;
    }

    public void setMonthMode() {
        if (mMonthMode)
            return;

        setMonthModeInternal();
    }

    private void setMonthModeInternal() {
        mMonthMode = true;
        childLeft.showMonthMode();
        childMiddle.showMonthMode();
        childRight.showMonthMode();
        onMiddleChildChanged(getCurrentMonth());
    }

    public void setWeekMode() {
        if (!mMonthMode)
            return;

        setWeekModeInternal();
    }

    public int getLineIndex(CalendarDay day) {
        return childMiddle.getLineIndex(day);
    }

    /**
     * Show specified week.
     * @param weekIndex week index, should be correct in current month.
     */
    public void setWeekIndex(int weekIndex) {
        boolean done = childMiddle.setWeekIndex(weekIndex);
        // if week mode, setup sibling views to show correctly.
        if (!mMonthMode && done) {
            onMiddleChildChanged(getCurrentMonth());
        }
    }

    private void setWeekModeInternal() {
        mMonthMode = false;
        childLeft.showWeekMode();
        childMiddle.showWeekMode();
        childRight.showWeekMode();

        onMiddleChildChanged(getCurrentMonth());
    }

    public boolean isMonthMode() {
        return mMonthMode;
    }

    /**
     * Be one of the {@link MonthView#SELECTION_SINGLE}, {@link MonthView#SELECTION_MULTI},
     * {@link MonthView#SELECTION_RANGE}, {@link MonthView#SELECTION_NONE}
     * @param selectionMode mode
     */
    public void setSelectionMode(int selectionMode) {
        if (childMiddle != null) {
            childLeft.setSelectionMode(selectionMode);
            childMiddle.setSelectionMode(selectionMode);
            childRight.setSelectionMode(selectionMode);
        }
    }

    public int getSelectionMode() {
        return childMiddle.getSelectionMode();
    }

    public CalendarDay[] getSelection() {
        return childMiddle.getSelection();
    }

//    private int applySelectionLineAsWeekIndex() {
//        int selectionLineIndex = childMiddle.getSelectionLineIndex();
//        childMiddle.setWeekIndex(selectionLineIndex == -1 ? 0 : selectionLineIndex);
//        return selectionLineIndex;
//    }

    /**
     * The visible day range of current month, including other month if other-month enabled.
     * @return array with 2 elements, first visible day and last visible day.
     */
    public CalendarDay[] getShowingDayRange() {
        return childMiddle.getShowingDayRange();
    }

    /**
     * The month day range of current month.
     * @return first day of month and last day of month.
     */
    public CalendarDay[] getMonthDayRange() {
        return childMiddle.getMonthDayRange();
    }

    /**
     * Maximum scroll range for scrolling.
     *
     * @return return the distance between week index and top.
     */
    int getMaximumScrollRange() {
        MonthView monthView = childMiddle;
        int index = monthView.getWeekIndex();
        return monthView.getHeightWithRows(index);
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
            if (left > 0 && !leftAble) {
                // can't move left
                return 0;
            }
            if (left < 0 && !rightAble) {
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

            if (dragger.settleCapturedViewAt(finalLeft, month_marginTop)) {
                ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == childMiddle) { // childMiddle is scrolling
                // offset left and right children
                childLeft.offsetLeftAndRight(dx);
                childRight.offsetLeftAndRight(dx);
                // if close to edge, alpha to 0;
                // alpha indicators
                if (mShowIndicator) {
                    int monthTitleWidth = childMiddle.getMonthTitleWidth();
                    int ind_width = indicator_left.getRight();
                    int distance = Math.abs(left);
                    float alpha = calcuIndicatorAlphaAtDistance(distance, mWidth, ind_width, monthTitleWidth);
                    indicator_left.setAlpha(alpha);
                    indicator_right.setAlpha(alpha);
                }
                ViewCompat.postInvalidateOnAnimation(MonthViewPager.this);
                if (mDragListener != null) {
                    mDragListener.onDrag(childMiddle, left, dx);
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_IDLE:
                    MonthView old = childMiddle;
                    boolean toleft = false;
                    // swap their position to make childMiddle still in middle
                    if (childMiddle.getLeft() == -mWidth) {
                        MonthView temp = childLeft;
                        childLeft = childMiddle;
                        childMiddle = childRight;
                        childRight = temp;
                        childRight.offsetLeftAndRight(3 * mWidth);
                        toleft = false;
                    } else if (childMiddle.getLeft() == mWidth) {
                        MonthView temp = childRight;
                        childRight = childMiddle;
                        childMiddle = childLeft;
                        childLeft = temp;
                        childLeft.offsetLeftAndRight(-3 * mWidth);
                        toleft = true;
                    }

                    if (childMiddle != old) {
                        CalendarMonth oldMonth = old.getCurrentMonth();
                        onMiddleChildChanged(oldMonth);
                        // swap listener to current middle
                        childMiddle.setOnMonthTitleClickListener(old.getOnMonthTitleClickListener());
                        childMiddle.setOnSelectionChangeListener(mSelectListener);
                        old.setOnMonthTitleClickListener(null);
                        old.setOnSelectionChangeListener(null);
                    }
                    break;
            }
        }
    }

    // when MonthViewPager start to scroll to left.
    private void onScrollToLeft() {
        // destination is edge, hide left indicator
        if (childLeft.getCurrentMonth().equals(leftEdge.getCalendarMonth()))
            if (mShowIndicator) {
                indicator_left.setVisibility(View.GONE);
            }
    }

    // when MonthViewPager start to scroll to right.
    private void onScrollToRight() {
        // destination is edge, hide right indicator
        if (childRight.getCurrentMonth().equals(rightEdge.getCalendarMonth()))
            if (mShowIndicator) {
                indicator_right.setVisibility(View.GONE);
            }
    }

    private void onSelectionInLeftOtherMonth() {
        if (mMonthMode) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    // be about to scroll to left, set left view index to the last row
                    childLeft.setWeekIndex(childLeft.getWeekRows() - 1);
                    smoothScrollToLeft();
                }
            }, 200);
        } else {
            setCurrentMonth(getCurrentMonth().previous());
            // must show the last row
            setWeekIndex(childMiddle.getWeekRows() - 1);
        }
    }

    private void onSelectionInRightOtherMonth() {
        if (mMonthMode) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    // be about to scroll to right, set right view index to 0
                    childRight.setWeekIndex(0);
                    smoothScrollToRight();
                }
            }, 200);
        } else {
            setCurrentMonth(getCurrentMonth().next());
            // must show the first row
            setWeekIndex(0);
        }
    }

    private class EnhancedSelectionListener implements MonthView.OnSelectionChangeListener {
        MonthView.OnSelectionChangeListener mListener;

        void setListener(MonthView.OnSelectionChangeListener listener) {
            mListener = listener;
        }

        @Override
        public void onSelectionChanged(MonthView monthView, CalendarDay[] now, CalendarDay[] old, CalendarDay selection, boolean byUser) {
            // left and right view also should change selection
            childLeft.setSelectionAtom(now);
            childRight.setSelectionAtom(now);

            if (selection != null) {
                int type = childMiddle.getDayType(selection);
                switch (type) {
                    case 0:
                        int index = childMiddle.getLineIndex(selection);
                        childMiddle.setWeekIndex(index);
                        break;
                    case 1:
                        onSelectionInRightOtherMonth();
                        break;
                    case -1:
                        onSelectionInLeftOtherMonth();
                        break;
                    case 2:
                        break;
                    case -2:
                        break;

                }
            }

            // call listener
            if (mListener != null)
                mListener.onSelectionChanged(monthView, now, old, selection, byUser);
        }
    }

    /**
     * Set day selection change listener
     *
     * @param listener listener
     */
    public void setOnSelectionChangeListener(MonthView.OnSelectionChangeListener listener) {
        mSelectListener.setListener(listener);
    }

    /**
     * set month label click listener
     *
     * @param onMonthTitleClickListener listener
     */
    public void setOnMonthTitleClickListener(MonthView.OnMonthTitleClickListener onMonthTitleClickListener) {
        childMiddle.setOnMonthTitleClickListener(onMonthTitleClickListener);
    }

    /**
     * add a listener to listen current showing month changed event in MonthViewPager
     *
     * @param listener listener
     */
    public void addOnMonthChangeListener(OnMonthChangeListener listener) {
        if (mChangeListeners == null) {
            mChangeListeners = new ArrayList<>();
        }
        mChangeListeners.add(listener);
    }

    /**
     * add a listener to listen current showing month changed event in MonthViewPager
     *
     * @param listener listener
     */
    public void removeOnMonthChangeListener(OnMonthChangeListener listener) {
        if (mChangeListeners != null) {
            mChangeListeners.remove(listener);
        }
    }

    /**
     * set a listener to listen MonthViewPager drag event;
     *
     * @param onDragListener listener
     */
    public void setOnDragListener(OnDragListener onDragListener) {
        this.mDragListener = onDragListener;
    }

    public interface OnMonthChangeListener {
        /**
         * current month has changed
         *
         * @param monthViewPager MonthViewPager
         * @param previous       left MonthView (may be null when at the left edge)
         * @param current        current MonthView
         * @param next           right MonthView (may be null when at the right edge)
         * @param currentMonth   new month
         * @param old            old month
         */
        void onMonthChanged(MonthViewPager monthViewPager, @Nullable MonthView previous, MonthView current, @Nullable MonthView next, CalendarMonth currentMonth, CalendarMonth old);
    }

    public interface OnDragListener {
        /**
         * drag callback
         *
         * @param middle current middle
         * @param left   left
         * @param dx     x offset
         */
        void onDrag(MonthView middle, int left, int dx);
    }
}
