package com.faith.fd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.faith.fd.activity.DiDiActivity;
import com.faith.fd.activity.PropAnimActivity;
import com.faith.fd.activity.bitmap.LoadBitmapActivity;
import com.faith.fd.activity.memoryleak.MemoryActivity;

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

    public void onPropAnimClk(View v){
        startActivity(new Intent(this, PropAnimActivity.class));
    }


    public void onMemoryClk(View v){
        startActivity(new Intent(this, MemoryActivity.class));
    }

    public void onBmpClk(View v){
        startActivity(new Intent(this, LoadBitmapActivity.class));
    }

}
