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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private final int DEFAULT_NUM_ROWS = 6;
    protected int dayCircleRadius;
    protected int SPACE_BETWEEN_WEEK_AND_DAY = 0;
    protected int normalDayTextSize;
    protected int WEEK_LABEL_TEXT_SIZE;
    protected int MONTH_HEADER_HEIGHT;
    protected int WEEK_LABEL_HEIGHT;
    protected int MONTH_LABEL_TEXT_SIZE;
    protected int mSelectedCircleColor;
    protected int spaceBetweenWeekAndDivider;
    protected int monthHeaderSizeCache;
    protected int weekLabelOffset = 0;
    protected int monthLabelOffset = 0;
    private int mOtherMonthTextColor;

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mWeekLabelPaint;
    protected Paint mDayNumPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mDayBgPaint;

    protected int decorTextColor;
    protected int mMonthTextColor;
    protected int mWeekColor;
    protected int normalDayTextColor;
    protected int todayTextColor;

    protected int mWeekStart = Calendar.SUNDAY;
    protected int mNumDays = 7;
    protected int mNumCells = mNumDays;
    private int mDayOfWeekStart = 0;
    private int mMonth = 0;
    protected int dayRowHeight = 0;
    protected int mWidth;
    private int mYear = 0;
    protected CalendarDay today;

    private int mNumRows = DEFAULT_NUM_ROWS;
    protected boolean mShowMonthTitle;
    protected boolean mShowWeekLabel;
    private boolean mShowWeekDivider;
    protected boolean mShowOtherMonth;

    private static final String DAY_OF_WEEK_FORMAT = "EEEEE";
    private OnDayClickListener mOnDayClickListener;
    private OnMonthTitleClickListener mOnMonthClicker;
    private CalendarDay selectedDay;
    private float downX;
    private float downY;
    private TypedArray mTypeArray;
    private boolean isCopy;
    private DayDecor mDecors;
    private int halfDayWidth;
    private DayDecor.Style todayStyle;
    private DayDecor.Style selectionStyle;
    private DayDecor.Style normalStyle;
    private DayDecor.Style otherMonthStyle;
    private Rect drawRect;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTypeArray = context.obtainStyledAttributes(attrs, R.styleable.MonthView);

        init(context, mTypeArray);
    }

    private MonthView(Context context, TypedArray typedArray, Void copy) {
        super(context);
        isCopy = true;
        init(context, typedArray);
    }

    private void init(Context context, TypedArray typedArray) {
        Resources resources = context.getResources();
        today = new CalendarDay(Calendar.getInstance());

        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        decorTextColor = resources.getColor(R.color.day_label_decor_text_color);
        mMonthTextColor = typedArray.getColor(R.styleable.MonthView_monthTitleColor, resources.getColor(R.color.month_title_color));
        mWeekColor = typedArray.getColor(R.styleable.MonthView_weekLabelTextColor, resources.getColor(R.color.week_label_text_color));
        normalDayTextColor = typedArray.getColor(R.styleable.MonthView_dayTextColor, resources.getColor(R.color.day_label_text_color));
        todayTextColor = resources.getColor(R.color.today_text_color);
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

//        typedArray.recycle();
        mPadding = getPaddingLeft();
        drawRect = new Rect();
        initStyle();
        initPaint();
        setYearAndMonth(today.getYear(), today.getMonth());
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
    public void setToday(CalendarDay today) {
        this.today = today;
        invalidate();
    }

    private void initStyle() {
        todayStyle = new DayDecor.Style();
        todayStyle.setBold(true);
        todayStyle.setTextColor(todayTextColor);

        selectionStyle = new DayDecor.Style();
        selectionStyle.setPureColorBgShape(DayDecor.Style.CIRCLE);
        selectionStyle.setPureColorBg(mSelectedCircleColor);

        normalStyle = new DayDecor.Style();

        otherMonthStyle = new DayDecor.Style();
        otherMonthStyle.setTextColor(mOtherMonthTextColor);
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
        canvas.drawText(getMonthTitleString(), pos[0], pos[1], mMonthTitlePaint);
    }

    /**
     * draw the day of month
     */
    protected void drawMonthDays(Canvas canvas) {
        int dayTop = SPACE_BETWEEN_WEEK_AND_DAY + MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT;
        int y = (dayRowHeight + normalDayTextSize) / 2 + dayTop;
        int halfDay = halfDayWidth;
        int firstDayOffset = findDayOffset();

        int dayOffset = mShowOtherMonth ? 0 : firstDayOffset;
        int cells = dayOffset + (mShowOtherMonth ? mNumRows * mNumDays : mNumCells);
        for(int i = dayOffset; i < cells; i++) {
            int day = i - firstDayOffset + 1;
            int month = mMonth + 1;
            if(day < 1) {
                CalendarMonth calendarMonth = new CalendarMonth(mYear, mMonth + 1).previous();
                month = calendarMonth.getMonth();
                int preMDays = CalendarUtils.getDaysInMonth(calendarMonth);
                day = preMDays + day;
            } else if(day > mNumCells) {
                month = new CalendarMonth(mYear, mMonth + 1).next().getMonth();
                day = day - mNumCells;
            }
            int dayLeft = dayOffset * halfDay * 2 + mPadding;
            int x = halfDay + dayLeft;

            boolean selected = false;
            if(selectedDay != null && selectedDay.equals(new CalendarDay(mYear, month, day))) { //selected
                selected = true;
            }

            // default color and size
            mDayNumPaint.setColor(decorTextColor);
            mDayNumPaint.setTextSize(normalDayTextSize);
            // set style
            DayDecor.Style style;
            if(month != mMonth + 1) { // other month
                style = otherMonthStyle;
            } else if(mDecors != null && mDecors.getDecorStyle(mYear, mMonth + 1, day) != null) { // exist decor
                style = mDecors.getDecorStyle(mYear, mMonth + 1, day);
            } else if (today.equals(new CalendarDay(mYear, mMonth + 1, day))) { // today
                style = todayStyle;
            } else if (selected) { // today
                style = selectionStyle;
            } else { // normal
                style = normalStyle;
                style.setTextColor(normalDayTextColor);
            }
            style.styledTextPaint(mDayNumPaint);
            // get text height
            String dayStr = String.format(Locale.getDefault(), "%d", day);
            mDayNumPaint.getTextBounds(dayStr, 0, dayStr.length(), drawRect);
            int textHeight = drawRect.height();

            // when selected, background always use selection style,
            // whenever it used be.
            if(selected) {
                style = selectionStyle;
            }
            // draw background
            if(style.isCircleBg()) {
                mDayBgPaint.setColor(style.getPureColorBg());
                canvas.drawCircle(x, y - textHeight / 2, dayCircleRadius, mDayBgPaint);
            } else if(style.isRectBg()) {
                mDayBgPaint.setColor(style.getPureColorBg());
                canvas.drawRect(dayLeft, dayTop, dayLeft + 2 * halfDay, dayTop + dayRowHeight, mDayBgPaint);
            } else if(style.isDrawableBg()) {
                Drawable drawable = style.getDrawableBg();
                int dHeight = drawable.getIntrinsicHeight();
                int dWidth = drawable.getIntrinsicWidth();

                int left, right, top, bottom;
                if(dWidth <= 0) { // fill
                    left = dayLeft;
                    right = dayLeft + 2 * halfDay;
                } else { // remain original size
                    left = x - dWidth / 2;
                    right = x + dWidth / 2;
                }
                if(dHeight <= 0) {
                    top = dayTop;
                    bottom = dayTop + dayRowHeight;
                } else {
                    top = y - textHeight / 2 - dHeight / 2;
                    bottom = y - textHeight / 2 + dHeight / 2;
                }
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(canvas);
            }
            canvas.drawText(dayStr, x, y, mDayNumPaint);

            // goto next day
            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += dayRowHeight;
                dayTop += dayRowHeight;
            }
        }
    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart) - mWeekStart;
    }

    public String getMonthTitleString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long millis = calendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    public int getMonthTitleWidth() {
        return (int) mMonthTitlePaint.measureText(getMonthTitleString());
    }

    private void onDayClick(CalendarDay calendarDay) {
        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
        setSelection(calendarDay);
    }

    public void setSelectionStyle(DayDecor.Style selectionStyle) {
        this.selectionStyle.combine(selectionStyle);
        invalidate();
    }

    /**
     * select specified calendar day.
     * @param calendarDay calendarDay; null to clear selection.
     */
    public void setSelection(CalendarDay calendarDay) {
        if(selectedDay == calendarDay) // not changed
            return;
        if(calendarDay != null && calendarDay.equals(selectedDay)) //same day
            return;

        selectedDay = calendarDay;
        invalidate();
    }

    private CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - padding)) {
            return null;
        }

        float yDayOffset = y - MONTH_HEADER_HEIGHT - WEEK_LABEL_HEIGHT - SPACE_BETWEEN_WEEK_AND_DAY;
        if(yDayOffset < 0)
            return null;

        int yDay = (int) yDayOffset / dayRowHeight;
        int day = 1 + ((int) ((x - padding) / (2 * halfDayWidth)) - findDayOffset()) + yDay * mNumDays;

        if(day < 1) {
            CalendarMonth preM = new CalendarMonth(mYear, mMonth + 1).previous();
            int preD = CalendarUtils.getDaysInMonth(preM) + day;
            return new CalendarDay(preM, preD);
        } else if(day > mNumCells) {
            CalendarMonth nextM = new CalendarMonth(mYear, mMonth + 1).next();
            int nextD = day - mNumCells;
            return new CalendarDay(nextM, nextD);
        } else
            return new CalendarDay(mYear, mMonth + 1, day);
    }

    private boolean isClickMonth(int x, int y) {
        int[] pos = getMonthDrawPoint();
        int centerX = pos[0];
        int bottom = pos[1];
        int extra = 10;
        int width = getMonthTitleWidth();
        Rect monthTitleRect = new Rect(centerX - width / 2 - extra, bottom - MONTH_LABEL_TEXT_SIZE - extra,
                centerX + width / 2 + extra, bottom + extra);

        return monthTitleRect.contains(x, y);
    }

    private int[] getMonthDrawPoint() {
        int x = mWidth / 2;
        int y = MONTH_HEADER_HEIGHT / 2 + (MONTH_LABEL_TEXT_SIZE / 3) + monthLabelOffset;
        return new int[] {x, y};
    }

    protected void initPaint() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        mDayBgPaint = new Paint();
        mDayBgPaint.setAntiAlias(true);
        mDayBgPaint.setColor(mSelectedCircleColor);
        mDayBgPaint.setStyle(Style.FILL);

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

//        Log.d("MonthView", "onMeasure->" + this.getId());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getShouldHeight());
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        halfDayWidth = (mWidth - 2 * mPadding) / (2 * mNumDays);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycle();
    }

    private void recycle() {
        if(mTypeArray != null) {
            mTypeArray.recycle();
            mTypeArray = null;
        }
    }

    protected void setOtherMonthTextColor(@ColorInt int color) {
        mOtherMonthTextColor = color;
        otherMonthStyle.setTextColor(color);
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
                            mOnMonthClicker.onMonthClick(this, new CalendarMonth(mYear, mMonth + 1));
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置当前显示的年和月
     * @param calendarMonth calendarMonth
     */
    public void setYearAndMonth(CalendarMonth calendarMonth) {
        setYearAndMonth(calendarMonth.getYear(), calendarMonth.getMonth());
    }

    /**
     * 设置当前显示的年和月
     * @param year 年
     * @param month 月
     */
    public void setYearAndMonth(int year, int month) {
        if(year == mYear && month == mMonth + 1)
            return;

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

        // we are not sure height will remain unchanged.
        requestLayout();
        invalidate();
    }

    public void setDecors(DayDecor mDecors) {
        this.mDecors = mDecors;
        invalidate();
    }

    public DayDecor getDecors() {
        return mDecors;
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
    }

    public void setDayCircleRadius(int px) {
        dayCircleRadius = px;
    }

    public void setDayRowHeight(int px) {
        dayRowHeight = px;
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
     * MonthView should be this height.
     * @return should height
     */
    public int getShouldHeight() {
        return getHeightWithRows(mNumRows);
    }

    /**
     * the max height MonthView could be.
     * @return max height
     */
    public int getMaxHeight() {
        return getHeightWithRows(DEFAULT_NUM_ROWS);
    }

    private int getHeightWithRows(int rows) {
        return MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT + SPACE_BETWEEN_WEEK_AND_DAY + dayRowHeight * rows;
    }

    // get a copy with same attributes defined in layout.
    protected MonthView staticCopy() {
        if(isCopy)
            // this is a copy, should not make a copy again.
            return null;
        return new MonthView(getContext(), mTypeArray, null);
    }

    public CalendarMonth getCurrentMonth() {
        return new CalendarMonth(mYear, mMonth + 1);
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public OnMonthTitleClickListener getOnMonthTitleClickListener() {
        return mOnMonthClicker;
    }

    public OnDayClickListener getOnDayClickListener() {
        return mOnDayClickListener;
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