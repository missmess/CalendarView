package com.missmess.calendarview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * MonthView can show a month, with week label in calendar style.
 * others:
 * 1.add day selected listener.
 * 2.decor days as you like.
 * 3.provide a lot of attribute, you can customize your own style.
 * ...
 */
public class MonthView extends View {
    protected final int DEFAULT_NUM_ROWS = 6;
    protected int dayCircleRadius;
    protected int SPACE_BETWEEN_WEEK_AND_DAY = 0;
    protected int normalDayTextSize;
    protected int WEEK_LABEL_TEXT_SIZE;
    protected int MONTH_HEADER_HEIGHT;
    protected int WEEK_LABEL_HEIGHT;
    protected int MONTH_LABEL_TEXT_SIZE;
    protected boolean mShowMonthTitle;
    protected boolean mShowWeekLabel;
    protected int mSelectedCircleColor;
    protected int spaceBetweenWeekAndDivider;
    protected int monthHeaderSizeCache;
    protected int weekLabelOffset = 0;
    protected int monthLabelOffset = 0;

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mWeekLabelPaint;
    protected Paint mDayNumPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mDayCirclePaint;

    protected int circleTextColor;
    protected int mMonthTextColor;
    protected int mWeekColor;
    protected int normalDayTextColor;
    protected int todayCircleBgColor;
    protected int todayTextColor;

    protected int mWeekStart = Calendar.SUNDAY;
    protected int mNumDays = 7;
    protected int mNumCells = mNumDays;
    private int mDayOfWeekStart = 0;
    private int mMonth = 0;
    protected int dayRowHeight = 0;
    protected int mWidth;
    private int mYear = 0;
    protected Calendar today;

    private int mNumRows = DEFAULT_NUM_ROWS;

    private static final String DAY_OF_WEEK_FORMAT = "EEEEE";
    private OnDayClickListener mOnDayClickListener;
    private OnMonthTitleClickListener mOnMonthClicker;
    private int selectedDay = 0;
    private float downX;
    private float downY;
    protected HashMap<Integer, Integer> decorColors;
    private boolean mShowWeekDivider;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MonthView);

        Resources resources = context.getResources();
        today = Calendar.getInstance();

        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        circleTextColor = typedArray.getColor(R.styleable.MonthView_dayCircleTextColor, resources.getColor(R.color.day_label_text_today_color));
        mMonthTextColor = typedArray.getColor(R.styleable.MonthView_monthTitleColor, resources.getColor(R.color.month_title_color));
        mWeekColor = typedArray.getColor(R.styleable.MonthView_weekLabelTextColor, resources.getColor(R.color.week_label_text_color));
        normalDayTextColor = typedArray.getColor(R.styleable.MonthView_dayTextColor, resources.getColor(R.color.day_label_text_color));
        todayCircleBgColor = typedArray.getColor(R.styleable.MonthView_todayCircleBgColor, resources.getColor(R.color.day_label_circle_bg_color));
        todayTextColor = typedArray.getColor(R.styleable.MonthView_todayTextColor, resources.getColor(R.color.today_text_color));
        mSelectedCircleColor = typedArray.getColor(R.styleable.MonthView_selectDayCircleBgColor, resources.getColor(R.color.day_select_circle_bg_color));

        normalDayTextSize = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayTextSize, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.MonthView_monthTextSize, resources.getDimensionPixelSize(R.dimen.text_size_month));
        WEEK_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.MonthView_weekLabelTextSize, resources.getDimensionPixelSize(R.dimen.text_size_week));
        MONTH_HEADER_HEIGHT = monthHeaderSizeCache =  typedArray.getDimensionPixelOffset(R.styleable.MonthView_monthHeaderHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        dayCircleRadius = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayCircleRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));

        dayRowHeight = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayRowHeight, resources.getDimensionPixelOffset(R.dimen.row_height));
        mShowMonthTitle = typedArray.getBoolean(R.styleable.MonthView_showMonthTitle, true);
        mShowWeekLabel = typedArray.getBoolean(R.styleable.MonthView_showWeekLabel, true);
        mShowWeekDivider = typedArray.getBoolean(R.styleable.MonthView_showWeekDivider, false);

        spaceBetweenWeekAndDivider = resources.getDimensionPixelSize(R.dimen.week_label_between_divider_size);
        if(!mShowMonthTitle) {
            MONTH_HEADER_HEIGHT = 0;
        }
        if(!mShowWeekLabel) {
            WEEK_LABEL_HEIGHT = 0;
        } else {
            WEEK_LABEL_HEIGHT = WEEK_LABEL_TEXT_SIZE + spaceBetweenWeekAndDivider;
        }

        typedArray.recycle();
        mPadding = getPaddingLeft();
        decorColors = new HashMap<>();
        initView();
        setYearAndMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1);
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 设置当前时间
     * @param today 当前时间
     */
    public void setToday(Calendar today) {
        this.today = today;
    }

    private void drawWeekLabels(Canvas canvas) {
        int y = MONTH_HEADER_HEIGHT + WEEK_LABEL_TEXT_SIZE + weekLabelOffset;
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            if (calendarDay == 0)
                calendarDay = mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;

            final Locale locale = getResources().getConfiguration().locale;
            SimpleDateFormat mDayOfWeekFormatter = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, locale);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDayOfWeekFormatter.format(cal.getTime()), x, y, mWeekLabelPaint);
        }

        if(mShowWeekDivider) {
            //draw divider under week label
            int yLine = MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT + weekLabelOffset;
            canvas.drawLine(mPadding, yLine - 1, mWidth - mPadding, yLine, mWeekLabelPaint);
        }
    }

    private void drawMonthTitle(Canvas canvas) {
//        Log.e("MonthView", "drawMonthTitle");
        int[] pos = getMonthDrawPoint();
//        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
//        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(getMonthAndYearString(), pos[0], pos[1], mMonthTitlePaint);
    }

    /**
     * draw the day of month
     */
    protected void drawMonthDays(Canvas canvas) {
        int y = (dayRowHeight + normalDayTextSize) / 2 + SPACE_BETWEEN_WEEK_AND_DAY + MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;

        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            String dayStr = String.format(Locale.getDefault(), "%d", day);
            Rect rect = new Rect();
            mDayNumPaint.getTextBounds(dayStr, 0, dayStr.length(), rect);
            if(day == selectedDay) {
                mDayCirclePaint.setColor(mSelectedCircleColor);
                canvas.drawCircle(x, y - rect.height() / 2, dayCircleRadius, mDayCirclePaint);

                mDayNumPaint.setColor(circleTextColor);
                canvas.drawText(dayStr, x, y, mDayNumPaint);
            }  else if(decorColors.get(day) != null){
                Integer color = decorColors.get(day);
                mDayCirclePaint.setColor(color);
                canvas.drawCircle(x, y - rect.height() / 2, dayCircleRadius, mDayCirclePaint);

                mDayNumPaint.setColor(circleTextColor);
                canvas.drawText(dayStr, x, y, mDayNumPaint);
            } else if (isToday(mYear, mMonth, day)) { //today
                mDayCirclePaint.setColor(todayCircleBgColor);
                canvas.drawCircle(x, y - rect.height() / 2, dayCircleRadius, mDayCirclePaint);

                mDayNumPaint.setColor(todayTextColor);
                mDayNumPaint.setFakeBoldText(true);
                canvas.drawText(dayStr, x, y, mDayNumPaint);
                mDayNumPaint.setFakeBoldText(false);
            } else { //not today
                mDayNumPaint.setColor(normalDayTextColor);
                canvas.drawText(dayStr, x, y, mDayNumPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += dayRowHeight;
            }
            day++;
        }
    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart) - mWeekStart;
    }

    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth);
        long millis = calendar.getTimeInMillis();
        return DateUtils.formatDateTime(getContext(), millis, flags);
    }

    private void onDayClick(CalendarDay calendarDay) {
        selectedDay = calendarDay.getDay();
        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
        invalidate();
    }

    private boolean isToday(int year, int month, int monthDay) {
        return (year == today.get(Calendar.YEAR)) && (month == today.get(Calendar.MONTH)) && (monthDay == today.get(Calendar.DAY_OF_MONTH));
    }

    private CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        float yDayOffset = y - MONTH_HEADER_HEIGHT - WEEK_LABEL_HEIGHT - SPACE_BETWEEN_WEEK_AND_DAY;
        if(yDayOffset < 0)
            return null;

        int yDay = (int) yDayOffset / dayRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        return new CalendarDay(mYear, mMonth + 1, day);
    }

    private boolean isClickMonth(int x, int y) {
        int[] pos = getMonthDrawPoint();
        int centerX = pos[0];
        int bottom = pos[1];
        int extra = 10;
        int width = (int) mMonthTitlePaint.measureText(getMonthAndYearString());
        Rect monthTitleRect = new Rect(centerX - width / 2 - extra, bottom - MONTH_LABEL_TEXT_SIZE - extra,
                centerX + width / 2 + extra, bottom + extra);

        return monthTitleRect.contains(x, y);
    }

    private int[] getMonthDrawPoint() {
        int x = mWidth / 2;
        int y = MONTH_HEADER_HEIGHT / 2 + (MONTH_LABEL_TEXT_SIZE / 3) + monthLabelOffset;
        return new int[] {x, y};
    }

    public void clearSelection() {
        selectedDay = 0;
//        invalidate();
    }

    public void clearDecors() {
        decorColors.clear();
//        invalidate();
    }

    protected void initView() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        mDayCirclePaint = new Paint();
        mDayCirclePaint.setFakeBoldText(true);
        mDayCirclePaint.setAntiAlias(true);
        mDayCirclePaint.setColor(todayCircleBgColor);
        mDayCirclePaint.setTextAlign(Align.CENTER);
        mDayCirclePaint.setStyle(Style.FILL);

        mWeekLabelPaint = new Paint();
        mWeekLabelPaint.setAntiAlias(true);
        mWeekLabelPaint.setTextSize(WEEK_LABEL_TEXT_SIZE);
        mWeekLabelPaint.setColor(mWeekColor);
        mWeekLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mWeekLabelPaint.setStyle(Style.FILL);
        mWeekLabelPaint.setTextAlign(Align.CENTER);
        mWeekLabelPaint.setFakeBoldText(true);

        mDayNumPaint = new Paint();
        mDayNumPaint.setAntiAlias(true);
        mDayNumPaint.setTextSize(normalDayTextSize);
        mDayNumPaint.setStyle(Style.FILL);
        mDayNumPaint.setTextAlign(Align.CENTER);
        mDayNumPaint.setColor(normalDayTextColor);
        mDayNumPaint.setFakeBoldText(false);
    }

    protected void onDraw(Canvas canvas) {
//        Log.d("MonthView", "onDraw");

        if(mShowMonthTitle) {
            drawMonthTitle(canvas);
        }
        if(mShowWeekLabel) {
            drawWeekLabels(canvas);
        }
        drawMonthDays(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        int height = 0;
//        switch (heightMode) {
//            case MeasureSpec.AT_MOST:
//                height = getShouldHeight();
//                break;
//            case MeasureSpec.EXACTLY:
//            case MeasureSpec.UNSPECIFIED:
//                height = heightSize;
//                break;
//        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getShouldHeight());
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            // still consume touch event
            return true;
        }
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(downX - x) < 10
                        && Math.abs(downY - y) < 10
                        && event.getEventTime() - event.getDownTime() < 500) {
                    CalendarDay calendarDay = getDayFromLocation(x, y);
                    if (calendarDay != null) {
                        onDayClick(calendarDay);
                    } else if(isClickMonth((int)x, (int)y)) {
                        // month title clicked
                        if(mOnMonthClicker != null) {
                            mOnMonthClicker.onMonthClick(this, new CalendarMonth(mYear, mMonth));
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置当前显示的年和月
     * @param year 年
     * @param month 月
     */
    public void setYearAndMonth(int year, int month) {
        clearSelection();
        clearDecors();

        mYear = year;
        mMonth = month - 1;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, mMonth);
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = calendar.get(Calendar.DAY_OF_WEEK);

        mWeekStart = calendar.getFirstDayOfWeek();

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);

        mNumRows = calculateNumRows();
    }

    public void decorateDay(int day, @ColorInt int color) {
        if(day < 1 || day > mNumCells) {
            throw new IllegalArgumentException("day " + day + " does not exist in this month");
        }
        decorColors.put(day, color);
        invalidate();
    }

    public void showMonthTitle(boolean show) {
        this.mShowMonthTitle = show;
        if(!mShowMonthTitle) {
            MONTH_HEADER_HEIGHT = 0;
        } else {
            MONTH_HEADER_HEIGHT = monthHeaderSizeCache;
        }
    }

    public void showWeekLabel(boolean show) {
        this.mShowWeekLabel = show;
        if(!mShowWeekLabel) {
            WEEK_LABEL_HEIGHT = 0;
        } else {
            WEEK_LABEL_HEIGHT = WEEK_LABEL_TEXT_SIZE + spaceBetweenWeekAndDivider;
        }
    }

    public void setNormalDayTextColor(@ColorInt int color) {
        normalDayTextColor = color;
    }

    public void setNormalDayTextSize(int px) {
        normalDayTextSize = px;
        mDayNumPaint.setTextSize(normalDayTextSize);
    }

    public void setDayCircleRadius(int px) {
        dayCircleRadius = px;
    }

    public void setDayRowHeight(int px) {
        dayRowHeight = px;
    }

    public void setCircleTextColor(@ColorInt int color) {
        circleTextColor = color;
    }

    public void setTodayCircleBgColor(@ColorInt int color) {
        todayCircleBgColor = color;
    }

    public void setTodayTextColor(@ColorInt int color) {
        todayTextColor = color;
    }

    void setWeekLabelOffset(int weekLabelOffset) {
        this.weekLabelOffset = weekLabelOffset;
        invalidate();
    }

    void setMonthLabelOffset(int monthLabelOffset) {
        this.monthLabelOffset = monthLabelOffset;
        invalidate();
    }

    /**
     * MonthView should be what height.
     * @return should height
     */
    public int getShouldHeight() {
        return MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT + SPACE_BETWEEN_WEEK_AND_DAY + dayRowHeight * mNumRows;
    }

    public int getCurrentMonth() {
        return mMonth + 1;
    }

    public int getCurrentYear() {
        return mYear;
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public void setOnMonthTitleClickListener(OnMonthTitleClickListener onMonthTitleClickListener) {
        this.mOnMonthClicker = onMonthTitleClickListener;
    }

    public interface OnDayClickListener {
        void onDayClick(MonthView monthView, CalendarDay calendarDay);
    }

    public interface OnMonthTitleClickListener {
        void onMonthClick(MonthView monthView, CalendarMonth calendarMonth);
    }
}