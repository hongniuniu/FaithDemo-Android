package ren.helloworld.wv.logic;

import java.io.File;

/**
 * Created by dafan on 2016/3/1 0001.
 * 获取微信用户Video目录的线程和替换转发视频及用户拍摄线程的通信桥梁<p/>
 * {@link WvFindUserRunnable} 负责找出当前手机上所有登录过的微信账号中最新的一个账号（的路径）<p/>
 * {@link WvForwardRunnable} 利用{@link WvFindUserRunnable}找到的账号路径扫描视频文件
 */
public class WvBridge {
  protected File curVideoDir;
  protected static WvBridge instance;

  public static WvBridge getInstance() {
    if (instance == null) {
      synchronized (WvBridge.class) {
        if (instance == null) instance = new WvBridge();
      }
    }
    return instance;
  }

  /**
   * 获取路径
   *
   * @throws InterruptedException
   */
  public synchronized File getCurVideoDirPath() throws InterruptedException {
    if (curVideoDir != null && curVideoDir.exists() && curVideoDir.isDirectory()) {
      return curVideoDir;
    } else {
      wait();
      return curVideoDir;
    }
  }

  /**
   * 存储路径
   *
   * @throws InterruptedException
   */
  public synchronized void setCurVideoDirPath(File file) throws InterruptedException {
    if (file != null && file.exists() && file.isDirectory()) {
      this.curVideoDir = file;
      notifyAll();
    }
  }
}
