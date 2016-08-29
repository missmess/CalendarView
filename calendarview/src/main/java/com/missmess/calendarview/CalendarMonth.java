package com.missmess.calendarview;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * @author wl
 * @since 2016/08/22 12:05
 */
public class CalendarMonth implements Comparable<CalendarMonth> {
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
        month = calendar.get(Calendar.MONTH) + 1;
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

    public CalendarMonth previous() {
        int preY = year;
        int preM = month - 1;
        if(preM == 0) {
            preY = year - 1;
            preM = 12;
        }
        return new CalendarMonth(preY, preM);
    }

    public CalendarMonth next() {
        int nextY = year;
        int nextM = month + 1;
        if(nextM == 13) {
            nextY = year + 1;
            nextM = 1;
        }
        return new CalendarMonth(nextY, nextM);
    }

    @Override
    public int compareTo(@NonNull CalendarMonth another) {
        if(another.getYear() > year || (another.getYear() == year && another.getMonth() > month)) {
            return -1;
        } else if(another.getYear() == year && another.getMonth() == month) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof CalendarMonth) {
            int result = compareTo((CalendarMonth) o);
            return result == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CalendarMonth: { year: " + year + ", month: " + month + " }";
    }
}
