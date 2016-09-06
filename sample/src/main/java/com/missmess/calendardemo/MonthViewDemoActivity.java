package com.missmess.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.DayDecor;
import com.missmess.calendarview.MonthView;

public class MonthViewDemoActivity extends AppCompatActivity {

    private MonthView monthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view_demo);

        //find view
        monthView = (MonthView) findViewById(R.id.mv);

        init();
    }

    private void init() {
        monthView.setYearAndMonth(new CalendarMonth(2017, 2));
        monthView.setToday(new CalendarDay(2017, 2, 12));
        // add decorators
        DayDecor dayDecor = new DayDecor();
        // circle bg
        dayDecor.putOne(new CalendarDay(2017, 2, 1), 0xFFFF6600);
        // rectangle bg
        int color = 0xFFAAAAAA;
        dayDecor.putOne(new CalendarDay(2017, 2, 4), color, DayDecor.Style.RECTANGLE);
        dayDecor.putOne(new CalendarDay(2017, 2, 11), color, DayDecor.Style.RECTANGLE);
        dayDecor.putOne(new CalendarDay(2017, 2, 18), color, DayDecor.Style.RECTANGLE);
        dayDecor.putOne(new CalendarDay(2017, 2, 25), color, DayDecor.Style.RECTANGLE);
        // drawable bg
        dayDecor.putOne(new CalendarDay(2017, 2, 19), getResources().getDrawable(R.drawable.a_decor));
        // styled background and text
        DayDecor.Style style = new DayDecor.Style();
        style.setTextSize(getResources().getDimensionPixelSize(R.dimen.big_text));
        style.setTextColor(0xFF72E6BC);
        style.setBold(true);
        style.setItalic(true);
        style.setUnderline(true);
        style.setStrikeThrough(true);
        style.setPureColorBgShape(DayDecor.Style.CIRCLE);
        style.setPureColorBg(0xFF66AA76);
        dayDecor.putOne(new CalendarDay(2017, 2, 24), style);
        monthView.setDecors(dayDecor);
        // add listener
        monthView.setOnMonthTitleClickListener(new MonthView.OnMonthTitleClickListener() {
            @Override
            public void onMonthClick(MonthView monthView, CalendarMonth calendarMonth) {
                Toast.makeText(MonthViewDemoActivity.this, "title clicked: " + calendarMonth.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        monthView.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                Toast.makeText(MonthViewDemoActivity.this, "day clicked: " + calendarDay.toString(), Toast.LENGTH_SHORT).show();
                monthView.clearSelection();
            }
        });
    }
}
