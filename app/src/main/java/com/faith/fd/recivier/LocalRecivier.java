package com.faith.fd.recivier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.faith.fd.DoubleBufferActivity;
import com.faith.fd.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @autor hongbing
 * @date 2017/7/12
 */

public class LocalRecivier extends BroadcastReceiver{

    private static final String TAG = "LocalRecivier";
    private static int NOTIFI_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "com.faith.fd.USER_ACTION") {
            Log.d(TAG, "大王派我来巡山...");
            showNotification(context);
        }
    }

    public void showNotification(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, 0, new Intent(context, DoubleBufferActivity.class), 0);
            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setContentIntent(contentIntent)
                    .build();// getNotification()
            mNotifyMgr.notify(NOTIFI_ID, notification);
            NOTIFI_ID++;
        }
    }

}
