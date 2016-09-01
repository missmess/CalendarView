package com.missmess.calendardemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.missmess.calendardemo.control.GetDecorsTask;
import com.missmess.calendardemo.model.DayEvent;
import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.DayDecor;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.MonthViewPager;

import java.util.List;

public class MonthViewPagerActivity extends AppCompatActivity {

    private MonthViewPager monthViewPager;
    private TextView textView;
    private ProgressDialog progressDialog;
    private List<DayEvent> yearEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view_pager);
        // find view
        monthViewPager = (MonthViewPager) findViewById(R.id.mvp);
        textView = (TextView) findViewById(R.id.tv);

        init();
        // request all events(daemon)
        getEvents();
    }

    private void init() {
        monthViewPager.setCurrentMonth(new CalendarMonth(2016, 3));
        monthViewPager.setToday(new CalendarDay(2016, 3, 12));
        monthViewPager.setMonthRange(new CalendarMonth(2015, 12), new CalendarMonth(2017, 2));
        monthViewPager.setOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                Log.d("onMonthChanged", "old=" + old.toString() + ";current=" + currentMonth.toString() +
                        ";left=" + (previous == null ? "null" : previous.getCurrentMonth().toString()) +
                        ";right=" + (next == null ? "null" : next.getCurrentMonth().toString()));
                textView.setText(R.string.app_name);
            }
        });
        monthViewPager.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                for(DayEvent event : yearEvents) {
                    if(event.isThisDay(calendarDay)) {
                        textView.setText(String.format("Today: %s\nThere is %d events", calendarDay.toString(), event.getEventDetails().length));
                        return;
                    }
                }
            }
        });
        monthViewPager.setOnMonthTitleClickListener(new MonthView.OnMonthTitleClickListener() {
            @Override
            public void onMonthClick(MonthView monthView, CalendarMonth calendarMonth) {
                monthViewPager.setCurrentMonth(new CalendarMonth(2017, 2));
            }
        });
        monthViewPager.setOnDragListener(new MonthViewPager.OnDragListener() {
            @Override
            public void onDrag(MonthView middle, int left, int dx) {
                Log.d("OnDragListener", "left==" + left + ";dx==" + dx);
            }
        });
    }

    private void getEvents() {
        new GetDecorsTask(new GetDecorsTask.DecorResult() {
            @Override
            public void onStart() {
                progressDialog = ProgressDialog.show(MonthViewPagerActivity.this, null, "loading...", false, false);
            }

            @Override
            public void onResult(List<DayEvent> events) {
                yearEvents = events;

                DayDecor dayDecor = new DayDecor();
                for(DayEvent event : yearEvents) {
                    CalendarDay calendarDay = new CalendarDay(event.getYear(), event.getMonth(), event.getDay());
                    dayDecor.putOne(calendarDay, event.getType().getColor());
                }
                monthViewPager.setDecors(dayDecor);

                progressDialog.dismiss();
            }
        }).execute(2016);
    }
}
