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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

/**
 * MonthView can show a month, with week label in calendar style.
 * others:
 * 1.add day selected listener.
 * 2.decor days as you like.
 * 3.provide a lot of attribute, you can customize your own style.
 * ...
 */
public class MonthView extends View {
    public static final int SELECTION_SINGLE = 0;
    public static final int SELECTION_MULTI = 1;
    public static final int SELECTION_RANGE = 2;
    public static final int SELECTION_NONE = 9;
    private final int DEFAULT_NUM_ROWS = 6;
    protected int dayCircleRadius;
    protected int SPACE_BETWEEN_WEEK_AND_DAY = 0;
    protected int normalDayTextSize;
    protected int WEEK_LABEL_TEXT_SIZE;
    protected int MONTH_HEADER_HEIGHT;
    protected int WEEK_LABEL_HEIGHT;
    protected int MONTH_LABEL_TEXT_SIZE;
    protected int mSelectedCircleColor;
    protected int week_label_padding;
    protected int monthHeaderSizeCache;
    protected int weekLabelOffset = 0;
    protected int monthLabelOffset = 0;
    private int mOtherMonthTextColor;
    private int mDisableTextColor;

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
    private OnSelectionChangeListener mOnSelectionChangeListener;
    private OnMonthTitleClickListener mOnMonthClicker;
    private LinkedHashSet<CalendarDay> selectedDays;
    private float downX;
    private float downY;
    private TypedArray mTypeArray;
    private boolean isCopy;
    private DayDecor mDecors;
    private float halfDayWidth;
    private DayDecor.Style todayStyle;
    private DayDecor.Style selectionStyle;
    private DayDecor.Style normalStyle;
    private DayDecor.Style otherMonthStyle;
    private DayDecor.Style disableStyle;
    private Rect drawRect;
    private CalendarDay leftEdge;
    private CalendarDay rightEdge;
    private boolean mWeekMode;
    private int mWeekIndex = 0;
    private Map<CalendarDay, String> dayLabels;
    private HashSet<CalendarDay> disabledDays;
    private int mSelectionMode = SELECTION_SINGLE;

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
        mDisableTextColor = typedArray.getColor(R.styleable.MonthView_dayDisableTextColor, resources.getColor(R.color.day_disable_text_color));

        normalDayTextSize = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayTextSize, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.MonthView_monthTextSize, resources.getDimensionPixelSize(R.dimen.text_size_month));
        WEEK_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.MonthView_weekLabelTextSize, resources.getDimensionPixelSize(R.dimen.text_size_week));
        MONTH_HEADER_HEIGHT = monthHeaderSizeCache = typedArray.getDimensionPixelOffset(R.styleable.MonthView_monthHeaderHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        dayCircleRadius = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayCircleRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));

        dayRowHeight = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayRowHeight, resources.getDimensionPixelOffset(R.dimen.row_height));
        mShowMonthTitle = typedArray.getBoolean(R.styleable.MonthView_showMonthTitle, true);
        mShowWeekLabel = typedArray.getBoolean(R.styleable.MonthView_showWeekLabel, true);
        mShowWeekDivider = typedArray.getBoolean(R.styleable.MonthView_showWeekDivider, false);
        mWeekMode = typedArray.getBoolean(R.styleable.MonthView_weekMode, false);
        mWeekStart = typedArray.getInt(R.styleable.MonthView_firstDayOfWeek, Calendar.SUNDAY);
        if (mWeekStart > Calendar.SATURDAY || mWeekStart < Calendar.SUNDAY)
            throw new IllegalStateException("start day of week can only be values 1 ~7");

        week_label_padding = typedArray.getDimensionPixelSize(R.styleable.MonthView_weekLabelPadding, resources.getDimensionPixelSize(R.dimen.week_label_padding));
        if (!mShowMonthTitle) {
            MONTH_HEADER_HEIGHT = 0;
        }
        if (!mShowWeekLabel) {
            WEEK_LABEL_HEIGHT = 0;
        } else {
            WEEK_LABEL_HEIGHT = WEEK_LABEL_TEXT_SIZE + week_label_padding;
        }

        //        typedArray.recycle();
        mPadding = getPaddingLeft();
        drawRect = new Rect();
        initStyle();
        initPaint();
        setYearAndMonth(today.getYear(), today.getMonth());

        leftEdge = new CalendarDay(1900, 2, 1);
        rightEdge = new CalendarDay(2049, 12, 31);
        disabledDays = new HashSet<>();
        dayLabels = new HashMap<>();
        selectedDays = new LinkedHashSet<>();
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 设置当前时间
     *
     * @param today 当前时间
     */
    public void setToday(@NonNull CalendarDay today) {
        this.today = today;
        invalidate();
    }

    public CalendarDay getToday() {
        return today;
    }

    public void setDayLabel(CalendarDay day, String label) {
        dayLabels.put(day, label);
    }

    public String getDayLabel(CalendarDay day) {
        return dayLabels.get(day);
    }

    private void initStyle() {
        todayStyle = new DayDecor.Style();
        todayStyle.setBold(true);
        todayStyle.setTextColor(todayTextColor);

        selectionStyle = new DayDecor.Style();
        selectionStyle.setBgShape(DayDecor.Style.CIRCLE);
        selectionStyle.setPureColorBg(mSelectedCircleColor);
        selectionStyle.setTextColor(decorTextColor);

        normalStyle = new DayDecor.Style();
        normalStyle.setTextColor(normalDayTextColor);

        otherMonthStyle = new DayDecor.Style();
        otherMonthStyle.setTextColor(mOtherMonthTextColor);

        disableStyle = new DayDecor.Style();
        disableStyle.setTextColor(mDisableTextColor);
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
        mDayBgPaint.setStrokeWidth(3);
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

    void drawWeekLabels(Canvas canvas) {
        int y = MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT / 2 + WEEK_LABEL_TEXT_SIZE / 2 + weekLabelOffset;
        float dayWidthHalf = halfDayWidth;

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            if (calendarDay == 0)
                calendarDay = mNumDays;
            float x = (2 * i + 1) * dayWidthHalf + mPadding;

            final Locale locale = getResources().getConfiguration().locale;
            SimpleDateFormat mDayOfWeekFormatter = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, locale);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDayOfWeekFormatter.format(cal.getTime()), x, y, mWeekLabelPaint);
        }

        if (mShowWeekDivider) {
            //draw divider under week label
            int yLine = MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT + weekLabelOffset;
            canvas.drawLine(mPadding, yLine - 1, mWidth - mPadding, yLine, mWeekLabelPaint);
        }
    }

    void drawMonthTitle(Canvas canvas) {
        //        Log.e("MonthView", "drawMonthTitle");
        int[] pos = getMonthDrawPoint();
        //        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        //        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(getMonthTitleString(), pos[0], pos[1], mMonthTitlePaint);
    }

    boolean isOtherMonthShowing() {
        return mWeekMode || mShowOtherMonth;
    }

    /**
     * draw the day of month
     */
    protected void drawMonthDays(Canvas canvas) {
        int dayTop = SPACE_BETWEEN_WEEK_AND_DAY + MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT;
        float halfDay = halfDayWidth;
        int firstDayOffset = findDayOffset();
        boolean weekMode = mWeekMode;

        // in a line, the offset with left of current day
        int offsetInLine = 0;
        // the offset with top left of current day
        int startOffset = weekMode ? getWeekIndex() * mNumDays : 0;
        // times we loop
        int cells = weekMode ? mNumDays : mNumRows * mNumDays;
        // loop to draw
        for (int i = startOffset; i < startOffset + cells; i++) {
            int day = i - firstDayOffset + 1;
            CalendarMonth currentMonth = getCurrentMonth();
            // if true, current drawing day is in other month
            boolean otherMonth = false;
            if (day < 1) {
                if (!isOtherMonthShowing()) {
                    offsetInLine++;
                    continue;
                }

                otherMonth = true;
                currentMonth = currentMonth.previous();
                int preMDays = CalendarUtils.getDaysInMonth(currentMonth);
                day = preMDays + day;
            } else if (day > mNumCells) {
                if (!isOtherMonthShowing()) {
                    offsetInLine++;
                    continue;
                }

                otherMonth = true;
                currentMonth = currentMonth.next();
                day = day - mNumCells;
            }
            // x position
            CalendarDay currentDay = new CalendarDay(currentMonth, day);
            // Now edge day is always the first or end day of the current month.
            if ((leftEdge != null && currentDay.compareTo(leftEdge) < 0)
                    || (rightEdge != null && currentDay.compareTo(rightEdge) > 0)) {
                offsetInLine++;
                // edge以外
                continue;
            }
            float dayLeft = offsetInLine * halfDay * 2 + mPadding;
            float x = halfDay + dayLeft;
            // if true, current drawing day is selected
            boolean selected = false;
            if (selectedDays.contains(currentDay)) { //selected
                selected = true;
            }

            boolean disabled = disabledDays.contains(currentDay);

            DayDecor.Style decoration = null;
            if (mDecors != null) {
                decoration = mDecors.getDecorStyle(currentDay);
            }
            // default color and size
            mDayNumPaint.setColor(normalDayTextColor);
            mDayNumPaint.setTextSize(normalDayTextSize);

            // ==================================
            // cover order for drawing:
            // 1. text: other month > disabled > selection > decorator > today > normal
            // 2. bg: disabled : none; otherwise is (selection + decorator), selection in foreground
            // ==================================
            DayDecor.Style style;
            if (!weekMode && otherMonth) { // other month, if in week mode, other month is unnecessary
                style = otherMonthStyle;
            } else if (disabled) {
                style = disableStyle;
            } else if (selected) { // select
                style = selectionStyle;
            } else if (decoration != null) { // exist decor
                if (decoration.getTextColor() == 0) {
                    int bgShape = decoration.getBgShape();
                    if (bgShape == DayDecor.Style.CIRCLE_STROKE || bgShape == DayDecor.Style.DOT) {
                        decoration.setTextColor(normalDayTextColor);
                    } else {
                        decoration.setTextColor(decorTextColor);
                    }
                }
                style = decoration;
            } else if (today.equals(currentDay)) { // today
                style = todayStyle;
            } else { // normal
                style = normalStyle;
            }
            style.assignStyleToPaint(mDayNumPaint);
            // get text height
            String dayStr = dayLabels.get(currentDay);
            if (TextUtils.isEmpty(dayStr))
                dayStr = String.format(Locale.getDefault(), "%d", day);
            mDayNumPaint.getTextBounds(dayStr, 0, dayStr.length(), drawRect);
            int textHeight = drawRect.height();
            float y = (dayRowHeight + mDayNumPaint.getTextSize()) / 2 + dayTop;

            if (!disabled) {
                // draw background
                if (decoration != null) {
                    drawDayBg(canvas, decoration, x, y, textHeight, dayTop, dayLeft);
                }
                if (selected) {
                    drawDayBg(canvas, selectionStyle, x, y, textHeight, dayTop, dayLeft);
                }
            }

            // draw text
            canvas.drawText(dayStr, x, y, mDayNumPaint);
            // goto next day
            offsetInLine++;
            if (offsetInLine == mNumDays) {
                offsetInLine = 0;
                dayTop += dayRowHeight;
            }
        }
    }

    private void drawDayBg(Canvas canvas, DayDecor.Style style, float x, float y, int textHeight,
                           int dayTop, float dayLeft) {
        float halfDay = halfDayWidth;
        switch (style.getBgShape()) {
            case DayDecor.Style.CIRCLE:
                mDayBgPaint.setStyle(Style.FILL);
                mDayBgPaint.setColor(style.getPureColorBg());
                canvas.drawCircle(x, y - textHeight / 2, dayCircleRadius, mDayBgPaint);
                break;
            case DayDecor.Style.RECTANGLE:
                mDayBgPaint.setStyle(Style.FILL);
                mDayBgPaint.setColor(style.getPureColorBg());
                canvas.drawRect(dayLeft, dayTop, dayLeft + 2 * halfDay, dayTop + dayRowHeight, mDayBgPaint);
                break;
            case DayDecor.Style.CIRCLE_STROKE:
                mDayBgPaint.setStyle(Style.STROKE);
                mDayBgPaint.setColor(style.getPureColorBg());
                canvas.drawCircle(x, y - textHeight / 2, dayCircleRadius, mDayBgPaint);
                break;
            case DayDecor.Style.DOT:
                mDayBgPaint.setStyle(Style.FILL);
                mDayBgPaint.setColor(style.getPureColorBg());
                int dot_r = getResources().getDimensionPixelSize(R.dimen.dot_radius);
                canvas.drawCircle(x, y + dot_r * 3, dot_r, mDayBgPaint);
                break;
            case DayDecor.Style.DRAWABLE: {
                Drawable drawable = style.getDrawableBg();
                int dHeight = drawable.getIntrinsicHeight();
                int dWidth = drawable.getIntrinsicWidth();

                float left, right, top, bottom;
                if (dWidth <= 0) { // fill
                    left = dayLeft;
                    right = dayLeft + 2 * halfDay;
                } else { // remain original size
                    left = x - dWidth / 2;
                    right = x + dWidth / 2;
                }
                if (dHeight <= 0) {
                    top = dayTop;
                    bottom = dayTop + dayRowHeight;
                } else {
                    top = y - textHeight / 2 - dHeight / 2;
                    bottom = y - textHeight / 2 + dHeight / 2;
                }
                drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);
                drawable.draw(canvas);
                break;
            }
        }
    }

    /**
     * The first day of current month offset x cells.
     *
     * @return the x
     */
    protected int findDayOffset() {
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
        selectActual(calendarDay, true);
    }

    public void setSelectionStyle(DayDecor.Style selectionStyle) {
        this.selectionStyle.combine(selectionStyle);
        invalidate();
    }

    /**
     * Be one of the {@link #SELECTION_SINGLE}, {@link #SELECTION_MULTI}, {@link #SELECTION_RANGE},
     * {@link #SELECTION_NONE}
     * @param selectionMode mode
     */
    public void setSelectionMode(int selectionMode) {
        if (mSelectionMode == selectionMode)
            return;

        this.mSelectionMode = selectionMode;
        setSelection(null);
    }

    public int getSelectionMode() {
        return mSelectionMode;
    }

    /**
     * Select a specified calendar day in this month. Selection day should always be visible, no
     * matter month mode or week mode.
     *
     * @param selection a day; set null to clear selection.
     * @return success selected
     */
    public boolean setSelection(@Nullable CalendarDay selection) {
        if (selection != null && leftEdge != null && selection.compareTo(leftEdge) < 0)
            return false;
        if (selection != null && leftEdge != null && selection.compareTo(rightEdge) > 0)
            return false;

        if (selection != null) {
            if (mWeekMode) {
                // selection can not out range of current week
                if (!isDayInWeek(selection)) {
                    return false;
                }
            } else {
                // selection can not out range of current month
                int com = getDayType(selection);
                if (com == -2 || com == 2) {
                    return false;
                }
            }

            // if disabled
            if (disabledDays.contains(selection))
                return false;
        }

        return selectActual(selection, false);
    }

    // set selection atomically no matter what limit
    void setSelectionAtom(@Nullable CalendarDay[] selections) {
        selectedDays.clear();
        if (selections != null) {
            selectedDays.addAll(Arrays.asList(selections));
        }
        invalidate();
    }

    private boolean selectActual(CalendarDay selection, boolean byUser) {
        int mode = mSelectionMode;
        // old selection array
        CalendarDay[] oldArray = new CalendarDay[selectedDays.size()];
        selectedDays.toArray(oldArray);

        boolean modified = false;
        if (selection == null || mode == SELECTION_NONE) { // if null, clear selections
            if (!selectedDays.isEmpty()) {
                selectedDays.clear();
                modified = true;
            }
        } else {
            boolean exist = selectedDays.contains(selection);
            switch (mode) {
                case SELECTION_SINGLE:
                    if (!exist) { // not exist, add it
                        selectedDays.clear();
                        modified = selectedDays.add(selection);
                    }
                    break;
                case SELECTION_MULTI:
                    if (exist) {
                        modified = selectedDays.remove(selection);
                    } else {
                        modified = selectedDays.add(selection);
                    }
                    break;
                case SELECTION_RANGE:
                    int size = selectedDays.size();
                    if (size == 0) {
                        modified = selectedDays.add(selection);
                    } else if (size == 1) {
                        if (exist) {
                            modified = selectedDays.remove(selection);
                        } else {
                            CalendarDay one = selectedDays.iterator().next();
                            int com = one.compareTo(selection);
                            CalendarDay start = com < 0 ? one.next() : selection;
                            CalendarDay end = com < 0 ? selection : one.previous();
                            // add range selections from the one to selection.
                            for (CalendarDay day = start; day.compareTo(end) <= 0; day = day.next()) {
                                selectedDays.add(day);
                            }
                            modified = true;
                        }
                    } else {
                        selectedDays.clear();
                        selectedDays.add(selection);
                        modified = true;
                    }
                    break;
            }
        }

        if (modified) {
            invalidate();
            CalendarDay[] newArray = new CalendarDay[selectedDays.size()];
            selectedDays.toArray(newArray);
            if (mOnSelectionChangeListener != null) {
                mOnSelectionChangeListener.onSelectionChanged(this, newArray, oldArray, selection, byUser);
            }
            return true;
        }

        return false;
    }

    public CalendarDay[] getSelection() {
        CalendarDay[] a = new CalendarDay[selectedDays.size()];
        return selectedDays.toArray(a);
    }

    public void setDayDisable(@NonNull CalendarDay disable) {
        boolean added = disabledDays.add(disable);
        if (!added)
            return;

        // clear selection
        selectedDays.remove(disable);

        // remove decor
        if (mDecors != null)
            mDecors.remove(disable);

        invalidate();
    }

    public boolean isDayDisabled(@NonNull CalendarDay day) {
        return disabledDays.contains(day);
    }

    public void clearDisable() {
        disabledDays.clear();
        invalidate();
    }

    public void setStartDayOfWeek(@IntRange(from = Calendar.SUNDAY, to = Calendar.SATURDAY) int dayOfWeek) {
        if (dayOfWeek > Calendar.SATURDAY || dayOfWeek < Calendar.SUNDAY)
            throw new IllegalStateException("start day of week can only be values 1 ~7");

        if (dayOfWeek == mWeekStart)
            return;

        this.mWeekStart = dayOfWeek;
        invalidate();
    }

    public int getStartDayOfWeek() {
        return mWeekStart;
    }

    protected void leftEdgeDay(CalendarDay lEdge) {
        leftEdge = lEdge;
    }

    protected void rightEdgeDay(CalendarDay rEdge) {
        rightEdge = rEdge;
    }

    private CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - padding)) {
            return null;
        }

        float yDayOffset = y - MONTH_HEADER_HEIGHT - WEEK_LABEL_HEIGHT - SPACE_BETWEEN_WEEK_AND_DAY;
        if (yDayOffset < 0)
            return null;

        int yDay = (int) yDayOffset / dayRowHeight;
        int day = 1 + ((int) ((x - padding) / (2 * halfDayWidth)) - findDayOffset()) + yDay * mNumDays;

        if (mWeekMode) {
            day += getWeekIndex() * mNumDays;
        }

        if (day < 1) {
            if (!isOtherMonthShowing()) {
                return null;
            } else {
                CalendarMonth preM = getCurrentMonth().previous();
                int preD = CalendarUtils.getDaysInMonth(preM) + day;
                return new CalendarDay(preM, preD);
            }
        } else if (day > mNumCells) {
            if (!isOtherMonthShowing()) {
                return null;
            } else {
                CalendarMonth nextM = getCurrentMonth().next();
                int nextD = day - mNumCells;
                return new CalendarDay(nextM, nextD);
            }
        } else
            return new CalendarDay(getCurrentMonth(), day);
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
        return new int[]{x, y};
    }

    protected void onDraw(Canvas canvas) {
        //        Log.d("MonthView", "onDraw");

        if (mShowMonthTitle) {
            drawMonthTitle(canvas);
        }
        if (mShowWeekLabel) {
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
        halfDayWidth = (float) (mWidth - 2 * mPadding) / (2 * mNumDays);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycle();
    }

    private void recycle() {
        if (mTypeArray != null) {
            mTypeArray.recycle();
            mTypeArray = null;
        }
    }

    protected void setOtherMonthTextColor(@ColorInt int color) {
        if (color == mOtherMonthTextColor)
            return;

        mOtherMonthTextColor = color;
        otherMonthStyle.setTextColor(color);
        invalidate();
    }

    public void setDisableTextColor(@ColorInt int color) {
        if (color == mDisableTextColor)
            return;

        mDisableTextColor = color;
        disableStyle.setTextColor(color);
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
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
                        // if this location is out of range.
                        if ((leftEdge != null && calendarDay.compareTo(leftEdge) < 0)
                                || (rightEdge != null && calendarDay.compareTo(rightEdge) > 0))
                            break;
                        // if disabled
                        if (disabledDays.contains(calendarDay))
                            break;

                        // else
                        onDayClick(calendarDay);
                    } else if (isClickMonth((int) x, (int) y)) { // clicked month title
                        // month title clicked
                        if (mOnMonthClicker != null) {
                            mOnMonthClicker.onMonthClick(this, getCurrentMonth());
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置当前显示的年和月
     * @param year  年
     * @param month 月
     */
    public void setYearAndMonth(int year, int month) {
        setYearAndMonth(new CalendarMonth(year, month));
    }

    /**
     * 设置当前显示的年和月
     *
     * @param calendarMonth calendarMonth
     */
    public void setYearAndMonth(@NonNull CalendarMonth calendarMonth) {
        if (calendarMonth.getYear() == mYear && calendarMonth.getMonth() == mMonth + 1)
            return;
        if ((leftEdge != null && calendarMonth.compareTo(leftEdge.getCalendarMonth()) < 0)
                || (rightEdge != null && calendarMonth.compareTo(rightEdge.getCalendarMonth()) > 0))
            return;

        mYear = calendarMonth.getYear();
        mMonth = calendarMonth.getMonth() - 1;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, mMonth);
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = calendar.get(Calendar.DAY_OF_WEEK);

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);

        mNumRows = calculateNumRows();

        if (ViewCompat.isLaidOut(this)) {
            // we are not sure height will remain unchanged.
            requestLayout();
            invalidate();
        }
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
        if (!mShowMonthTitle) {
            MONTH_HEADER_HEIGHT = 0;
        } else {
            MONTH_HEADER_HEIGHT = monthHeaderSizeCache;
        }
    }

    public void showWeekLabel(boolean show) {
        this.mShowWeekLabel = show;
        if (!mShowWeekLabel) {
            WEEK_LABEL_HEIGHT = 0;
        } else {
            WEEK_LABEL_HEIGHT = WEEK_LABEL_TEXT_SIZE + week_label_padding;
        }
    }

    void setNormalDayTextColor(@ColorInt int color) {
        normalDayTextColor = color;
        normalStyle.setTextColor(color);
    }

    void setNormalDayTextSize(int px) {
        normalDayTextSize = px;
    }

    protected void setShowOtherMonth(boolean show) {
        if (mShowOtherMonth == show)
            return;

        mShowOtherMonth = show;
        invalidate();
    }

    void setDayCircleRadius(int px) {
        dayCircleRadius = px;
    }

    void setDayRowHeight(int px) {
        dayRowHeight = px;
    }

    protected void setWeekLabelOffset(int weekLabelOffset) {
        this.weekLabelOffset = weekLabelOffset;
        invalidate();
    }

    protected void setMonthLabelOffset(int monthLabelOffset) {
        this.monthLabelOffset = monthLabelOffset;
        invalidate();
    }

    public void showWeekMode() {
        if (mWeekMode)
            return;

        this.mWeekMode = true;
        invalidate();
        requestLayout();
    }

    /**
     * Week index indicate the showing week rows when in week mode, or the destination week row when
     * use {@link ScrollingMonthPagerBehavior} to transit MonthMode to WeekMode.
     * @param weekIndex week row index, should be in range of 0 ~ ({@link #getWeekRows()} - 1).
     * @return true - success set.
     */
    public boolean setWeekIndex(int weekIndex) {
        if (mWeekIndex == weekIndex)
            return false;

        if (weekIndex < 0 || weekIndex >= mNumRows)
            return false;

        this.mWeekIndex = weekIndex;
        if (mWeekMode) {
            invalidate();
        }
        return true;
    }

    public int getWeekIndex() {
        return mWeekIndex;
    }

    public boolean isWeekMode() {
        return mWeekMode;
    }

    boolean isDayInWeek(CalendarDay day) {
        if (!mWeekMode)
            throw new IllegalStateException("do not call this when not in week mode");

        int dayOffset = findDayOffset();
        int start = mWeekIndex * mNumDays - dayOffset;
        CalendarDay startDay = CalendarUtils.offsetDay(new CalendarDay(getCurrentMonth(), 1), start);
        if (day.compareTo(startDay) < 0)
            return false;

        CalendarDay endDay = CalendarUtils.offsetDay(startDay, mNumDays);
        if (day.compareTo(endDay) > 0)
            return false;

        return true;
    }

    public void showMonthMode() {
        if (!mWeekMode)
            return;

        this.mWeekMode = false;
        invalidate();
        requestLayout();
    }

    public boolean isMonthMode() {
        return !mWeekMode;
    }

//    /**
//     * Line index of selection showing.
//     *
//     * @return return -1 for no selection or selection is invisible in current month.
//     */
//    public int getSelectionLineIndex() {
//        if (!selectedDays.isEmpty()) {
//            int type = getSelectionType();
//            switch (type) {
//                case 0:
//                    CalendarDay day = selectedDays.iterator().next();
//                    return (findDayOffset() + day.getDay() - 1) / mNumDays;
//                case 1:
//                    return mNumRows - 1;
//                case 2:
//                    return -1;
//                case -1:
//                    return 0;
//                case -2:
//                    return -1;
//            }
//        }
//
//        return -1;
//    }

//    /**
//     * Selection day type.
//     *
//     * @return -3 - no selection, others see {@link #getDayType(CalendarDay)}
//     */
//    int getSelectionType() {
//        if (selectedDays.isEmpty())
//            return -3;
//
//        return getDayType(selectedDays.iterator().next());
//    }

    /**
     * Line index of calendar day in current month, return -1 if not in current month.
     * @return index or -1.
     */
    public int getLineIndex(@NonNull CalendarDay day) {
        int type = getDayType(day);
        switch (type) {
            case 0:
                return (findDayOffset() + day.getDay() - 1) / mNumDays;
            default:
                return -1;
        }
    }

    /**
     * A type of day relative to this month.
     *
     * @return <p>0 - if day is in current month</p>
     * <p>-1 - if day is in previous month and visible now</p>
     * <p>-2 - if day is in previous month but not visible</p>
     * <p>1 - if day is in next month and visible now</p>
     * <p>2 - if day is in next month but not visible</p>
     */
    public int getDayType(@NonNull CalendarDay day) {
        int com = day.getCalendarMonth().compareTo(getCurrentMonth());
        if (com == 0) {
            // current month
            return 0;
        }

        int dayOffset = findDayOffset();
        if (com < 0) {
            CalendarDay first = new CalendarDay(getCurrentMonth(), 1);
            CalendarDay otherMonthStartDay = CalendarUtils.offsetDay(first, -dayOffset);
            if (isOtherMonthShowing() && day.compareTo(otherMonthStartDay) >= 0) {
                if (leftEdge != null && day.compareTo(leftEdge) < 0)
                    return -2;
                else
                    // select day is in previous month and visible
                    return -1;
            } else {
                return -2;
            }
        } else {
            CalendarDay last = new CalendarDay(getCurrentMonth(), mNumCells);
            CalendarDay otherMonthEndDay = CalendarUtils.offsetDay(last, mNumRows * mNumDays - dayOffset - mNumCells);
            if (isOtherMonthShowing() && day.compareTo(otherMonthEndDay) <= 0) {
                if (rightEdge != null && day.compareTo(rightEdge) > 0)
                    return 2;
                else
                    // select day is in next month and visible
                    return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * return the number of week of this month.
     *
     * @return week number rows.
     */
    int getWeekRows() {
        return mNumRows;
    }

    /**
     * Height of MonthView should be.
     *
     * @return should height
     */
    public int getShouldHeight() {
        if(mWeekMode) {
            return getHeightWithRows(1);
        } else {
            return getHeightWithRows(mNumRows);
        }
    }

    /**
     * The visible day range of current month, including other month if other-month enabled.
     * @return array with 2 elements, first visible day and last visible day.
     */
    public CalendarDay[] getShowingDayRange() {
        CalendarDay start, end;
        CalendarMonth currentMonth = getCurrentMonth();

        int firstDayOffset = findDayOffset();
        // the offset with top left of current day
        int startOffset = mWeekMode ? getWeekIndex() * mNumDays : 0;
        // times we loop
        int cells = mWeekMode ? mNumDays : mNumRows * mNumDays;

        int startDay = startOffset - firstDayOffset + 1;
        if (startDay < 1) {
            if (!isOtherMonthShowing()) {
                start = new CalendarDay(currentMonth, 1);
            } else {
                CalendarMonth previousMonth = currentMonth.previous();
                int day = CalendarUtils.getDaysInMonth(previousMonth) + startDay;
                start = new CalendarDay(previousMonth, day);
            }
        } else {
            start = new CalendarDay(currentMonth, startDay);
        }

        int endDay = startDay + cells - 1;
        if (endDay > mNumCells) {
            if (!isOtherMonthShowing()) {
                end = new CalendarDay(currentMonth, mNumCells);
            } else {
                int day = endDay - mNumCells;
                end = new CalendarDay(currentMonth.next(), day);
            }
        } else {
            end = new CalendarDay(currentMonth, endDay);
        }

        if (leftEdge != null && start.compareTo(leftEdge) < 0)
            start = leftEdge;

        if (rightEdge != null && end.compareTo(rightEdge) > 0)
            end = rightEdge;

        return new CalendarDay[] {start, end};
    }

    /**
     * The month day range of current month.
     * @return first day of month and last day of month.
     */
    public CalendarDay[] getMonthDayRange() {
        CalendarMonth currentMonth = getCurrentMonth();
        CalendarDay start = new CalendarDay(currentMonth, 1);
        CalendarDay end = new CalendarDay(currentMonth, mNumCells);
        return new CalendarDay[] {start, end};
    }

    /**
     * the max height MonthView could be.
     *
     * @return max height
     */
    public int getMaxHeight() {
        return getHeightWithRows(DEFAULT_NUM_ROWS);
    }

    public int getHeightWithRows(int rows) {
        return MONTH_HEADER_HEIGHT + WEEK_LABEL_HEIGHT + SPACE_BETWEEN_WEEK_AND_DAY + dayRowHeight * rows;
    }

    /**
     * Height of one day row.
     * @return height
     */
    public int getDayRowHeight() {
        return dayRowHeight;
    }

    // get a copy with same attributes defined in layout.
    protected MonthView staticCopy() {
        if (isCopy)
            // this is a copy, should not make a copy again.
            return null;
        return new MonthView(getContext(), mTypeArray, null);
    }

    public CalendarMonth getCurrentMonth() {
        return new CalendarMonth(mYear, mMonth + 1);
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        mOnSelectionChangeListener = listener;
    }

    public OnMonthTitleClickListener getOnMonthTitleClickListener() {
        return mOnMonthClicker;
    }

    public OnSelectionChangeListener getOnSelectionChangeListener() {
        return mOnSelectionChangeListener;
    }

    public void setOnMonthTitleClickListener(OnMonthTitleClickListener onMonthTitleClickListener) {
        this.mOnMonthClicker = onMonthTitleClickListener;
    }

    public interface OnSelectionChangeListener {
        /**
         * Selection has changed on month view
         *
         * @param monthView monthView
         * @param now       now selections array, not be null
         * @param old       old selections array, not be null
         * @param selection the day which user is interacting with. If click, the day clicked; if
         *                  call {@link #setSelection(CalendarDay)}, the day user call, may be null.
         * @param byUser    true - selection changed by user click, false - selection changed by
         *                  call {@link #setSelection(CalendarDay)}.
         */
        void onSelectionChanged(MonthView monthView, CalendarDay[] now, CalendarDay[] old,
                                @Nullable CalendarDay selection, boolean byUser);
    }

    public interface OnMonthTitleClickListener {
        void onMonthClick(MonthView monthView, CalendarMonth calendarMonth);
    }
}