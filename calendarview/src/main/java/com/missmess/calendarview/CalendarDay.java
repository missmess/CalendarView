package com.missmess.calendarview;

import java.util.Calendar;

/**
 * @author wl
 * @since 2016/08/22 12:04
 */
public class CalendarDay {
    int month;
    int year;
    int day;

    public CalendarDay() {
        this(Calendar.getInstance());
    }

    public CalendarDay(int year, int month, int day) {
        setDay(year, month, day);
    }

    public CalendarDay(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
    }

    public void setDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "{ year: " + year + ", month: " + month + ", day: " + day + " }";
    }
}
