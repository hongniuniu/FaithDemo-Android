package com.faith.fd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;
import com.faith.fd.bean.CustomVideo;
import com.faith.fd.utils.CommonUtil;
import com.faith.fd.utils.FileUtil;
import com.faith.fd.utils.PlayVideoHelper;

import java.io.File;

import ren.helloworld.wv.helper.WvUtils;

/**
 * 视频预览播放页
 * @autor hongbing
 * @date 2017/7/29
 */
public class PlayVideoActivity extends BaseActivity implements View.OnClickListener{

    private TextView mTvSaveAlbum;
    private TextView mTvEditoAgain;
    private TextView mTvForwardVideo;
    private RelativeLayout mLlPlayVideo;
    private PlayVideoHelper mPlayerHelper;

    private String mName, mPath;
    /**
     * 是否可以转发
     */
    private boolean mIsForward = false;
    /**
     * 是否可以再次编辑视频
     */
    private boolean mIsShowEidt = false;
    private CustomVideo mCustomVideo;

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);
        Bundle bundle = intent.getBundleExtra("bundle");
        if(bundle == null){
            finish();
            showToast("参数异常...");
            return;
        }
        mIsShowEidt = bundle.getBoolean("show_edit");
        mIsForward = bundle.getBoolean("isforward");
        mCustomVideo = bundle.getParcelable("data");
        mName = mCustomVideo.getName();
        mPath = mCustomVideo.getLocalVideoPath();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_play_video;
    }

    @Override
    protected void initView() {
        mLlPlayVideo = (RelativeLayout) findViewById(R.id.ll_play_root);
        mLlPlayVideo.setOnClickListener(this);

        mPlayerHelper = new PlayVideoHelper(mLlPlayVideo);
        mPlayerHelper.play(mName, mPath);

        if (mIsShowEidt) {
            mTvEditoAgain = (TextView) findViewById(R.id.tv_edit_again);
            mTvEditoAgain.setOnClickListener(this);
            mTvEditoAgain.setVisibility(View.VISIBLE);
        }

        if (mIsForward) {
            mTvForwardVideo = (TextView) findViewById(R.id.tv_forward_video);
            mTvForwardVideo.setOnClickListener(this);
            mTvForwardVideo.setVisibility(View.VISIBLE);

            mTvSaveAlbum = (TextView) findViewById(R.id.tv_save_album);
            mTvSaveAlbum.setOnClickListener(this);
            mTvSaveAlbum.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerHelper.onDestroy();
    }



    @Override
    public void onClick(View v) {
        if (v == mLlPlayVideo) {
            finish();
        }
        // 再次编辑视频
        else if (mTvEditoAgain != null && v == mTvEditoAgain) {
//            if (mIsForward) {
//                finish();
//                return;
//            }
//
//            finish();
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("data", mCustomVideo);
//            CustomVideoFragment fragment = new CustomVideoFragment();
//            fragment.setArguments(bundle);
//            mActivity.addFragment(fragment);
        }

        // 转发视频
        else if (mTvForwardVideo != null && v == mTvForwardVideo) {
//            checkForwardService(mCustomVideo);
        }

        // 保存到相册
        else if (mTvSaveAlbum != null && v == mTvSaveAlbum) {
            // 自定义相册文件
            String finalNewDir = FileUtil.SD_CUSTOM_VIDEO_DIR().getAbsolutePath();

            // 复制视频文件
            File fromVideo = new File(mCustomVideo.getLocalVideoPath());
            File toVideo = new File(finalNewDir, fromVideo.getName());
            WvUtils.fileChannelCopy(fromVideo, toVideo);

            // 复制图片文件
            File fromJpg = new File(mCustomVideo.getLocalImagePath());
            File toJpg = new File(finalNewDir, fromJpg.getName());
            WvUtils.fileChannelCopy(fromJpg, toJpg);

            // 更换为最新的路径
            mCustomVideo.setLocalImagePath(toJpg.getAbsolutePath());
            mCustomVideo.setLocalVideoPath(toVideo.getAbsolutePath());

            // 保存记录
//            FaithApplication.app().getConfig().saveCustom(mCustomVideo);
            CommonUtil.scanPhoto(mAct, toVideo);
      /*ImageUtils.scanPhoto(getContext(), toJpg);*/
            showToast("保存成功");

//            MobclickAgent.onEvent(getContext(), "click_save");// 保存到相册事件统计
        }
    }
}
