package ren.helloworld.wv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ren.helloworld.wv.helper.WvNotify;
import ren.helloworld.wv.logic.WvFindUserRunnable;
import ren.helloworld.wv.logic.WvForwardRunnable;

/**
 * 在这个服务中应该启动两个线程
 * 1.视频替换及转发线程
 * 2.获取当前使用的Video目录的线程
 * <p/>
 * 这两个线程对同步对象块进行操作
 */
public class WvService extends Service {
  private Thread mForwardThread;
  private Thread mFindUserThread;

  public WvService() {
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (mFindUserThread == null) {
      mFindUserThread = new Thread(WvFindUserRunnable.getInstance());
    }

    if (mForwardThread == null) {
      mForwardThread = new Thread(WvForwardRunnable.getInstance());
    }

    if (!mFindUserThread.isAlive() || mFindUserThread.isInterrupted()) {
      WvFindUserRunnable.getInstance().start();
      mFindUserThread.setName("find-cur-user");
      mFindUserThread.start();
    }

    if (!mForwardThread.isAlive() || mForwardThread.isInterrupted()) {
      WvForwardRunnable.getInstance().start();
      mFindUserThread.setName("find-forward-shot-latest");
      mForwardThread.start();
      startForeground(0x87562, WvNotify.getInstance().normal(false, true));
    }

    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    // 释放资源
    mForwardThread = null;
    mFindUserThread = null;
    WvForwardRunnable.getInstance().stop();
    WvFindUserRunnable.getInstance().stop();
    WvNotify.getInstance().normal(true, false);

    super.onDestroy();
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
  }
}
