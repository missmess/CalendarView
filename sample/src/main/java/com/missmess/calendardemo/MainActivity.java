package com.missmess.calendardemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.button:
                startActivity(new Intent(this, YearViewActivity.class));
                break;
            case R.id.button1:
                startActivity(new Intent(this, MonthViewActivity.class));
                break;
            case R.id.button2:
                startActivity(new Intent(this, TransitDemoActivity.class));
                break;
        }
    }
}
