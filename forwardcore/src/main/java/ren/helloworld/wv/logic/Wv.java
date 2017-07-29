package ren.helloworld.wv.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import ren.helloworld.wv.helper.WvNotify;
import ren.helloworld.wv.helper.WvUtils;
import ren.helloworld.wv.service.WvService;

/**
 * Created by dafan on 2016/2/29 0029.
 */
public class Wv {
  private static Context context;

  /**
   * 初始化
   */
  public static void init(Context cxt, Class<? extends Activity> cls) {
    context = cxt;
    WvForwardRunnable.init(cxt);
    WvFindUserRunnable.init(cxt);
    WvNotify.init(cxt, cls);
  }

  /**
   * 开启视频转发功能
   */
  public static void start() {
    if (Wv.isRunning()) return;

    WvUtils.loge("服务正在开启中");
    context.startService(new Intent(context, WvService.class));
  }

  /**
   * 关闭视频转发功能
   */
  public static void stop() {
    if (!Wv.isRunning()) return;

    WvUtils.loge("服务即将被停止");
    context.stopService(new Intent(context, WvService.class));
  }

  /**
   * 检测转发服务是否开启了
   */
  public static boolean isRunning() {
    if (WvForwardRunnable.getInstance() == null) return false;
    if (WvFindUserRunnable.getInstance() == null) return false;
    return WvFindUserRunnable.getInstance().isRunning() && WvForwardRunnable.getInstance()
        .isRunning();
  }
}
