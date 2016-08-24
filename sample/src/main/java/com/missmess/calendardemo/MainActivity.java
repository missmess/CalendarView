package com.missmess.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.missmess.calendarview.CalendarDay;
import com.missmess.calendarview.CalendarMonth;
import com.missmess.calendarview.YearMonthTransformer;
import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.YearView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private YearView yearView;
    private MonthView monthView;
    private YearMonthTransformer transformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        yearView = (YearView) findViewById(R.id.yv);
        monthView = (MonthView) findViewById(R.id.smv);

//        yearView.setYear(2015);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2016);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 17);
        yearView.setToday(calendar);
        yearView.decorateDay(new CalendarDay(2016, 3, 12), 0xFFFF6600);
        yearView.decorateDay(new CalendarDay(2016, 3, 11), 0xFFDC66C0);
        yearView.decorateDay(new CalendarDay(2016, 9, 23), 0xFFBDCC76);

        transformer = new YearMonthTransformer(yearView, monthView);
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
    }

    @Override
    public void onBackPressed() {
        if (!transformer.applyHide())
            super.onBackPressed();
    }
}
