package com.missmess.calendardemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        dayDecor.putOne(new CalendarDay(2017, 2, 11), 0xFFAAAAAA, DayDecor.Style.RECTANGLE);
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
        monthView.setOnSelectionChangeListener(new MonthView.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(MonthView monthView, CalendarDay now, CalendarDay old, boolean byUser) {
                Toast.makeText(MonthViewDemoActivity.this, "selection change to: " + now, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.button5:
                monthView.setSelection(null);
                break;
            case R.id.button6:
                DayDecor.Style style = new DayDecor.Style();
                style.setBold(true);
                style.setTextSize(getResources().getDimensionPixelSize(R.dimen.big_text));
                style.setPureColorBg(Color.BLACK);
                monthView.setSelectionStyle(style);
                break;
            case R.id.button7:
                if(monthView.getCurrentMonth().getYear() == 2017) {
                    monthView.setYearAndMonth(2000, 4);
                } else {
                    monthView.setYearAndMonth(2017, 2);
                }
                break;
            case R.id.button8:
                if(monthView.isWeekMode()) {
                    monthView.showMonthMode();
                } else {
                    monthView.showWeekMode();
                }
                break;
        }
    }
}
