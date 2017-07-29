package ren.helloworld.wv.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dafan on 2016/4/19 0019.
 */
public class ScanVideo {
  public static List<File> getAllVideoFile(List<File> list, ScanType type) {
    List<File> videoList = new ArrayList<>();
    for (File file : list) {
      String name = file.getName();

      // 查找的是转发的视频
      if (type.name().equals(ScanType.FORWARD.name())) {
        if (name.length() > 20 && name.endsWith(".mp4")) videoList.add(file);
      }

      // 查找的是拍摄视频
      else if (type.name().equals(ScanType.SHOT.name())) {
        if (name.matches("tempvideo\\d+.mp4")) videoList.add(file);
      }

      // 查找的是所有视频
      else {
        if (name.endsWith(".mp4")) videoList.add(file);
      }
    }
    return videoList;
  }
}
