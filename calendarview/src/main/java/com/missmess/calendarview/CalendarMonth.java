package com.missmess.calendarview;

import java.util.Calendar;

/**
 * @author wl
 * @since 2016/08/22 12:05
 */
public class CalendarMonth {
    int month;
    int year;

    public CalendarMonth() {
        this(Calendar.getInstance());
    }

    public CalendarMonth(int year, int month) {
        setMonth(year, month);
    }

    public CalendarMonth(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
    }

    public void setMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "{ year: " + year + ", month: " + month + " }";
    }
}
