package ren.helloworld.wv.logic;

import android.content.Context;
import android.content.Intent;

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
 * Created by dafan on 2016/3/1 0001.
 * 查找当前微信用户主目录的线程
 * 并将查找出来的结果传递给同步代码块
 * <p/>
 * ---------------------------------------------------------------
 * 2016年4月18日15:43:04
 * <p/>
 * 目前查找微信用户使用的目录的算法是：
 * 找到所有的微信用户目录
 * 找出每一个用户目录中最新的文件
 * 将每个用户目录的最新文件放到集合中，再次找出一个最新文件
 * 这个最新文件的父文件夹就是当前用户正在使用的用户视频目录
 */
public class WvFindUserRunnable implements Runnable {
  private boolean mIsRun = false;
  private boolean mIsSuccess = false;
  private static Context context;
  private static WvFindUserRunnable instance;
  private static FileComparator fileComparator;
  private String mErrorMsg = "没有检测到微信文件目录";

  protected WvFindUserRunnable() {
  }

  public static void init(Context cxt) {
    context = cxt;
  }

  public static WvFindUserRunnable getInstance() {
    if (instance == null) {
      synchronized (WvFindUserRunnable.class) {
        if (instance == null) {
          instance = new WvFindUserRunnable();
          fileComparator = new FileComparator();
        }
      }
    }
    return instance;
  }

  /**
   * 停止扫描线程
   */
  public void stop() {
    mIsRun = false;
    instance = null;
  }

  /**
   * 打开线程循环的开关
   * 注意：这个不是开启循环，只是打开循环的门
   */
  public void start() {
    mIsRun = true;
  }

  /**
   * 当前线程是否在运行中
   */
  public boolean isRunning() {
    return mIsRun;
  }

  /**
   * 发送启动失败的广播
   */
  protected void sendError(String errorMsg) {
    Intent intent = new Intent(WvConfig.START_STATUS_FAILED);
    intent.putExtra("error_msg", errorMsg);
    context.sendBroadcast(intent);
  }

  /**
   * 发送启动成功的广播
   */
  protected void sendSuccess() {
    context.sendBroadcast(new Intent(WvConfig.START_STATUS_SUECCESS));
  }

  @Override public void run() {
    while (mIsRun) {
      System.gc();
      final File[] userDirs = WvUtils.getWxUserDirs();

      // 当前没有登录的微信用户，休眠5秒后在检测
      if (userDirs == null || userDirs.length == 0) {
        System.gc();
        sendError("没有检测到微信用户目录");
        WvUtils.loge(mErrorMsg);
        WvUtils.sleep(5000);
        continue;
      }

      // 当前只有一个微信用户，通过信息渠道传递信息并进行下一次的获取
      // 休眠10秒后继续检测
      if (userDirs.length == 1) {
        try {
          WvBridge.getInstance().setCurVideoDirPath(new File(userDirs[0], "video"));
        } catch (InterruptedException e) {
          WvUtils.logex(e);
        }

        // 通知回调，开启服务成功
        // 成功的状态只汇报一次
        if (!mIsSuccess) {
          mIsSuccess = true;
          sendSuccess();
        }

        System.gc();
        WvUtils.loge("当前只有一个登录的用户" + userDirs[0].getName());
        WvUtils.loge("已经成功查找了一次用户目录，休息15后继续检测");
        WvUtils.sleep(10000);
        continue;
      }

      // 当前手机登录的微信账号不止一个
      int userCount = userDirs.length;
      WvUtils.loge("当前设备登录过的微信用户共有：" + userCount + "个");
      WvFindUserBridge.getInstance().setUserVideoDirCount(userCount);

      // 为每一个用户目录创建一个线程
      // 这个线程会找出每一个用户目录下video文件夹下最新的视频文件
      for (int i = 0; i < userCount; i++) {
        final int iIndex = i;
        new Thread(new Runnable() {
          @Override public void run() {

            final String threadName1 = "find-cur-user-" + iIndex;
            Thread.currentThread().setName(threadName1);

            //取出用户目录
            File userDir = userDirs[iIndex];
            String userDirName = userDir.getName();

            // 并判断该用户是否有video文件夹
            File userVideoDir = new File(userDir, "video");
            if (!userVideoDir.exists() || !userVideoDir.isDirectory()) {
              WvUtils.loge(threadName1 + "--用户目录" + userDirName + "下没有video文件夹");
              WvFindUserBridge.getInstance().setUserLatest(iIndex, null);
              WvUtils.sleep(1000);
              return;
            }

            // 判断该用户目录的video文件夹下是否有文件
            if (userVideoDir.list().length == 0) {
              WvUtils.loge(threadName1 + "--用户的video文件夹" + userDirName + "下没有任何文件");
              WvFindUserBridge.getInstance().setUserLatest(iIndex, null);
              WvUtils.sleep(1000);
              return;
            }

            // 将文件数组转为文件列表，方便后期切割
            final List<File> fileList = Arrays.asList(userVideoDir.listFiles());

            // 计算线程池大小
            final int count = fileList.size();
            int temp_1 = count % WvConfig.SCAN_PER_COUNT_FIND_USER;
            int temp_2 = count / WvConfig.SCAN_PER_COUNT_FIND_USER;
            final int poolSize = temp_1 == 0 ? temp_2 : temp_2 + 1;

            // 创建线程池
            final WvLatestVideoBridge wvLatestVideoBridge = new WvLatestVideoBridge(fileComparator);
            wvLatestVideoBridge.setSize(poolSize);

            for (int j = 0; j < poolSize; j++) {
              final int jIndex = j;
              new Thread(new Runnable() {
                @Override public void run() {
                  String threadName2 = threadName1 + "-[child-" + jIndex + "]";
                  Thread.currentThread().setName(threadName2);

                  List<File> subFileList = new ArrayList<File>();
                  if (jIndex != poolSize - 1) {
                    subFileList = fileList.subList(jIndex * WvConfig.SCAN_PER_COUNT_FIND_USER,
                        (jIndex + 1) * WvConfig.SCAN_PER_COUNT_FIND_USER);
                  } else {
                    subFileList =
                        fileList.subList(jIndex * WvConfig.SCAN_PER_COUNT_FIND_USER, count);
                  }
                  // WvUtils.loge(threadName2 + "：所查找的列表" + subFileList);

                  List<File> subAllVideoFileList =
                      ScanVideo.getAllVideoFile(subFileList, ScanType.ALLMP4);
                  if (subAllVideoFileList == null || subAllVideoFileList.isEmpty()) {
                    wvLatestVideoBridge.setLatestVideoFile(null);
                    WvUtils.loge(threadName2 + "：没有找到任何视频文件");
                  } else {
                    Collections.sort(subAllVideoFileList, fileComparator);
                    File latestVideo = subAllVideoFileList.get(0);
                    wvLatestVideoBridge.setLatestVideoFile(latestVideo);
                    WvUtils.loge(threadName2 + "：找到一个最新的视频文件：" + latestVideo.getName());
                  }
                }
              }).start();
            }
            try {
              File file = wvLatestVideoBridge.getLatestVideoFile();
              WvFindUserBridge.getInstance().setUserLatest(iIndex, file);
            } catch (InterruptedException e) {
              WvUtils.logex(e);
            }
          }
        }).start();
      }

      ArrayList<File> allNewList = null;
      try {
        // 如果上面的线程没有完成的话，这里话停住，直到所有目录获取完毕才会继续往下执行
        allNewList = WvFindUserBridge.getInstance().getAllUserLatest();
      } catch (InterruptedException e) {
        WvUtils.logex(e);
      }
      if (allNewList.isEmpty()) {
        System.gc();
        sendError("请使用微信先转发一个视频才能开启此功能");
        WvUtils.loge("所有的用户目录下都没有视频文件");
        WvUtils.sleep(15000);
        continue;
      }

      // 将空移除，然后进行排序
      ArrayList<File> allNewList2 = new ArrayList<>();
      for (int i = 0; i < allNewList.size(); i++) {
        File file = allNewList.get(i);
        if (file != null) allNewList2.add(file);
      }
      if (allNewList2.isEmpty()) {
        System.gc();
        sendError("请使用微信先转发一个视频才能开启此功能");
        WvUtils.loge("所有的用户目录下都没有视频文件");
        WvUtils.sleep(15000);
        continue;
      }

      // 将每一个用户目录的video文件夹中最新的视频文件进行排序
      Collections.sort(allNewList2, fileComparator);
      File newVideo = allNewList2.get(0);
      WvUtils.loge("所有用户下最新的视频文件: " + newVideo.getName());
      try {
        // 将最新文件的父文件夹就是video的目录传递给同步代码块
        WvBridge.getInstance().setCurVideoDirPath(newVideo.getParentFile());
      } catch (InterruptedException e) {
        WvUtils.logex(e);
      }

      // 通知回调，开启服务成功
      // 成功的状态只汇报一次
      if (!mIsSuccess) {
        mIsSuccess = true;
        sendSuccess();
      }

      System.gc();
      WvUtils.loge("已经成功查找了一次，休息15秒");
      WvUtils.sleep(15000);
    }// while end
  }// run end
}// WvFindUserRunnable end
