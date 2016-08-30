package com.missmess.calendarview;

import android.support.annotation.ColorInt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wl
 * @since 2016/08/30 11:33
 */
public class DayDecor {
    private Map<CalendarDay, Integer> decorMaps;

    public DayDecor() {
        decorMaps = new HashMap<>();
    }

    public void putOne(CalendarDay calendarDay, @ColorInt int color) {
        decorMaps.put(calendarDay, color);
    }

    public void putAll(Map<CalendarDay, Integer> decors) {
        decorMaps.putAll(decors);
    }

    public @ColorInt Integer getDecorColor(int year, int month, int day) {
        return getDecorColor(new CalendarDay(year, month, day));
    }

    public @ColorInt Integer getDecorColor(CalendarDay calendarDay) {
        return decorMaps.get(calendarDay);
    }

    public void clear() {
        decorMaps.clear();
    }

    public Map<CalendarDay, Integer> getInnerMap() {
        return decorMaps;
    }
}
