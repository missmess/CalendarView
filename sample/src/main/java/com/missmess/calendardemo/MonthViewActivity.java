package com.missmess.calendardemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.MonthViewPager;

public class MonthViewActivity extends AppCompatActivity {

    private MonthViewPager monthViewPager;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);
        // find view
        monthViewPager = (MonthViewPager) findViewById(R.id.mvp);
        textView = (TextView) findViewById(R.id.tv);

        init();
    }

    private void init() {
        monthViewPager.setMonthRange(new CalendarMonth(2015, 12), new CalendarMonth(2017, 2));
        monthViewPager.setCurrentMonth(new CalendarMonth(2016, 3));
        monthViewPager.setToday(new CalendarDay(2016, 3, 12));
        monthViewPager.setOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                Toast.makeText(MonthViewActivity.this, "month changed! from " + old.toString() + " to " + currentMonth.toString(), Toast.LENGTH_LONG).show();
            }
        });
        monthViewPager.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                textView.setText(String.format("This is all events in %s", calendarDay.toString()));
            }
        });
        monthViewPager.setOnMonthTitleClickListener(new MonthView.OnMonthTitleClickListener() {
            @Override
            public void onMonthClick(MonthView monthView, CalendarMonth calendarMonth) {
                monthViewPager.setCurrentMonth(new CalendarMonth(2017, 1));
            }
        });
        monthViewPager.setOnDragListener(new MonthViewPager.OnDragListener() {
            @Override
            public void onDrag(MonthView middle, int left, int dx) {
                Log.d("OnDragListener", "left==" + left + ";dx==" + dx);
            }
        });
    }
}
