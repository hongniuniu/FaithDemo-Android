/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package life.knowledge4.videotrimmer.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class TrimVideoUtils {

    private static final String TAG = TrimVideoUtils.class.getSimpleName();

    /**
     * 准备截取视频
     *
     * @throws IOException
     */
    public static void startTrim(Context context, @NonNull File src, @NonNull String dst, long startMs, long endMs, @NonNull OnTrimVideoListener callback) throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String fileName = "MP4_" + timeStamp + ".mp4";
        final String filePath = dst + fileName;

        File file = new File(filePath);
        file.getParentFile().mkdirs();
        Log.d(TAG, "Generated file path " + filePath);

        try {
            trimVideo(context, src, file, startMs, endMs, callback);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError("视频截取失败");
            }
        }
    }

    /**
     * 截取视频
     *
     * @throws IOException
     * @throws FFmpegCommandAlreadyRunningException
     */
    private static void trimVideo(final Context context, @NonNull File src, @NonNull final File dst, final long startMs, final long endMs, @NonNull final OnTrimVideoListener callback) throws IOException, FFmpegCommandAlreadyRunningException {
        double start = startMs / 1000;// 开始截取时间
        double duration = (endMs - startMs) / 1000;// 持续时间

        // 截取视频
        String[] cmds = new String[]{"-ss", start + "", "-t", duration + "", "-i", src.getAbsolutePath(), "-vcodec", "copy", "-acodec", "copy", dst.getAbsolutePath()};
        Log.e(TAG, Arrays.toString(cmds));

        // 执行截取
        FFmpeg.getInstance(context).execute(cmds, new ExecuteBinaryResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onProgress(String message) {
                super.onProgress(message);
                Log.e(TAG, "onProgress :" + message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
                Log.e(TAG, "onSuccess :" + message);
                getVideoFirstFrame(context, dst, startMs, endMs, callback);
            }

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.e(TAG, "onFailure :" + message);
                if (callback != null)
                    callback.onError("视频截取失败");
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    /**
     * 为裁剪后的视频生成一张缩略图
     */
    public static void getVideoFirstFrame(Context context, @NonNull final File video, final long startMs, final long endMs, @NonNull final OnTrimVideoListener callback) {
        String frameName = video.getName().replace(".mp4", "") + ".jpg";
        final File frame = new File(video.getParentFile(), frameName);
        String[] cmds = new String[]{"-i", video.getAbsolutePath(), "-y", frame.getAbsolutePath()};

        try {
            FFmpeg.getInstance(context).execute(cmds, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
          /*Log.e(TAG, "onFailure " + message);*/
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    if (frame.exists() && frame.isFile()) {
                        if (callback != null) {
                            callback.getResult(video.getAbsolutePath(), frame.getAbsolutePath(), endMs - startMs);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("视频缩略图生成失败");
                        }
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
