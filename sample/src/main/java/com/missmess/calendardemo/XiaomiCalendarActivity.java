package com.missmess.calendardemo;

import android.app.ProgressDialog;
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

import java.util.List;

/**
 * @author wl
 * @since 2017/08/10 14:56
 */
public class XiaomiCalendarActivity extends AppCompatActivity {
    private MonthViewPager monthViewPager;
    private TextView textView;
    private TextView month;
    private TextView year;
    private ProgressDialog progressDialog;
    private List<DayEvent> yearEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xiaomi_calendar);
        // find view
        monthViewPager = (MonthViewPager) findViewById(R.id.mvp);
        textView = (TextView) findViewById(R.id.tv);
        month = (TextView) findViewById(R.id.month);
        year = (TextView) findViewById(R.id.year);

        init();
        // request all events(daemon)
        getEvents();
    }

    private void init() {
        monthViewPager.setMonthRange(new CalendarMonth(2016, 1), new CalendarMonth(2017, 10));
        monthViewPager.addOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                Log.d("xiaomi_calendar", "old=" + old.toString() + ";current=" + currentMonth.toString());
                year.setText(currentMonth.getYear() + "年");
                month.setText(currentMonth.getMonth() + "月");
            }
        });
        monthViewPager.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                for(DayEvent event : yearEvents) {
                    if(event.isThisDay(calendarDay)) {
                        textView.setText(String.format("Today is \n%s\nToday have %d events", calendarDay.toString(), event.getEventDetails().length));
                        return;
                    }
                }
                textView.setText(R.string.no_event);
            }
        });
    }

    private void getEvents() {
        new GetDecorsTask(new GetDecorsTask.DecorResult() {
            @Override
            public void onStart() {
                progressDialog = ProgressDialog.show(XiaomiCalendarActivity.this, null, "loading...", false, false);
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
