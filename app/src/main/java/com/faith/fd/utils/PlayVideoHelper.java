package com.faith.fd.utils;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faith.fd.R;
import com.faith.fd.bean.CustomVideo;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;


/**
 * Created by dafan on 2016/5/27 0027.
 */
public class PlayVideoHelper implements UniversalVideoView.VideoViewCallback {
  private static final String TAG = "VideoView";
  private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

  private int mSeekPosition;
  private int mCachedHeight;
  private TextView mTvWaterMark;
  private View mVideoRootLayout;
  private UniversalVideoView mUvvVideoView;
  private UniversalMediaController mUmController;
  private CustomVideo customeView;

  public PlayVideoHelper(View view) {
    mVideoRootLayout = view.findViewById(R.id.uvv_root_layout);
    mUvvVideoView = (UniversalVideoView) view.findViewById(R.id.uvv_video_view);
    mUmController = (UniversalMediaController) view.findViewById(R.id.uvv_media_controller);

		/*mUvvVideoView.setZOrderOnTop(true);*/
    mUvvVideoView.setVideoViewCallback(this);
    mUvvVideoView.setMediaController(mUmController);
  }

  public UniversalVideoView getVideoView() {
    return mUvvVideoView;
  }

  public UniversalMediaController getController() {
    return mUmController;
  }

  /**
   * 播放视频，设置标题
   */
  public void play(String name, String path) {

//    setVideoAreaSize(path);


    mUmController.setTitle(name);
    mUvvVideoView.setVideoPath(path);
    mUvvVideoView.requestFocus();
    mUvvVideoView.start();
  }

  /**
   * @param waterMarkText
   */
  public void showWaterMark(String waterMarkText) {
    if (mTvWaterMark == null) {
      mTvWaterMark = (TextView) mVideoRootLayout.findViewById(R.id.tv_show_watermark);
    }
    if (StringUtils.isEmpty(waterMarkText)) {
      mTvWaterMark.setText("再歪一点");
    } else {
      mTvWaterMark.setText(waterMarkText);
    }
    if (mTvWaterMark.getVisibility() != View.VISIBLE) mTvWaterMark.setVisibility(View.VISIBLE);
  }

  public TextView getTvWaterMark() {
    if (mTvWaterMark == null) {
      mTvWaterMark = (TextView) mVideoRootLayout.findViewById(R.id.tv_show_watermark);
    }
    return mTvWaterMark;
  }

  public int getWidth() {
    return mUvvVideoView.getMeasuredWidth();
  }

  /**
   * 在播放过程中的重新播放
   */
  public void rePlay(String name, String path) {
    mUvvVideoView.closePlayer();
    play(name, path);
  }

  /**
   * 设置视频区域大小
   */
  public void setVideoAreaSize(final String path) {
    mVideoRootLayout.post(new Runnable() {
      @Override public void run() {
        int width = mVideoRootLayout.getWidth();
        mCachedHeight = (int) (width * 3f / 4f);
        //  mCachedHeight = (int) (width * 9f / 16f);
        //  mCachedHeight = (int) (width * 405f / 720f);
        ViewGroup.LayoutParams videoLayoutParams = mVideoRootLayout.getLayoutParams();
        videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoLayoutParams.height = mCachedHeight;
        mVideoRootLayout.setLayoutParams(videoLayoutParams);
        mUvvVideoView.setVideoPath(path);
        mUvvVideoView.requestFocus();
      }
    });
  }

  public void onPause() {
    Log.d(TAG, "onPause ");
    if (mUvvVideoView != null && mUvvVideoView.isPlaying()) {
      mSeekPosition = mUvvVideoView.getCurrentPosition();
      Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
      mUvvVideoView.pause();
    }
  }

  public void recycler() {

  }

  public void onSaveInstanceState(Bundle outState) {
    Log.d(TAG, "onSaveInstanceState Position=" + mUvvVideoView.getCurrentPosition());
    outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
  }

  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    if (savedInstanceState == null) return;
    mSeekPosition = savedInstanceState.getInt(SEEK_POSITION_KEY);
    Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
  }

  @Override public void onScaleChange(boolean isFullscreen) {

  }

  @Override public void onPause(MediaPlayer mediaPlayer) {
    Log.d(TAG, "onPause UniversalVideoView callback");
  }

  @Override public void onStart(MediaPlayer mediaPlayer) {

   /* if (mTvWaterMark == null) {
      return;
    }

    RelativeLayout.LayoutParams layoutParams =
        (RelativeLayout.LayoutParams) mTvWaterMark.getLayoutParams();
    if (customeView.getRepWidth() > customeView.getRepHeight()) {
      layoutParams.width = DensityUtil.dp2px(App.app(), 200);
      Log.d("smile", "200");
    } else {
      layoutParams.width = DensityUtil.dp2px(App.app(), 115);
      Log.d("smile", "115");
    }
    mTvWaterMark.setLayoutParams(layoutParams);*/
  }

  @Override public void onBufferingStart(MediaPlayer mediaPlayer) {
    Log.d(TAG, "onBufferingStart UniversalVideoView callback");
  }

  @Override public void onBufferingEnd(MediaPlayer mediaPlayer) {
    Log.d(TAG, "onBufferingEnd UniversalVideoView callback");
  }

  public void setCustomeView(CustomVideo customeView) {
    this.customeView = customeView;
  }

  public void onDestroy() {
    mUvvVideoView.onDestroy();
  }
}
