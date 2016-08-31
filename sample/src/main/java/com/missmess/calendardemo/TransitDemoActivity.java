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
import com.missmess.calendarview.TransitRootView;
import com.missmess.calendarview.YearMonthTransformer;
import com.missmess.calendarview.YearView;

import java.util.ArrayList;
import java.util.List;

public class TransitDemoActivity extends AppCompatActivity {
    private final int YEAR = 2016;
    private List<DayEvent> yearEvents;
    private YearMonthTransformer transformer;
    private TransitRootView rootView;
    private YearView yearView;
    private MonthView monthView;
    private View rl_title;
    private View ll_data;
    private ListView listView;
    private EventAdapter adapter;
    private TextView tv_year;
    private TextView textView1;
    private TextView textView2;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_demo);
        // find view
        rootView = (TransitRootView) findViewById(R.id.trv);
        ll_data = findViewById(R.id.ll_data);
        tv_year = (TextView) findViewById(R.id.tv_year);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        yearView = (YearView) findViewById(R.id.yv);
        listView = (ListView) findViewById(R.id.lv);
        rl_title = findViewById(R.id.rl_title);
        monthView = (MonthView) findViewById(R.id.mv);
        transformer = new YearMonthTransformer(rootView, yearView, monthView);
        progressDialog = new ProgressDialog(this);

        // init listener
        initListener();
        // obtain events and decors
        getEvents();
    }

    private void getEvents() {
        new GetDecorsTask(new GetDecorsTask.DecorResult() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onResult(List<DayEvent> events) {
                yearEvents = events;

                // init YearView
                initYearView();
                // init other view data
                initDatas();

                progressDialog.dismiss();
            }
        }).execute(YEAR);
    }

    private void initYearView() {
        yearView.setYear(YEAR);
        yearView.setToday(new CalendarDay(YEAR, 5, 17));

        DayDecor dayDecor = new DayDecor();
        for(DayEvent event : yearEvents) {
            CalendarDay calendarDay = new CalendarDay(event.getYear(), event.getMonth(), event.getDay());
            dayDecor.putOne(calendarDay, event.getType().getColor());
        }
        yearView.setDecors(dayDecor);
    }

    private void initDatas() {
        adapter = new EventAdapter();
        listView.setAdapter(adapter);

        tv_year.setText(yearView.getYearString());
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
        yearView.setOnMonthClickListener(new YearView.OnMonthClickListener() {
            @Override
            public void onMonthClick(YearView yearView, CalendarMonth calendarMonth) {
                transformer.applyShow(calendarMonth.getMonth());
            }
        });
        monthView.setOnDayClickListener(new MonthView.OnDayClickListener() {
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
        transformer.setOnTransitListener(new YearMonthTransformer.OnTransitListener() {
            @Override
            public void onY2MTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutView(tv_year, false);
                transiter.alphaView(rl_title, false);
            }

            @Override
            public void onY2MTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideInView(ll_data, false);
            }

            @Override
            public void onM2YTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutView(ll_data, true);
            }

            @Override
            public void onM2YTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                // clear event info
                adapter.clear();
                adapter.notifyDataSetChanged();
                transiter.slideInView(tv_year, true);
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
