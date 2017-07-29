package com.faith.fd.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;
import com.faith.fd.bean.VideoItem;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnK4LVideoListener;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

/**
 * 视频裁剪
 *
 * @autor hongbing
 * @date 2017/7/28
 */
public class CutVideoActivity extends BaseActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private static final String TAG = "CutVideoActivity";
    private VideoItem mVideoItem;
    private K4LVideoTrimmer mVideoTrimmer;


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(newBase);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//    }

    @Override
    protected void handleIntent(Intent intent) {
        Bundle bundle = intent.getBundleExtra("budle");
        mVideoItem = bundle.getParcelable("data");
    }

    @Override
    protected int getLayoutId() {
        /**
         * 如果style中主题使用了<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">,
         * 这里需要用getSupportActionBar()
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("视频裁剪");
        return R.layout.activity_cutvideo;
    }

    @Override
    protected void initView() {
        mVideoTrimmer = (K4LVideoTrimmer) findViewById(R.id.video_trimmer);
        mVideoTrimmer.setMaxDuration(15);
        mVideoTrimmer.setOnTrimVideoListener(this);
        mVideoTrimmer.setOnK4LVideoListener(this);
        mVideoTrimmer.setVideoURI(Uri.parse(mVideoItem.getPath()));
        mVideoTrimmer.setVideoInformationVisibility(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                Log.d(TAG, "保存裁剪结果...");
                mVideoTrimmer.onSaveClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onVideoPrepared() {

    }

    @Override
    public void onTrimStarted() {

    }

    @Override
    public void getResult(String videoPath, String framePath, long duration) {
        Log.d(TAG,"getResult-->裁剪结果回调：videoPath = " + videoPath +
                "-->framePath = " + framePath +
                "-->duration = " + duration);
        mVideoItem.setPath(videoPath);
        mVideoItem.setFrame(framePath);
        mVideoItem.setDuration(duration);
        Intent intent = new Intent();
        intent.putExtra("data",mVideoItem);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
    }

    @Override
    public void onError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(message);
            }
        });
    }
}
