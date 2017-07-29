package com.faith.fd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.faith.fd.FaithApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by dafan on 2016/11/24 0024.
 */

public class WaterMarkUtils {

  private static SharedPreferences mSp =
      FaithApplication.app().getSharedPreferences("config", Context.MODE_PRIVATE);

  private static final String WATERMARK_MD5 = "watermark_md5";
  private static final String WATERMARK_TEXT = "watermark_text";
  private static final String WATERMARK_OPEN = "watermark_open";

  private static final String WATERMARK_MATERIAL_MD5 = "watermark_material_md5";
  private static final String WATERMARK_MATERIAL_TEXT = "watermark_material_text";

  /**
   * 存储水印文字
   */
  public static void setPuzzleWaterMarkText(String text) {
    mSp.edit().putString(WATERMARK_TEXT, text).apply();
  }

  /**
   * 获取水印文字
   */
  public static String getPuzzleWaterMarkText() {
    return mSp.getString(WATERMARK_TEXT, "再歪一点");
  }

  /**
   * 素材相关的水印文字从缓存中获取
   */
  public static String getMaterialWaterMarkText() {
    return mSp.getString(WATERMARK_MATERIAL_TEXT, "再歪一点");
  }

  /**
   * 素材相关的水印文字的存储
   *
   * @param waterMarkText 水印文字内容
   */
  public static void saveMaterialWaterMarkText(String waterMarkText) {
    if (TextUtils.isEmpty(waterMarkText)) waterMarkText = "";
    mSp.edit().putString(WATERMARK_MATERIAL_TEXT, waterMarkText).apply();
  }

  /**
   * 设置拼接水印的显示和隐藏
   */
  public static void setPuzzleOpen(boolean isOpen) {
    mSp.edit().putBoolean(WATERMARK_OPEN, isOpen).apply();
  }

  /**
   * 拼接水印是否显示，默认显示
   */
  public static boolean isPuzzleOpen() {
    return mSp.getBoolean(WATERMARK_OPEN, true);
  }

  /**
   * 获取水印图片的路径
   *
   * @param textView 显示水印的TextView
   * @throws FileNotFoundException
   */
  public static String getWatermarkPath(boolean isMaterial, TextView textView) {
    File saveMark = null;
    String watermarkStr = "";

    if (isMaterial) {// 素材界面的水印
      watermarkStr = getMaterialWaterMarkText();
      saveMark = new File(
          FileUtil.SD_APP_MASK_DIR() + File.separator + "material_" + StringUtils.getMD5(
              watermarkStr) + ".png");
    } else {// 拼接界面的水印
      watermarkStr = getPuzzleWaterMarkText();
      saveMark = new File(
          FileUtil.SD_APP_MASK_DIR() + File.separator + StringUtils.getMD5(watermarkStr) + ".png");
    }

    if (!saveMark.exists()) {
      makeWaterMark(isMaterial, saveMark, textView);
      return saveMark.getAbsolutePath();
    }

    // 检测水印是否被替换了
    byte[] bytes = encryptMD5File(saveMark);
    String newMD5 = Arrays.toString(bytes);
    String oldMD5 = mSp.getString((isMaterial ? WATERMARK_MATERIAL_MD5 : WATERMARK_MD5), "");
    if (!newMD5.equals(oldMD5)) {
      saveMark.delete();
      makeWaterMark(isMaterial, saveMark, textView);
    }
    return saveMark.getAbsolutePath();
  }

  /**
   * 获取TextView的显示样式，并保存为图片
   *
   * @param isMaterial 是不是素材相关的水印
   */
  private static void makeWaterMark(boolean isMaterial, File saveFile, TextView textView) {
    try {
      textView.setDrawingCacheEnabled(true);
      Bitmap bitmap = textView.getDrawingCache();
      FileOutputStream fos = new FileOutputStream(saveFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
      IOUtils.safeClose(fos);
      String md5 = Arrays.toString(encryptMD5File(saveFile));
      mSp.edit().putString(isMaterial ? WATERMARK_MATERIAL_MD5 : WATERMARK_MD5, md5).apply();
      bitmap.recycle();
      textView.setDrawingCacheEnabled(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * MD5加密文件
   */
  private static byte[] encryptMD5File(File file) {
    if (file == null) return null;
    FileInputStream fis = null;
    DigestInputStream digestInputStream;
    try {
      fis = new FileInputStream(file);
      MessageDigest md = MessageDigest.getInstance("MD5");
      digestInputStream = new DigestInputStream(fis, md);
      byte[] buffer = new byte[256 * 1024];
      while (digestInputStream.read(buffer) > 0) ;
      md = digestInputStream.getMessageDigest();
      return md.digest();
    } catch (NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      IOUtils.safeClose(fis);
    }
  }

  /**
   * 给图片打水印
   *
   * @param srcFile the bitmap object you want proecss
   * @return return a bitmap object ,if paramter's length is 0,return null
   */
//  public static void syncWaterMark(final Context context, final File srcFile,
//      final Bitmap watermark, final Bitmap markbitmap) {
//
//    new Thread() {
//      public void run() {
//        Bitmap src = ImageUtils.getBitmapByFile(srcFile);
//        if (src == null || watermark == null) {
//          return;
//        }
//        int w = src.getWidth();
//        int h = src.getHeight();
//        int ww = watermark.getWidth();
//        int wh = watermark.getHeight();
//        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);//创建一个新的和SRC长度宽度一样的位图
//        Canvas cv = new Canvas(newb);
//        if (markbitmap != null) {
//          //cv.drawBitmap(markbitmap, 0, 0, null);
//        }
//        cv.drawBitmap(watermark, w - ww - 10, h - wh - 10, null);//在src的右下角画入水印
//        cv.save(Canvas.ALL_SAVE_FLAG);//保存
//        cv.restore();
//        try {
//          ImageUtils.saveImageToSD(context, srcFile.getAbsolutePath(), newb, 100);
//          ImageUtils.scanPhoto(context, srcFile.getAbsolutePath());// 刷新到相册
//          new Handler(context.getMainLooper()).post(new Runnable() {
//            @Override public void run() {
//              ToastUtils.showToastShort(context, "图片保存成功");
//            }
//          });
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//    }.start();
//  }

//  public static void syncMarkBitmap(final Context context, final File srcFile,
//      final Bitmap watermark) {
//
//    Bitmap src = ImageUtils.getBitmapByFile(srcFile);
//    if (src == null || watermark == null) {
//      return;
//    }
//    int w = src.getWidth();
//    int h = src.getHeight();
//    //create the new blank bitmap
//    Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);//创建一个新的和SRC长度宽度一样的位图
//    Canvas cv = new Canvas(newb);
//    //draw src into
//    cv.drawBitmap(src, 0, 0, null);//在 0，0坐标开始画入src
//    //draw watermark into
//    cv.drawBitmap(watermark, 0, 0, null);//在src的右下角画入水印
//    //save all clip
//    cv.save(Canvas.ALL_SAVE_FLAG);//保存
//    //store
//    cv.restore();//存储
//    try {
//      ImageUtils.saveImageToSD(context, srcFile.getAbsolutePath(), newb, 100);
//      Log.d("smile", "low:" + srcFile.getAbsolutePath());
//
//      ImageUtils.scanPhoto(context, srcFile.getAbsolutePath());// 刷新到相册
//      new Handler(context.getMainLooper()).post(new Runnable() {
//        @Override public void run() {
//          //ToastUtils.showToastShort(context, "图片保存成功");
//        }
//      });
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
}
