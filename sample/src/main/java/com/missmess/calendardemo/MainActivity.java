package com.missmess.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.missmess.calendarview.AnimTransiter;
import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.TransitRootView;
import com.missmess.calendarview.YearMonthTransformer;
import com.missmess.calendarview.YearView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private YearView yearView;
    private MonthView monthView;
    private YearMonthTransformer transformer;
    private TextView textView;
    private final int year = 2016;
    private View rl_title;
    private View ll_data;
    private TransitRootView rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (TransitRootView) findViewById(R.id.trv);
        textView = (TextView) findViewById(R.id.tv);
        ll_data = findViewById(R.id.ll_data);
        yearView = (YearView) findViewById(R.id.yv);
        rl_title = findViewById(R.id.rl_title);
        monthView = (MonthView) findViewById(R.id.mv);
        transformer = new YearMonthTransformer(rootView, yearView, monthView);

        initYearInfo();

        yearView.setOnMonthClickListener(new YearView.OnMonthClickListener() {
            @Override
            public void onMonthClick(YearView simpleMonthView, CalendarMonth calendarMonth) {
                transformer.applyShow(calendarMonth.getMonth());
            }
        });
        monthView.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(MonthView monthView, CalendarDay calendarDay) {
                Toast.makeText(getApplicationContext(), calendarDay.getYear() + "-" + calendarDay.getMonth() + "-" + calendarDay.getDay(), Toast.LENGTH_SHORT).show();
            }
        });
        transformer.setOnTransitListener(new YearMonthTransformer.OnTransitListener() {
            @Override
            public void onY2MTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutView(rl_title, false);
            }

            @Override
            public void onY2MTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideInView(ll_data, false);
//                transiter.alphaView(ll_data, true);
            }

            @Override
            public void onM2YTransitStart(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideOutView(ll_data, true);
            }

            @Override
            public void onM2YTransitEnd(AnimTransiter transiter, YearView yearView, MonthView monthView) {
                transiter.slideInView(rl_title, true);
            }
        });
    }

    private void initYearInfo() {
        yearView.setYear(year);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 17);
        yearView.setToday(calendar);
        yearView.decorateDay(new CalendarDay(year, 3, 14), 0xFFFF6600);
        yearView.decorateDay(new CalendarDay(year, 3, 15), 0xFFDC66C0);
        yearView.decorateDay(new CalendarDay(year, 6, 2), 0xFF66AA76);
        yearView.decorateDay(new CalendarDay(year, 9, 23), 0xFF66AA76);
    }

    @Override
    public void onBackPressed() {
        if (!transformer.applyHide())
            super.onBackPressed();
    }
}
