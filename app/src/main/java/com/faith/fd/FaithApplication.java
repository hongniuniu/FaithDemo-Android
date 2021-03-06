package com.faith.fd;

import android.app.Application;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

/**
 * 应用类
 * @autor hongbing
 * @date 2017/7/22
 */
public class FaithApplication extends Application {

    private static FaithApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LeakCanary.install(this);
        if (BuildConfig.DEBUG) { // 如果线程出了问题，控制台会有警告输出，可以定位到代码。
            // 针对线程的相关策略
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());

            // 针对VM的相关策略
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }

    /**
     * 获取引用实例
     */
    public static FaithApplication app() {
        return instance;
    }
}
