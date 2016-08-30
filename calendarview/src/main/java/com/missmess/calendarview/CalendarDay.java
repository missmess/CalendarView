package com.missmess.calendarview;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * @author wl
 * @since 2016/08/22 12:04
 */
public class CalendarDay implements Comparable<CalendarDay> {
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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        setDay(year, month, day);
    }

    public void setDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        if(year < 1900) {
            throw new IllegalArgumentException("year can not small than 1900");
        }
        int maxDays = CalendarUtils.getDaysInMonth(month - 1, year);
        if(day > maxDays) {
            throw new IllegalArgumentException("this date doesn't exist");
        }
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

    public Date getDate() {
        return new Date(year - 1900, month - 1, day);
    }

    @Override
    public int compareTo(@NonNull CalendarDay another) {
        if(another.getYear() > year || (another.getYear() == year && another.getMonth() > month)
                || (another.getYear() == year && another.getMonth() == month && another.getDay() > day)) {
            return -1;
        } else if(another.getYear() == year && another.getMonth() == month && another.getDay() == day) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof CalendarDay) {
            int result = compareTo((CalendarDay) o);
            return result == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CalendarDay: { " + year + "-" + month + "-" + day + " }";
    }
}
