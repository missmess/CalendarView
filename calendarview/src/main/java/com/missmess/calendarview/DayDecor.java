package com.missmess.calendarview;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wl
 * @since 2016/08/30 11:33
 */
public class DayDecor {
    private Map<CalendarDay, Style> decorMaps;

    public DayDecor() {
        decorMaps = new HashMap<>();
    }

    /**
     * add or replace a decor with circle bg mapping to a CalendarDay.
     * @param calendarDay calendarDay
     * @param circleBgColor circle bg color
     */
    public void putOne(CalendarDay calendarDay, @ColorInt int circleBgColor) {
        putOne(calendarDay, circleBgColor, Style.CIRCLE);
    }

    /**
     * add or replace a decor with pure color bg mapping to a CalendarDay.
     * @param calendarDay calendarDay
     * @param bgColor color
     * @param shape {@link Style#CIRCLE circle}, {@link Style#RECTANGLE rectangle}
     */
    public void putOne(CalendarDay calendarDay, @ColorInt int bgColor, int shape) {
        Style style = new Style();
        style.setPureColorBg(bgColor);
        style.setBgShape(shape);
        putOne(calendarDay, style);
    }

    /**
     * add or replace a decor with specified drawable bg mapping to a CalendarDay.
     * @param calendarDay calendarDay
     * @param drawable drawable
     */
    public void putOne(CalendarDay calendarDay, Drawable drawable) {
        Style style = new Style();
        style.setDrawableBg(drawable);
        putOne(calendarDay, style);
    }

    /**
     * add or replace a decor with specified style bg mapping to a CalendarDay.
     * @param calendarDay calendarDay
     * @param style Style
     */
    public void putOne(CalendarDay calendarDay, Style style) {
        decorMaps.put(calendarDay, style);
    }

    public void putAll(Map<CalendarDay, Style> decors) {
        decorMaps.putAll(decors);
    }

    /**
     * get decor style
     * @param year year
     * @param month month
     * @param day day
     * @return {@link Style}
     */
    public Style getDecorStyle(int year, int month, int day) {
        return getDecorStyle(new CalendarDay(year, month, day));
    }

    public Style getDecorStyle(CalendarDay calendarDay) {
        return decorMaps.get(calendarDay);
    }

    public void remove(CalendarDay calendarDay) {
        decorMaps.remove(calendarDay);
    }

    /**
     * clear all decors
     */
    public void clear() {
        decorMaps.clear();
    }

    /**
     * get style maps
     * @return map
     * @hide
     */
    public Map<CalendarDay, Style> getInnerMap() {
        return decorMaps;
    }

    public static class Style {
        public static final int DRAWABLE = 0;
        /**
         * circle shape of pure color bg
         */
        public static final int CIRCLE = 1;
        /**
         * rectangle shape of pure color bg
         */
        public static final int RECTANGLE = 2;
        /**
         * circle stroke color bg
         */
        public static final int CIRCLE_STROKE = 3;
        public static final int DOT = 5;

        // attributes
        // text
        private boolean isBold = false;
        private boolean isItalic = false;
        private boolean underline = false;
        private boolean strikeThrough = false;
        private @ColorInt int textColor = 0;
        private int textSize = 0;
        // bg
        private int bgShape = DRAWABLE;
        private @ColorInt int pureColorBg = 0;
        private Drawable drawableBg = null;
        
        public Style() {}
        
        public Style(Style src) {
            setBold(src.isBold());
            setItalic(src.isItalic());
            setUnderline(src.isUnderline());
            setStrikeThrough(src.isStrikeThrough());
            setTextColor(src.getTextColor());
            setTextSize(src.getTextSize());
            setBgShape(src.getBgShape());
            setPureColorBg(src.getPureColorBg());
            setDrawableBg(src.getDrawableBg());
        }

        /**
         * add text attributes to specified paint.
         */
        void assignStyleToPaint(Paint paint) {
            paint.setFakeBoldText(isBold);
            paint.setTextSkewX(isItalic ? -0.25f : 0f);
            paint.setUnderlineText(underline);
            paint.setStrikeThruText(strikeThrough);
            if(textColor != 0)
                paint.setColor(textColor);
            if(textSize != 0)
                paint.setTextSize(textSize);
        }

        /**
         * combine a other Style with this. If an attribute in other is not configured, use attr of this,
         * if configured, use other's instead.
         * @param other a Style combine with this
         */
        void combine(Style other) {
            setBold(other.isBold);
            setItalic(other.isItalic);
            setUnderline(other.isUnderline());
            setStrikeThrough(other.isStrikeThrough());
            if(other.getTextColor() != 0)
                setTextColor(other.getTextColor());
            if(other.getTextSize() != 0)
                setTextSize(other.getTextSize());
            if(other.getBgShape() != 0)
                setBgShape(other.getBgShape());
            if(other.getPureColorBg() != 0)
                setPureColorBg(other.getPureColorBg());
            if(other.getDrawableBg() != null)
                setDrawableBg(other.getDrawableBg());
        }

        ///////////////////////////////////////////////////////////////////////////
        // SETTER & GETTER
        ///////////////////////////////////////////////////////////////////////////
        public void setBold(boolean bold) {
            isBold = bold;
        }

        public void setItalic(boolean italic) {
            isItalic = italic;
        }

        public void setUnderline(boolean underline) {
            this.underline = underline;
        }

        public void setStrikeThrough(boolean strikeThrough) {
            this.strikeThrough = strikeThrough;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public void setTextSize(int textSize) {
            this.textSize = textSize;
        }

        public void setBgShape(int bgShape) {
            this.bgShape = bgShape;
        }

        public void setPureColorBg(int pureColorBg) {
            this.pureColorBg = pureColorBg;
        }

        public void setDrawableBg(Drawable drawableBg) {
            this.drawableBg = drawableBg;
        }

        public boolean isBold() {
            return isBold;
        }

        public boolean isItalic() {
            return isItalic;
        }

        public boolean isUnderline() {
            return underline;
        }

        public boolean isStrikeThrough() {
            return strikeThrough;
        }

        public int getTextColor() {
            return textColor;
        }

        public int getTextSize() {
            return textSize;
        }

        public int getBgShape() {
            return bgShape;
        }

        public @ColorInt int getPureColorBg() {
            return pureColorBg;
        }

        public Drawable getDrawableBg() {
            return drawableBg;
        }
    }
}
