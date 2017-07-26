package com.faith.fd.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.faith.fd.R;

/**
 * scollview自带下拉回弹功能测试
 * @autor hongbing
 * @date 2017/7/25
 */
public class ScrollViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollview);
    }


    /**
     *
     * 结论：
     * android7.0及以上：scrollview内容超出屏幕，会自动有上下拉回弹效果
     * 7.0以下的模拟器都是没有该效果的
     * 也不排除有些手机厂商对官方的sdk做个修改
     *
     *
     *
     *
     */
}
