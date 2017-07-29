package ren.helloworld.wv.logic;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dafan on 2016/4/18 0018.
 * <p/>
 * 每一个用户目录中的最新的文件的集合
 */
public class WvFindUserBridge {
  private static WvFindUserBridge instance;

  protected int count = 0;
  protected ArrayList<File> allUserLatest = new ArrayList<>();

  protected WvFindUserBridge() {

  }

  public static final WvFindUserBridge getInstance() {
    if (instance == null) {
      synchronized (WvFindUserBridge.class) {
        if (instance == null) {
          instance = new WvFindUserBridge();
        }
      }
    }
    return instance;
  }

  /**
   * 设置当前有几个用户(目录)
   */
  public void setUserVideoDirCount(int count) {
    this.count = count;
    allUserLatest.clear();
    allUserLatest = new ArrayList<>();
  }

  /**
   * 获取所有用户视频目录中每个目录的最新文件的集合
   *
   * @throws InterruptedException
   */
  public synchronized ArrayList<File> getAllUserLatest() throws InterruptedException {
    if (allUserLatest != null && allUserLatest.size() == count) {
      return allUserLatest;
    } else {
      wait();
      return allUserLatest;
    }
  }

  /**
   * 将每一个用户视频文件夹下最新的文件加入到集合中
   */
  public synchronized void setUserLatest(int index, File file) {
    if (index < 0 && index >= count) return;

    allUserLatest.add(file);
    if (allUserLatest != null && allUserLatest.size() == count) notifyAll();
  }
}
