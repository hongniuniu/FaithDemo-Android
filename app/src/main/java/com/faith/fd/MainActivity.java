package com.faith.fd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.faith.fd.activity.DiDiActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDiDiClk(View v) {
        Log.d(TAG,"onDiDiClk...");
        startActivity(new Intent(this, DiDiActivity.class));
    }

}
