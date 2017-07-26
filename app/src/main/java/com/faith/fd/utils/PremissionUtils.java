package com.faith.fd.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.faith.fd.FaithApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dafan on 2016/10/21 0021.
 */

public class PremissionUtils {
  /**
   * 获取程序注册的所有权限
   */
  private static String[] getAllPermissions() {
    List<String> params;
    List<String> notRequest = new ArrayList<>();
    notRequest.add("android.permission.RECEIVE_USER_PRESENT");
    try {
      String[] permissions = FaithApplication.app()
          .getPackageManager()
          .getPackageInfo("com.faith.fd",
              PackageManager.GET_PERMISSIONS).requestedPermissions;
      params = Arrays.asList(permissions);
      params.removeAll(notRequest);
    } catch (Exception e) {
      return new String[0];
    }
    return params.toArray(new String[0]);
  }

  /**
   * 检测程序运行的权限是否通过
   */
  public static void checkAllPermissions(Activity activity, final Listener listener) {
    if (PermissionsManager.hasAllPermissions(activity, PremissionUtils.getAllPermissions())) {
      listener.onGranted();
      return;
    }
    PermissionsManager.getInstance()
        .requestAllManifestPermissionsIfNecessary(activity, new PermissionsResultAction() {
          @Override public void onGranted() {
            listener.onGranted();
          }

          @Override public void onDenied(String permission) {
            if (permission.equals("android.permission.SYSTEM_ALERT_WINDOW")) {
              onGranted();
            } else {
              listener.onDenied(permission + "\n权限被禁止。");
            }
          }
        });
  }

  public interface Listener {
    void onGranted();

    void onDenied(String permission);
  }
}
