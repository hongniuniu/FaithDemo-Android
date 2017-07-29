package com.faith.fd.utils;

import android.util.Log;

/**
 * Created by dafan on 2016/11/24 0024.
 */

public class VideoProgressUtils {
  private static final String TAG = "VideoProgressUtils";
  public static int videoProgress(String message, long totalTime) {
    if (StringUtils.isEmpty(message)) return 0;

    if (!message.startsWith("frame=")) return 0;

    if (!message.contains("time=")) return 0;

    int start = message.indexOf("time=");
    String time = message.substring(start + 5, start + 16);

    String[] times = time.split(":");
    String[] secondsStr = times[2].split("\\.");

    int hour = Integer.parseInt(times[0]);
    int minutes = Integer.parseInt(times[1]);
    int seconds = Integer.parseInt(secondsStr[0]);
    int milli = Integer.parseInt(secondsStr[1]);

    long duration = hour * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + milli;
    int progress = (int) ((duration * 1f / totalTime) * 100);
    Log.e(TAG,"处理进度：" + progress);

    return progress;
  }
}
