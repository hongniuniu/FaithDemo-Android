package ren.helloworld.wv.helper;

import java.io.File;
import java.util.Comparator;

/**
 * Created by dafan on 2016/4/22 0022.
 * 对文件进行降序排列
 */
public class FileComparator implements Comparator<File> {
  @Override public int compare(File f1, File f2) {
    long diff = f1.lastModified() - f2.lastModified();
    if (diff > 0) {
      return -1;
    } else if (diff == 0) {
      return 0;
    } else {
      return 1;
    }
  }
}
