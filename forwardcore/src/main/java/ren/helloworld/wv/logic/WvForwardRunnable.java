package ren.helloworld.wv.logic;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ren.helloworld.wv.helper.FileComparator;
import ren.helloworld.wv.helper.ScanType;
import ren.helloworld.wv.helper.ScanVideo;
import ren.helloworld.wv.helper.WvConfig;
import ren.helloworld.wv.helper.WvUtils;

/**
 * Created by dafan on 16-2-26.
 * 对用户需要转发的线程和用户拍摄的小视频进行替换的线程
 */
public class WvForwardRunnable implements Runnable {
  private boolean mIsRun = false;
  private static Context context;
  private static WvForwardRunnable instance;
  private static FileComparator fileComparator;

  protected WvForwardRunnable() {
  }

  public static void init(Context cxt) {
    context = cxt;
  }

  public static WvForwardRunnable getInstance() {
    if (instance == null) {
      synchronized (WvForwardRunnable.class) {
        if (instance == null) {
          instance = new WvForwardRunnable();
          fileComparator = new FileComparator();
        }
      }
    }
    return instance;
  }

  public void stop() {
    mIsRun = false;
    instance = null;
  }

  public void start() {
    mIsRun = true;
  }

  public boolean isRunning() {
    return mIsRun;
  }

  @Override public void run() {
    long startTime = System.currentTimeMillis();

    while (mIsRun) {
      File videoDir = null;
      try {
        videoDir = WvBridge.getInstance().getCurVideoDirPath();
      } catch (InterruptedException e) {
        WvUtils.logex(e);
      }

      if (videoDir == null || !videoDir.exists() || !videoDir.isDirectory()) {
        WvUtils.loge("查找用户目录线程给的用户目录不对");
        continue;
      }

      // 判断当前用户的video文件夹下是否有文件
      if (videoDir.listFiles() == null || videoDir.listFiles().length == 0) {
        WvUtils.loge("查找用户目录线程给的用户目录下没有任何文件");
        continue;
      }
      WvUtils.loge("当前用户目录：" + videoDir.getParentFile().getName());

      // 将用户video文件夹下的文件数组转为列表，方便后期切割
      final List<File> fileList = Arrays.asList(videoDir.listFiles());
      if (fileList == null || fileList.isEmpty() || fileList.size() == 0) {
        WvUtils.loge("查找用户目录线程给的用户目录下没有任何文件");
        continue;
      }

      // 计算配置的线程池的大小
      final int count = fileList.size();
      int temp_1 = count % WvConfig.SCAN_PER_COUNT_FORWARD;
      int temp_2 = count / WvConfig.SCAN_PER_COUNT_FORWARD;
      final int poolSize = temp_1 == 0 ? temp_2 : temp_2 + 1;

      // 最新的转发视频的通信桥梁
      final WvLatestVideoBridge wvLatestForwardVideoBridge =
          new WvLatestVideoBridge(fileComparator);
      // 通信桥梁所连接的通信个数
      wvLatestForwardVideoBridge.setSize(poolSize);

      // 最新的拍摄视频的通信桥梁
      final WvLatestVideoBridge wvLatestShotVideoBridge = new WvLatestVideoBridge(fileComparator);
      // 通信桥梁所连接的通信个数
      wvLatestShotVideoBridge.setSize(poolSize);

      for (int i = 0; i < poolSize; i++) {
        final int iIndex = i;
        new Thread(new Runnable() {
          @Override public void run() {
            String threadName = "find-forward-shot-" + iIndex;
            Thread.currentThread().setName(threadName);

            // 对列表进行切割
            List<File> subFileList = new ArrayList<File>();
            if (iIndex != poolSize - 1) {
              subFileList = fileList.subList(iIndex * WvConfig.SCAN_PER_COUNT_FORWARD,
                  (iIndex + 1) * WvConfig.SCAN_PER_COUNT_FORWARD);
            } else {
              subFileList = fileList.subList(iIndex * WvConfig.SCAN_PER_COUNT_FORWARD, count);
            }
            // WvUtils.loge(threadName + "：线程扫描的文件列表：" + subFileList);

            List<File> subForwardVideoList =
                ScanVideo.getAllVideoFile(subFileList, ScanType.FORWARD);
            if (subForwardVideoList == null || subForwardVideoList.isEmpty()) {
              wvLatestForwardVideoBridge.setLatestVideoFile(null);
              WvUtils.loge(threadName + "：线程没有扫描到可以转发的视频");
            } else {
              Collections.sort(subForwardVideoList, fileComparator);
              File latestVideo = subForwardVideoList.get(0);
              wvLatestForwardVideoBridge.setLatestVideoFile(latestVideo);
              WvUtils.loge(threadName + "：线程扫描到一个最新的可转发视频：" + latestVideo.getName());
            }

            List<File> subShotVideoList = ScanVideo.getAllVideoFile(subFileList, ScanType.SHOT);
            if (subShotVideoList == null || subShotVideoList.isEmpty()) {
              wvLatestShotVideoBridge.setLatestVideoFile(null);
              WvUtils.loge(threadName + "：线程没有扫描到拍摄的视频");
            } else {
              Collections.sort(subShotVideoList, fileComparator);
              File latestVideo = subShotVideoList.get(0);
              wvLatestShotVideoBridge.setLatestVideoFile(latestVideo);
              WvUtils.loge(threadName + "：线程扫描到一个拍摄的视频：" + latestVideo.getName());
            }
          }
        }).start();
      }

      // 获取最新的转发视频
      File latestForwardVideo = null;
      try {
        latestForwardVideo = wvLatestForwardVideoBridge.getLatestVideoFile();
      } catch (InterruptedException e) {
        WvUtils.logex(e);
        continue;
      }
      if (latestForwardVideo == null) {
        continue;
      }
      WvUtils.loge("扫描完成，最新的可转发视频：" + latestForwardVideo.getName());

      // 检测最新的转发视频的缩略图
      File latestForwardThumb =
          new File(videoDir, WvUtils.getFileNameNoEx(latestForwardVideo.getName()) + ".jpg");
      if (latestForwardThumb == null || !latestForwardThumb.exists()) {
        continue;
      }
      WvUtils.loge("扫描完成，最新的可转发视频对应的缩略图：" + latestForwardThumb.getName());

      // 获取最新的拍摄视频
      File latestShotVideo = null;
      try {
        latestShotVideo = wvLatestShotVideoBridge.getLatestVideoFile();
      } catch (InterruptedException e) {
        WvUtils.logex(e);
        continue;
      }
      if (latestShotVideo == null) {
        continue;
      }

      if (latestShotVideo.lastModified() <= startTime) {
        continue;
      }
      WvUtils.loge("扫描完成，最新的拍摄视频：" + latestShotVideo.getName());

      // 检测最新拍摄的小视频的缩略图
      File latestShotThumb = new File(videoDir, latestShotVideo.getName() + ".thumb");
      if (latestShotThumb == null || !latestShotThumb.exists()) {
        WvUtils.loge("最新的拍摄视频的缩略图不存在");
        continue;
      }
      WvUtils.loge("扫描完成，最新的拍摄视频对应的缩略图：" + latestShotThumb.getName());

      latestShotThumb.delete();
      latestShotVideo.delete();

      latestForwardVideo.renameTo(latestShotVideo);
      latestForwardThumb.renameTo(latestShotThumb);

      // 恢复是为了保持记录的存在
      WvUtils.fileChannelCopy(latestShotVideo, latestForwardVideo);
      WvUtils.fileChannelCopy(latestShotThumb, latestForwardThumb);

      startTime = System.currentTimeMillis();
      WvUtils.sleep(WvConfig.TIME);
      System.gc();
    }
  }
}
