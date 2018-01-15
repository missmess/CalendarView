package com.missmess.calendardemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.missmess.calendardemo.adapter.Event2Adapter;
import com.missmess.calendardemo.control.GetDecorsTask;
import com.missmess.calendardemo.model.DayEvent;
import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.DayDecor;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.MonthViewPager;
import com.missmess.calendarview.ScrollingMonthPagerBehavior;

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
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xiaomi_calendar);
        // find view
        monthViewPager = (MonthViewPager) findViewById(R.id.mvp);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        textView = (TextView) findViewById(R.id.tv);
        month = (TextView) findViewById(R.id.month);
        year = (TextView) findViewById(R.id.year);

        init();
        // request all events(daemon)
        getEvents();
    }

    private void init() {
        monthViewPager.setCurrentMonth(new CalendarMonth(2017, 8));
        monthViewPager.setMonthRange(new CalendarMonth(2016, 10), new CalendarMonth(2018, 10));
        monthViewPager.addOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                Log.d("xiaomi_calendar", "old=" + old.toString() + "; current=" + currentMonth.toString());
                year.setText(currentMonth.getYear() + "年");
                month.setText(currentMonth.getMonth() + "月");
            }
        });
        monthViewPager.setOnSelectionChangeListener(new MonthView.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(MonthView monthView, CalendarDay[] now, CalendarDay[] old, CalendarDay selection, boolean byUser) {
                //                Log.v("xiaomi_calendar", "byUser=" + byUser + "; old=" + Arrays.toString(old) + "; now=" +  Arrays.toString(now));
                //                for(DayEvent event : yearEvents) {
                //                    if(Arrays.asList(now).contains(event.getCalendarDay())) {
                //                        textView.setText(String.format("Today is \n%s\nToday have %d events", now[0], event.getEventDetails().length));
                //                        return;
                //                    }
                //                }
                //                textView.setText(R.string.no_event);
            }
        });

        recyclerView.setAdapter(new Event2Adapter());
        ScrollingMonthPagerBehavior.from(recyclerView).setOnStateChangeListener(new ScrollingMonthPagerBehavior.OnStateChangeListener() {
            @Override
            public void onExpanded() {
                Log.w("xiaomiactivity", "onExpanded");
            }

            @Override
            public void onCollapsed() {
                Log.w("xiaomiactivity", "onCollapsed");
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
        getMenuInflater().inflate(R.menu.xiaomi_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                if(monthViewPager.isShowingOtherMonth()) {
                    monthViewPager.setShowOtherMonth(false);
                } else {
                    monthViewPager.setShowOtherMonth(true);
                }
                break;
            case R.id.item2:
                if(monthViewPager.isMonthMode()) {
                    monthViewPager.setWeekMode();
                } else {
                    monthViewPager.setMonthMode();
                }
                break;
            case R.id.item3:
                monthViewPager.setCurrentMonth(new CalendarMonth(2017, 1));
                break;
            case R.id.item4:
                monthViewPager.setSelection(new CalendarDay(2017, 4, 2));
                break;
            case R.id.item5:
                CalendarDay[] range = monthViewPager.getShowingDayRange();
                String s = "从" + range[0].toString() + "到" + range[1].toString();
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                break;
            case R.id.item6:
                monthViewPager.setDayDisable(new CalendarDay(monthViewPager.getCurrentMonth(), 30));
                break;
            case R.id.item7:
                monthViewPager.setSelectionMode(MonthView.SELECTION_NONE);
                break;
            case R.id.item8:
                monthViewPager.setSelectionMode(MonthView.SELECTION_SINGLE);
                break;
            case R.id.item9:
                monthViewPager.setSelectionMode(MonthView.SELECTION_MULTI);
                break;
            case R.id.item10:
                monthViewPager.setSelectionMode(MonthView.SELECTION_RANGE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
