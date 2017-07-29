package ren.helloworld.wv.helper;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * Created by dafan on 2016/2/23 0023.
 */
public class WvUtils {
  /**
   * 判断手机是否有SD卡
   */
  public static boolean hasSdcard() {
    return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
  }

  /**
   * 获取SD卡根路径
   */
  public static String getSDRoot() {
    return Environment.getExternalStorageDirectory().getAbsolutePath();
  }

  /**
   * 获取手机外置SD卡的根目录
   */
  public static String getExternalSDRoot() {
    Map<String, String> evn = System.getenv();
    return evn.get("SECONDARY_STORAGE");
  }

  /**
   * 获取微信根目录
   */
  public static String getWxRootPath() {
    String wxroot =
        getSDRoot() + File.separator + "tencent" + File.separator + "MicroMsg" + File.separator;
    File file = new File(wxroot);
    if (file.exists() && file.isDirectory()) return wxroot;
    loge("SD卡目录没有 tencent 目录或者 MicroMsg 目录");
    return null;
  }

  /**
   * 获取本机所有的微信账号目录
   */
  public static File[] getWxUserDirs() {
    String wxRootPath = getWxRootPath();
    if (TextUtils.isEmpty(wxRootPath)) return null;

    File[] files = new File(getWxRootPath()).listFiles(new FileFilter() {
      @Override public boolean accept(File pathname) {
        String name = pathname.getName();
        if (name.matches("[a-fA-F0-9]{32}")) {
          logi("用户目录：" + name);
          return true;
        }
        return false;
      }
    });

    return files;
  }

  /**
   * 文件重命名
   */
  public static boolean reNamePath(String oldName, String newName) {
    File f = new File(oldName);
    return f.renameTo(new File(newName));
  }

  /**
   * 日志打印
   */
  public static void logi(String string) {
    if (WvConfig.ISDEGBUG) Log.i(WvConfig.LOGNAME, string + "");
  }

  /**
   * 错误日志打印
   */
  public static void loge(String string) {
    if (WvConfig.ISDEGBUG) Log.e(WvConfig.LOGNAME, string + "");
  }

  /**
   * 异常输出
   */
  public static void logex(Exception e) {
    if (WvConfig.ISDEGBUG) e.printStackTrace();
  }

  /**
   * 提示
   */
  public static void toast(Context context, String msg) {
    Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
  }

  /**
   * 线程休眠，单位为毫秒
   *
   * @param time 毫秒时间
   */
  public static void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch (Exception e) {
      WvUtils.logex(e);
    }
  }

  /**
   * 判断手机是否是Android5.0系统以上
   */
  public static boolean isAboveAndroid5() {
    boolean bool = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    logi("当前手机系统版本是否是5.0以上系统：" + (bool ? "是" : "否"));
    return bool;
  }

  /**
   * 使用文件通道的方式复制文件
   *
   * @param from 源文件
   * @param to 复制到的新文件
   */

  public static void fileChannelCopy(File from, File to) {
    FileInputStream fi = null;
    FileOutputStream fo = null;
    FileChannel in = null;
    FileChannel out = null;
    try {
      fi = new FileInputStream(from);
      fo = new FileOutputStream(to);
      in = fi.getChannel();//得到对应的文件通道
      out = fo.getChannel();//得到对应的文件通道
      in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
    } catch (Exception e) {
      WvUtils.logex(e);
    } finally {
      try {
        fi.close();
        in.close();
        fo.close();
        out.close();
      } catch (Exception e) {
        WvUtils.logex(e);
      }
    }
  }

  /*
 * Java文件操作 获取不带扩展名的文件名
 *
 *  Created on: 2011-8-2
 *      Author: blueeagle
 */
  public static String getFileNameNoEx(String filename) {
    if ((filename != null) && (filename.length() > 0)) {
      int dot = filename.lastIndexOf('.');
      if ((dot > -1) && (dot < (filename.length()))) {
        return filename.substring(0, dot);
      }
    }
    return filename;
  }
}
