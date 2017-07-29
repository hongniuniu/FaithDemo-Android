package com.faith.fd.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.TextView;

import com.faith.fd.R;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.text.DecimalFormat;

/**
 * Created by smile on 16-11-28.
 */

public class UiHelper {

  /**
   * 显示设置声音Dialog
   */
//  public static void showSoundDialog(Activity activity, List<VideoItem> videoList) {
//    BottomSheetDialog soundDialog = new BottomSheetDialog(activity);
//    View view = activity.getLayoutInflater().inflate(R.layout.dialog_sound_view, null, false);
//
//    RecyclerView rvSound = (RecyclerView) view.findViewById(R.id.rv_sound);
//    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
//    rvSound.setLayoutManager(layoutManager);
//
//    SoundAdapter soundAdapter = new SoundAdapter();
//    soundAdapter.setData(videoList);
//    rvSound.setAdapter(soundAdapter);
//
//    soundDialog.setContentView(view);
//    soundDialog.show();
//  }

  /**
   * 显示设置水印Dialog
   */
//  public static void showWatermarkDialog(Activity activity, final boolean isMaterial,
//      final TextView tvWaterMark) {
//    final BottomSheetDialog watermarkDialog = new BottomSheetDialog(activity);
//    View view = activity.getLayoutInflater().inflate(R.layout.dialog_watermark_view, null, false);
//    final EditText mEtWatermark = (EditText) view.findViewById(R.id.et_watermark);
//    TextView tvConfim = (TextView) view.findViewById(R.id.tv_confirm);
//    tvConfim.setVisibility(isMaterial ? View.VISIBLE : View.GONE);
//    tvConfim.setOnClickListener(new View.OnClickListener() {
//      @Override public void onClick(View v) {
//        watermarkDialog.dismiss();
//        String txt = mEtWatermark.getText().toString();
//        if (StringUtils.isEmpty(txt)) txt = "做个视频";
//        tvWaterMark.setText(txt);
//        WaterMarkUtils.saveMaterialWaterMarkText(txt);
//      }
//    });
//
//    final SwitchButton sbWatermark = (SwitchButton) view.findViewById(R.id.sb_watermark);
//    sbWatermark.setVisibility(isMaterial ? View.GONE : View.VISIBLE);
//    sbWatermark.setChecked(WaterMarkUtils.isPuzzleOpen());
//    sbWatermark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (isChecked) {
//          tvWaterMark.setVisibility(View.VISIBLE);
//          tvWaterMark.setText(WaterMarkUtils.getPuzzleWaterMarkText());
//        } else {
//          tvWaterMark.setVisibility(View.GONE);
//        }
//      }
//    });
//
//    mEtWatermark.setText(isMaterial ? WaterMarkUtils.getMaterialWaterMarkText()
//        : WaterMarkUtils.getPuzzleWaterMarkText());
//    mEtWatermark.setSelection(mEtWatermark.length());
//
//    watermarkDialog.setContentView(view);
//    watermarkDialog.show();
//    watermarkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//      @Override public void onDismiss(DialogInterface dialog) {
//        String txt = mEtWatermark.getText().toString();
//        if (StringUtils.isEmpty(txt)) txt = "做个视频";
//        if (!isMaterial) {
//          WaterMarkUtils.setPuzzleWaterMarkText(txt);
//          WaterMarkUtils.setPuzzleOpen(sbWatermark.isChecked());
//          if (sbWatermark.isChecked()) {
//            tvWaterMark.setVisibility(View.VISIBLE);
//            tvWaterMark.setText(WaterMarkUtils.getPuzzleWaterMarkText());
//          }
//        } else {
//          tvWaterMark.setText(txt);
//          WaterMarkUtils.saveMaterialWaterMarkText(txt);
//        }
//      }
//    });
//  }

  public interface OnCompressListener {
    void onCompress(int compress);
  }

  private final static int maxBit = 1000;
  private final static int minBit = 100;

  public static void showCompressDialog(Activity activity, final long time,
      final OnCompressListener listener) {
    View view = activity.getLayoutInflater().inflate(R.layout.layout_compress, null);
    final TextView tvVideoSize = (TextView) view.findViewById(R.id.tv_compress_video_size);
    final TextView tvCancel = (TextView) view.findViewById(R.id.tv_compress_cancel);
    final TextView tvOk = (TextView) view.findViewById(R.id.tv_compress_ok);
    final RangeSeekBar<Integer> rangeSeekBar =
        (RangeSeekBar<Integer>) view.findViewById(R.id.rsb_comprose_video);

    int defScale = 50;
    tvVideoSize.setText("视频大小：" + getVideoSize(time, defScale));

    rangeSeekBar.setSelectedMaxValue(defScale);
    rangeSeekBar.setNotifyWhileDragging(true);
    rangeSeekBar.setOnRangeSeekBarChangeListener(
        new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
          @Override public void onRangeSeekBarValuesChanged(RangeSeekBar<Integer> bar,
              RangeSeekBar.Thumb pressedThumb, Integer minValue, Integer maxValue) {
            tvVideoSize.setText("视频大小：" + getVideoSize(time, maxValue));
          }
        });

    final BottomSheetDialog dialog = new BottomSheetDialog(activity);
    dialog.setContentView(view);
    dialog.show();

    tvCancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dialog.cancel();
      }
    });

    tvOk.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dialog.cancel();
        if (listener != null) {
          int curBit =
              minBit + (int) ((maxBit - minBit) * (rangeSeekBar.getSelectedMaxValue() / 100f));
          listener.onCompress(curBit);
        }
      }
    });
  }

  /**
   * [视频编码率（Kbps为单位）+（音频编码率（Kbps为单位）]/ 8×影片总长度（秒为单位）= 文件大小（MB为单位）
   */
  public static String getVideoSize(long time, int curScale) {
    int curBit = minBit + (int) ((maxBit - minBit) * (curScale / 100f));
    float size = ((curBit + 32) / 8 * (time / 1000L)) / 1024.00f;
    DecimalFormat df = new DecimalFormat("0.00");
    return df.format((double) size) + "MB";
  }

  /**
   * 创建显示视频进度的Dialog
   */
  public static ProgressDialog createVideoProgressDialog(final Context context) {
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setMessage("视频正在制作中...");
    progressDialog.setMax(100);
    progressDialog.setCancelable(false);
    progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消预览",
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            progressDialog.cancel();
            FFmpeg.getInstance(context).killRunningProcesses();
          }
        });
    return progressDialog;
  }
}
