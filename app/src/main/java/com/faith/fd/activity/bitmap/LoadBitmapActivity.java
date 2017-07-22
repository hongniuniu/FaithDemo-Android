package com.faith.fd.activity.bitmap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.faith.fd.R;

/**
 * @autor hongbing
 * @date 2017/7/22
 */
public class LoadBitmapActivity extends AppCompatActivity {
    private static final String TAG = "LoadBitmapActivity";
    private ImageView testImg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_bitmap);
        testImg = (ImageView) findViewById(R.id.id_testImg);
        loadImgRes();

//        loadImgbg();
    }


    private void loadImgRes() {
        long startTime = System.currentTimeMillis();
        testImg.setImageResource(R.mipmap.wx_red_package_dialog_bg);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "setImageResource耗时：" + (endTime - startTime) + "ms");

        /**
         *
         *
         * 07-22 11:28:08.602 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：45ms
         07-22 11:28:11.035 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：1ms
         07-22 11:28:13.877 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：1ms
         07-22 11:28:16.730 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：1ms
         07-22 11:28:18.667 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：0ms
         07-22 11:28:20.125 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：0ms
         07-22 11:28:21.614 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：42ms
         07-22 11:28:23.102 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：0ms
         07-22 11:28:41.043 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：38ms
         07-22 11:28:46.115 1904-1904/com.faith.fd D/LoadBitmapActivity: setImageResource耗时：0ms

         *
         */

    }

    private void loadImgbg(){
        long startTime = System.currentTimeMillis();
        testImg.setBackgroundResource(R.mipmap.wx_red_package_dialog_bg);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "setBackgroundResource耗时：" + (endTime - startTime) + "ms");

        /**
         * 连续执行10此的结果，第9次等待了10s多有
         07-22 11:24:40.965 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：89ms
         07-22 11:24:47.883 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：1ms
         07-22 11:24:50.506 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:24:52.714 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:24:54.803 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:24:56.859 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:24:58.639 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:25:01.712 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         07-22 11:25:30.409 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：50ms
         07-22 11:25:34.808 29280-29280/com.faith.fd D/LoadBitmapActivity: setBackgroundResource耗时：0ms
         *
         */
    }

}
