package com.missmess.calendardemo;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.missmess.calendardemo.control.GetDecorsTask;
import com.missmess.calendardemo.model.DayEvent;
import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.DayDecor;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.MonthViewPager;

import java.util.Arrays;
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
        monthViewPager.setDayLabel(monthViewPager.getToday(), "今");
        monthViewPager.setMonthRange(new CalendarMonth(2015, 12), new CalendarMonth(2017, 2));
        monthViewPager.addOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                Log.d("onMonthChanged", "old=" + old.toString() + ";current=" + currentMonth.toString() +
                        ";left=" + (previous == null ? "null" : previous.getCurrentMonth().toString()) +
                        ";right=" + (next == null ? "null" : next.getCurrentMonth().toString()));
            }
        });
        monthViewPager.setOnSelectionChangeListener(new MonthView.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(MonthView monthView, CalendarDay[] now, CalendarDay[] old, CalendarDay selection, boolean byUser) {
                for(DayEvent event : yearEvents) {
                    if(Arrays.asList(now).contains(event.getCalendarDay())) {
                        textView.setText(String.format("Today is \n%s\nToday have %d events", now[0].toString(), event.getEventDetails().length));
                        return;
                    }
                }
                textView.setText(R.string.no_event);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mvp_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                DayDecor.Style style = new DayDecor.Style();
                style.setBold(true);
                style.setTextSize(getResources().getDimensionPixelSize(R.dimen.big_text));
                style.setPureColorBg(Color.BLACK);
                monthViewPager.setSelectionStyle(style);
                break;
            case R.id.item2:
                monthViewPager.setSelection(new CalendarDay(monthViewPager.getCurrentMonth(), 1));
                break;
            case R.id.item3:
                monthViewPager.setCurrentMonth(new CalendarMonth(2016, 1));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
