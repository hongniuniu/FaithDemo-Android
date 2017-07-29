package ren.helloworld.wv.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ren.helloworld.wv.helper.FileComparator;

/**
 * Created by dafan on 2016/4/18 0018.
 * <p/>
 * 多线程扫描文件夹下最新的mp4文件
 */
public class WvLatestVideoBridge {
  private int mSize = -1;
  private List<File> mLatestVideoFiles;
  private FileComparator mFileComparator;

  public WvLatestVideoBridge(FileComparator comparator) {
    this.mFileComparator = comparator;
  }

  /**
   * 其实是线程个数
   */
  public synchronized void setSize(int size) {
    mSize = size;
    mLatestVideoFiles = new ArrayList<>();
  }

  /**
   * 获取文件夹下最新的文件
   *
   * @throws InterruptedException
   */
  public synchronized File getLatestVideoFile() throws InterruptedException {
    if (mLatestVideoFiles == null || mLatestVideoFiles.size() != mSize) wait();

    // 将空的过滤掉
    ArrayList<File> mLatestVideoFilesNotNull = new ArrayList<>();
    for (File file : mLatestVideoFiles) {
      if (file != null) mLatestVideoFilesNotNull.add(file);
    }

    if (mLatestVideoFilesNotNull.isEmpty()) return null;

    Collections.sort(mLatestVideoFilesNotNull, mFileComparator);
    return mLatestVideoFilesNotNull.get(0);
  }

  /**
   * 将每个切割的列表中最新的视频文件加入到此
   */
  public synchronized void setLatestVideoFile(File latestVideoFile) {
    // 添加的有些是空的，后期要过滤
    mLatestVideoFiles.add(latestVideoFile);
    if (mLatestVideoFiles != null && mLatestVideoFiles.size() == mSize) {
      notifyAll();
    }
  }
}
