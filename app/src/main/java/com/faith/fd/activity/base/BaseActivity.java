package com.faith.fd.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * @autor hongbing
 * @date 2017/7/26
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected Activity mAct;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAct = this;
        setContentView(getLayoutId());
        handleIntent(getIntent());
        initView();
        initData();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected void initData(){}

    protected void handleIntent(Intent intent){};

    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void showToast(String toastText) {
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }


}
