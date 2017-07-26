package com.faith.fd.activity.eventdistribute;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * 事件分发机制熟悉测试类
 * @autor hongbing
 * @date 2017/7/24
 */
public class EventDistributeActivity extends AppCompatActivity {
    private static final String TAG = "EventDistributeActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn = new Button(this);

        setContentView(btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick execute");
            }
        });

        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG,"onTouch execute,action = MotionEvent.ACTION_DOWN");
                        return false;
//                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG,"onTouch execute,action = MotionEvent.ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG,"onTouch execute,action = MotionEvent.ACTION_UP");
                        break;
                    default:
                        break;
                }
//                Log.d(TAG,"onTouch execute,action = " + event.getAction());
                return false;
            }
        });
    }
}
