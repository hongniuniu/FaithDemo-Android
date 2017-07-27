package com.faith.fd.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;
import com.faith.fd.bean.VideoItem;
import com.faith.fd.imageloader.GlideImageLoader;
import com.faith.fd.utils.CommonUtil;
import com.faith.fd.utils.DensityUtil;
import com.faith.fd.utils.FileUtil;
import com.faith.fd.widget.ImgVideoView;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;
import vn.tungdx.mediapicker.utils.MediaUtils;

/**
 * @autor hongbing
 * @date 2017/7/27
 */
public class CustomActivity extends BaseActivity {

    private static final String TAG = "CustomActivity";
    
    private static final int SELECT_IMG = 0X001; // 选择图片
    private static final int SELECT_VIDEO = 0x003; // 选择图片

    private ImgVideoView mIvv;
    private RelativeLayout rlContent; // 被保存被bitmao的视图对象

    private ImagePicker imagePicker;
    private int handlingIndex = -1; // 正在处理的border的索引

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        rlContent = (RelativeLayout) findViewById(R.id.rl_content);
        mIvv = (ImgVideoView) findViewById(R.id.id_ivv);
        mIvv.setSelectedBorderColor(Color.BLUE);
        for (int i = 0; i < mIvv.getBorderSize(); i++) {
            mIvv.addPiece(BitmapFactory.decodeResource(getResources(), R.mipmap.template_default_img));
        }
        mIvv.setBackgroundResource(R.mipmap.wx_red_package_dialog_bg);
        initData();
    }

    private void initData() {
        initImagePicker();
    }

    /**
     * 初始化imagepicker配置
     */
    private void initImagePicker(){
        imagePicker = ImagePicker.getInstance();
        imagePicker.setCrop(false);
        imagePicker.setMultiMode(false);
        imagePicker.setImageLoader(new GlideImageLoader());
    }

    public void onBgClk(View v){
        handlingIndex = mIvv.findHandlingPieceIndex();
        Log.d(TAG,"当前选择border的索引值 = " + handlingIndex);
        if(handlingIndex < 0){
            showToast("请先选个板块区域...");
            return;
        }
        switch (handlingIndex){
            case 0:
            case 1:// 打开图片
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, SELECT_IMG);
                break;
            default: // 打开视频
                MediaOptions.Builder builder = new MediaOptions.Builder();
                MediaOptions options = builder.selectVideo().canSelectMultiVideo(false).build();
                MediaPickerActivity.open(mAct, SELECT_VIDEO, options);
                break;
        }
    }

    public void onCompoundClk(View v){
        handlingIndex = mIvv.findHandlingPieceIndex();
        if(handlingIndex < 0){
            showToast("连根毛都没有，没法合成...");
            return;
        }
        String baseImageName = CommonUtil.getMD5("" + System.currentTimeMillis()) + ".jpg";
        final File file = new File(FileUtil.SD_CUSTOM_IMAGE_DIR(),baseImageName);
        compoundImg(file);
    }

    public Bitmap createBitmap() {
        //在创建图片的时候将边框隐藏
        mIvv.setNeedDrawBorder(false);
//        mIvv.setNeedDrawOuterBorder(false);
        Bitmap bitmap =
                Bitmap.createBitmap(rlContent.getWidth(), rlContent.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        rlContent.draw(canvas);
        int _10dp = DensityUtil.dp2px(mAct, 30);
        RectF rectF = new RectF(_10dp, _10dp, rlContent.getWidth() - _10dp * 2,
                rlContent.getHeight() - _10dp * 2);
        canvas.clipRect(rectF);

        mIvv.setNeedDrawBorder(true);
        return bitmap;
    }

    /**
     * 合成图片
     * @param imgFile
     */
    private void compoundImg(File imgFile) {
        Bitmap bitmap = null;
        FileOutputStream outputStream = null;
        try {
            bitmap = createBitmap();
            outputStream = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            if (!imgFile.exists()) {
                return;
            }
            // 两个一起使用会存在两个文件,直接用广播通知媒体库更新，显示自己存储的图片就行
//            try {
            // 更新媒体库
//                MediaStore.Images.Media.insertImage(getContentResolver(),
//                        imgFile.getAbsolutePath(), imgFile.getName(), null);
            sendBroadcast(
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgFile)));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            showToast("图片合成成功!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 替换选中的区域块的图片
     * 如果当前没有选中的区域块，则默认替换第一个区域块的图片
     */
    private void replaceImage(String imagePath) {
        // 计算容器宽高，并根据容器大小对图片进行压缩
        if(handlingIndex < 0){
            showToast("请先选个板块区域...");
            return;
        }
        int imgW = (int) mIvv.getBorder(handlingIndex).getRect().width();
        int imgH = (int) mIvv.getBorder(handlingIndex).getRect().height();
        Log.d("容器大小","宽 = " + imgW + "-->高 = " + imgH);
        Glide.with(this).load(imagePath).asBitmap().into(new SimpleTarget<Bitmap>(imgW,imgH) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mIvv.replace(resource);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == SELECT_IMG) {
                ArrayList<ImageItem> images =
                        (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                ImageItem item = images.get(0);
                String path = item.path;
                if (TextUtils.isEmpty(path) || path.endsWith(".gif")) {
                    showToast("不支持此格式的图片");
                    return;
                }
                handlingIndex = mIvv.findHandlingPieceIndex();
                Log.d(TAG,"容器索引 = " + handlingIndex);
                replaceImage(item.path);
//                mPuzzlebeen.remove(findPuzzleBean(handlingIndex));
//                mPuzzlebeen.add(Puzzlebean.iamge(handlingIndex, item));
            }
        }
        // 选择视频结果
        else if (requestCode == SELECT_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<MediaItem> mediaItemList =
                        MediaPickerActivity.getMediaItemSelected(mAct, data);
                if (mediaItemList == null || mediaItemList.isEmpty()) {
                    showToast("沒有任何文件");
                    return;
                }
                MediaItem mediaItem = mediaItemList.get(0);
                VideoItem videoItem = new VideoItem();
                videoItem.setDuration(mediaItem.getDuration());
                videoItem.setPath(MediaUtils.getRealVideoPathFromURI(getContentResolver(),
                        mediaItem.getUriOrigin()));
//                String fName = CutVideoFragment.class.getName();
//                Bundle bundle = CutVideoFragment.createBundle(videoItem);
//                BaseContentActivity.startActivityForFragmentResult(this, true, fName, bundle, CUT_VIDEO);
            }
        }
    }
}
