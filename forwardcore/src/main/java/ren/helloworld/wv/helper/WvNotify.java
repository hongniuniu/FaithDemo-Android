package ren.helloworld.wv.helper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import ren.helloworld.wx_video.R;

/**
 * Created by dafan on 16-2-28.
 */
public class WvNotify {
  private static Context context;
  private static WvNotify instance;
  private static Class<? extends Activity> startClass;
  private static NotificationManager notificationManager;
  private static NotificationCompat.Builder notificationBuilder;

  public static void init(Context cxt, Class<? extends Activity> cls) {
    context = cxt;
    startClass = cls;
  }

  public static WvNotify getInstance() {
    if (instance == null) {
      synchronized (WvNotify.class) {
        if (instance == null) {
          if (context == null) {
            throw new IllegalArgumentException(
                "if you want to calling this,must be init at before");
          }
          instance = new WvNotify();
          initNotify();
          initNotifyService();
        }
      }
    }
    return instance;
  }

  /**
   * 初始化服务
   */
  protected static void initNotifyService() {
    notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  /**
   * 构建基本的通知
   */
  protected static void initNotify() {
    PendingIntent pendingIntent = getPendingIntent(context);
    notificationBuilder = new NotificationCompat.Builder(context);
    notificationBuilder.setWhen(System.currentTimeMillis());
    notificationBuilder.setTicker("做个视频正在运行……");
    notificationBuilder.setContentTitle("做个视频正在运行");
    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
    notificationBuilder.setSmallIcon(R.mipmap.ic_notify_small);
    notificationBuilder.setLargeIcon(
        BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notify_large));
    notificationBuilder.setAutoCancel(false);
    notificationBuilder.setContentIntent(pendingIntent);
    notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
    notificationBuilder.setOngoing(false);
    return;
  }

  /**
   * 跟新通知栏
   *
   * @param isShow 是否立即展示
   * @param isRun 核心服务是否在运行状态
   */
  public Notification normal(boolean isShow, boolean isRun) {
    notificationBuilder.setContentText("视频转发功能已经" + (isRun ? "开启" : "关闭"));
    Notification notification = notificationBuilder.build();
    notification.flags |= Notification.FLAG_NO_CLEAR;
    if (isShow) notificationManager.notify(0x87562, notification);
    return notification;
  }

  /**
   * @param context
   * @return
   */
  private static PendingIntent getPendingIntent(Context context) {
    Intent appIntent = new Intent(context, startClass);
    appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    appIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent contentIntent = PendingIntent.getActivity(WvNotify.context, 688, appIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

		/*PendingIntent contentIntent = null;
    try {
			PackageManager packageManager = context.getPackageManager();
			Intent intent = new Intent();
			intent = packageManager.getLaunchIntentForPackage(context.getApplicationInfo().packageName);
			contentIntent = PendingIntent.getActivity(WvNotify.context, 688, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} catch (Exception e) {
		}*/

    return contentIntent;
  }
}
