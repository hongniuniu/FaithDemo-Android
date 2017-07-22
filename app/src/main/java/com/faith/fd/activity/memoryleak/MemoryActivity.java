package com.faith.fd.activity.memoryleak;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.faith.fd.R;

/**
 * 制造内存泄漏页
 * @autor hongbing
 * @date 2017/7/22
 */
public class MemoryActivity extends AppCompatActivity implements SampleListener{

    private static final String TAG = "MemoryActivity";
//    private BigObject mBigObject = new BigObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_leak);
        Log.d(TAG,"onCreate size:" + ListenerManager.getInstance().getListenerSize());
        ListenerManager.getInstance().addListener(this);
    }

    @Override
    public void doSomething() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int size = ListenerManager.getInstance().getListenerSize();
        Log.d(TAG,"size = " + size);
        ListenerManager.getInstance().removeListener(this);
    }
}
