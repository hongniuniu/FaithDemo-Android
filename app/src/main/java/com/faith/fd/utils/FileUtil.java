package com.faith.fd.utils;

import android.os.Environment;

import java.io.File;

/**
 * @autor hongbing
 * @date 2017/7/26
 */

public class FileUtil {

    /**
     * 程序在SD的缓存根目录
     */
    public static final File SD_ROOT_DIR() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + "Faith"
                + File.separator);
        if (!file.exists() || !file.isDirectory()) file.mkdirs();
        return file;
    }

    /**
     * 定制图片下载目录
     */
    public static final File SD_CUSTOM_IMAGE_DIR() {
        File file = new File(
                SD_ROOT_DIR().getAbsolutePath() + File.separator + "custom_image" + File.separator);
        if (!file.exists() || !file.isFile()) file.mkdirs();
        return file;
    }

    /**
     * 定制视频下载目录
     */
    public static final File SD_CUSTOM_VIDEO_DIR() {
        File file = new File(
                SD_ROOT_DIR().getAbsolutePath() + File.separator + "custom_video" + File.separator);
        if (!file.exists() || !file.isFile()) file.mkdirs();
        return file;
    }
}
