package com.faith.fd;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.faith.fd.recivier.LocalRecivier;

public class DoubleBufferActivity extends AppCompatActivity {

    private LocalRecivier mRecivier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_buffer);
        mRecivier = new LocalRecivier();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mRecivier,new IntentFilter("com.faith.fd.USER_ACTION"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRecivier);
    }
}
