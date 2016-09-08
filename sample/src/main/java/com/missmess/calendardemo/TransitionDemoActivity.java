package com.missmess.calendardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.missmess.calendarview.MonthView;
import com.missmess.calendarview.TransitRootView;
import com.missmess.calendarview.YearView;

public class TransitionDemoActivity extends AppCompatActivity {

    private TransitRootView transitRootView;
    private YearView yearView;
    private MonthView monthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition_demo);

        //find view
        transitRootView = (TransitRootView) findViewById(R.id.trv);
        yearView = (YearView) findViewById(R.id.yv);
        monthView = (MonthView) findViewById(R.id.mv);

        //init
        transitRootView.assignView(yearView, monthView);
    }

    @Override
    public void onBackPressed() {
        if(!transitRootView.applyHide()) {
            super.onBackPressed();
        }
    }
}
