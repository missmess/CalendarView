package com.missmess.calendardemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.missmess.calendardemo.adapter.EventAdapter;
import com.missmess.calendardemo.control.GetDecorsTask;
import com.missmess.calendardemo.model.DayEvent;
import com.missmess.calendardemo.model.EventType;
import com.missmess.calendarview.AnimTransiter;
import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.DayDecor;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.MonthViewPager;
import com.missmess.calendarview.TransitRootView;
import com.missmess.calendarview.YearMonthTransformer;
import com.missmess.calendarview.YearView;

import java.util.ArrayList;
import java.util.List;

public class ComplexDemoActivity extends AppCompatActivity {
    private final int YEAR = 2016;
    private List<DayEvent> yearEvents;
    private YearMonthTransformer transformer;
    private TransitRootView rootView;
    private YearView yearView;
    private View rl_title;
    private View ll_data;
    private ListView listView;
    private EventAdapter adapter;
    private TextView tv_year;
    private TextView textView1;
    private TextView textView2;
    private ProgressDialog progressDialog;
    private MonthViewPager monthViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_demo);
        // find view
        rootView = (TransitRootView) findViewById(R.id.trv);
        ll_data = findViewById(R.id.ll_data);
        tv_year = (TextView) findViewById(R.id.tv_year);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        yearView = (YearView) findViewById(R.id.yv);
        listView = (ListView) findViewById(R.id.lv);
        rl_title = findViewById(R.id.rl_title);
        monthViewPager = (MonthViewPager) findViewById(R.id.mvp);

        //init
        yearView.setYear(YEAR);
        yearView.setToday(new CalendarDay(YEAR, 5, 17));
        tv_year.setText(yearView.getYearString());
        adapter = new EventAdapter();
        listView.setAdapter(adapter);
        monthViewPager.setMonthRange(new CalendarMonth(YEAR, 1), new CalendarMonth(YEAR, 12));
        transformer = new YearMonthTransformer(rootView, yearView, monthViewPager);

        // add listener
        initListener();
        // obtain events and decors
        getEvents(YEAR);
    }

    private void getEvents(int year) {
        new GetDecorsTask(new GetDecorsTask.DecorResult() {
            @Override
            public void onStart() {
                progressDialog = ProgressDialog.show(ComplexDemoActivity.this, null, "loading...", true, false);
            }

            @Override
            public void onResult(List<DayEvent> events) {
                yearEvents = events;
                // add decorators
                addDecors();
                // init other view data
                initDatas();

                progressDialog.dismiss();
            }
        }).execute(year);
    }

    private void addDecors() {
        DayDecor dayDecor = new DayDecor();
        for(DayEvent event : yearEvents) {
            CalendarDay calendarDay = new CalendarDay(event.getYear(), event.getMonth(), event.getDay());
            dayDecor.putOne(calendarDay, event.getType().getColor());
        }
        yearView.setDecors(dayDecor);
    }

    private void initDatas() {
        textView1.setText(getString(R.string.event_str, yearEvents.size()));
        ArrayList<EventType> temp = new ArrayList<>();
        for(DayEvent event : yearEvents) {
            if(!temp.contains(event.getType())) {
                temp.add(event.getType());
            }
        }
        textView2.setText(getString(R.string.event_type_str, temp.size()));
    }

    private void initListener() {
        monthViewPager.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                for(DayEvent event : yearEvents) {
                    if(event.isThisDay(calendarDay)) {
                        adapter.setDetails(event.getType().name(), event.getEventDetails());
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        monthViewPager.addOnMonthChangeListener(new MonthViewPager.OnMonthChangeListener() {
            @Override
            public void onMonthChanged(MonthViewPager monthViewPager, MonthView previous, MonthView current, MonthView next, CalendarMonth currentMonth, CalendarMonth old) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        transformer.setOnTransitListener(new YearMonthTransformer.OnTransitListener() {
            @Override
            public void onY2MTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutViewVertical(tv_year, false);
                transiter.alphaView(rl_title, false);
            }

            @Override
            public void onY2MTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideInViewVertical(ll_data, false);
            }

            @Override
            public void onM2YTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutViewVertical(ll_data, true);
            }

            @Override
            public void onM2YTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                // clear event info
                adapter.clear();
                adapter.notifyDataSetChanged();
                transiter.slideInViewVertical(tv_year, true);
                transiter.alphaView(rl_title, true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!transformer.applyHide())
            super.onBackPressed();
    }

}
