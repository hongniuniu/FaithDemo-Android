package com.faith.fd.activity.memoryleak;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * @autor hongbing
 * @date 2017/7/22
 */

public class BigObject {
    private ArrayList<Bitmap> mBitmaps = new ArrayList<>();
    private String[] values = new String[1000];

    public BigObject(){
        for (int i = 0; i < 20; i++) {
            mBitmaps.add(Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888));
        }

        for (int i = 0; i < 1000; i++) {
            values[i] = "value:" + i;
        }
    }
}
