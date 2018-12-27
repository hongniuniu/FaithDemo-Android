package com.faith.fd.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;
import com.faith.fd.bean.CustomVideo;
import com.faith.fd.bean.Puzzlebean;
import com.faith.fd.bean.VideoItem;
import com.faith.fd.imageloader.GlideImageLoader;
import com.faith.fd.utils.CommonUtil;
import com.faith.fd.utils.DensityUtil;
import com.faith.fd.utils.FileUtil;
import com.faith.fd.utils.UiHelper;
import com.faith.fd.utils.VideoProgressUtils;
import com.faith.fd.utils.WaterMarkUtils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.xiaopo.flying.puzzle.Border;
import com.xiaopo.flying.puzzle.ImgVideoPuzzleView;
import com.xiaopo.flying.puzzle.PuzzlePiece;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;
import vn.tungdx.mediapicker.utils.MediaUtils;

/**
 * @autor hongbing
 * @date 2017/7/27
 */
public class CustomActivity extends BaseActivity implements ImgVideoPuzzleView.OnPieceSelectedListener{

    private static final String TAG = "CustomActivity";
    
    private static final int SELECT_IMG = 0X001; // 选择图片
    public static final int CUT_VIDEO = 0X002; // 裁剪视频
    private static final int SELECT_VIDEO = 0x003; // 选择视频

    private ImgVideoPuzzleView mIvv;
    private RelativeLayout rlContent; // 被保存被bitmao的视图对象
    private TextView mTvMark;
    private Button mBtn;

    private ImagePicker imagePicker;
    private int handlingIndex = -1; // 正在处理的border的索引
    private List<Puzzlebean> mPuzzlebeen;
    private ProgressDialog mProgressDialog;
    /**
     * 当前是哪种方式显示
     */
    private boolean mCurIsHor = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_custom;
    }

    @Override
    protected void initView() {
        mPuzzlebeen = new ArrayList<>();
        // 制作视频的进度条对话框
        mProgressDialog = UiHelper.createVideoProgressDialog(mAct);
        rlContent = (RelativeLayout) findViewById(R.id.rl_content);
        mIvv = (ImgVideoPuzzleView) findViewById(R.id.id_ivv);
        mIvv.setSelectedBorderColor(Color.BLUE);
        mIvv.setSelectedListener(this);
        for (int i = 0; i < mIvv.getBorderSize(); i++) {
            mPuzzlebeen.add(Puzzlebean.defa(i));
            mIvv.addPiece(BitmapFactory.decodeResource(getResources(), R.mipmap.template_default_img));
        }
        mIvv.setBackgroundResource(R.mipmap.wx_red_package_dialog_bg);
        mTvMark = (TextView) findViewById(R.id.tv_show_watermark);
        mBtn = (Button) findViewById(R.id.id_bgBtn);
        mBtn.setText("导入图片");
    }

    @Override
    protected void initData() {
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

    /**
     * 查找视频数量
     * @return
     */
    private List<VideoItem> findVideoList() {
        List<VideoItem> list = new ArrayList<>();
        for (int i = 0; i < mIvv.getBorderSize(); i++) {
            Puzzlebean puzzlebean = mPuzzlebeen.get(i);
            if (puzzlebean.getType() == Puzzlebean.TYPE_VIDEO) {
                puzzlebean.getVideoItem().setIndex(puzzlebean.getIndex());
                list.add(puzzlebean.getVideoItem());
            }
        }
        return list;
    }

    public void onCompoundClk(View v) {
        handlingIndex = mIvv.findHandlingPieceIndex();
        if (handlingIndex < 0) {
            showToast("连根毛都没有，没法合成...");
            return;
        }
        // 是否不包含视频
        final boolean noVideo = findVideoList().isEmpty();
        String baseImageName = CommonUtil.getMD5("" + System.currentTimeMillis()) + ".jpg";
        final File baseImage =
                new File(noVideo ? FileUtil.SD_CUSTOM_IMAGE_DIR() : FileUtil.SD_CUSTOM_VIDEO_DIR(),
                        baseImageName);
        final File file = new File(FileUtil.SD_CUSTOM_IMAGE_DIR(), baseImageName);
        if (noVideo) { // 不包含视频，直接合成图片
            compoundImg(file);
            return;
        }
        mIvv.save(baseImage, noVideo, new ImgVideoPuzzleView.Callback() {
            @Override
            public void onSuccess() {
                createVideo(baseImage);
            }

            @Override
            public void onFailed() {
                showToast("视频合成失败...");
            }
        });
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

    /**
     * 制作视频
     */
    private void createVideo(final File baseImage) {
        // 视频排序，找出时间最长的视频的时间毫秒数
        List<VideoItem> videoItems = findVideoList();
        Collections.sort(videoItems, new CompareDuration());
        long time = videoItems.get(0).getDuration();

        UiHelper.showCompressDialog(mAct, time, new UiHelper.OnCompressListener() {
            @Override public void onCompress(int compress) {
//                if (mIsMask)// 图片遮罩
//                {
//                    createMaskCmds(baseImage, compress);
//                } else// 模板拼接
//                {
                    createPuzzleCmds(baseImage, compress);
//                }
            }
        });
    }

    private String i2(int i) {
        return i + "" + i;
    }

    /**
     * 获取可见图片在原始图中的RectF
     */
    private RectF findPieceRectF2Frame(int index) {
        //得到块位置
        Border border = mIvv.getBorder(index);
        RectF borderRect = border.getRect();

        PuzzlePiece piece = mIvv.findIndexPiece(index);
        piece.getWidth();//得到原始图片宽
        piece.getHeight();//得到原始图片高

        //得到矩阵的映射RectF
        RectF rectF = new RectF();
        piece.getMatrix().mapRect(rectF);

        //得到图片测量的比例
        Float scale = mIvv.findIndexScale(index);

        //组装新的RectF
        RectF newRectF = new RectF();
        newRectF.left = (borderRect.left - rectF.left) / scale;
        newRectF.top = (borderRect.top - rectF.top) / scale;
        newRectF.right = (borderRect.right - rectF.right) / scale;
        newRectF.bottom = (borderRect.bottom - rectF.bottom) / scale;

        return newRectF;
    }

    // 处理有视频的模块
    private void createPuzzleCmds(File baseImage, int compress) {
        // 执行命令拼接
        List<String> cmds = new ArrayList<>();

        // 全局选项
        cmds.add("-y");// 覆盖输出结果
        cmds.add("-threads");// 线程个数
        cmds.add("8");// 8个线程一起工作

        // filter命令拼接
        StringBuilder sbfilter = new StringBuilder("");
        StringBuilder sbvolume = new StringBuilder("");

        // 循环背景图片
        cmds.add("-loop");
        cmds.add("1");

        // 输入背景图片
        cmds.add("-i");
        cmds.add(baseImage.getAbsolutePath());

        // 获取将要制作的所有视频
        List<VideoItem> videoItems = findVideoList();
        Collections.sort(videoItems, new CompareDuration());
        int videoCount = videoItems.size();

        for (int i = 0; i < videoCount; i++) {
            VideoItem videoItem = videoItems.get(i);
            // 视频在所有块中的位置
            int index = videoItem.getIndex();

            // 输入视频
            cmds.add("-i");
            cmds.add(videoItem.getPath());

            // 模块相对于基图的块区域
            RectF rfBase = mIvv.getBorder(index).getRect();
            Log.d(TAG,"模块区域 = " + rfBase.toString());
            // 缩放倍数
            float sl = mIvv.findIndexScale(index);
//            float sl = 1.f;
            Log.d(TAG,"缩放倍数 = " + sl);

            // 视频区域，用于裁剪、缩放等，模块相对于帧图片的块区域
            RectF rfframe = findPieceRectF2Frame(index);
            Log.d(TAG,"视频区域 = " + rfframe.toString());
            float w = rfframe.right - rfframe.left;
            float h = rfframe.bottom - rfframe.top;
            float l = rfframe.left;
            float t = rfframe.top;

            int vp = i + 1;// video position

            String vtag = "[" + vp + ":v]";
            //对块区域的位置进行修剪
            String crop = "crop=" + w + ":" + h + ":" + l + ":" + t;
            //对图片进行放大或缩小
            String scale = "scale=" + w * sl + ":" + h * sl;

            float ol = rfBase.left;
            float ot = rfBase.top;
            String overlay = "overlay=" + ol + ":" + ot;
            if (i == 0) overlay += ":shortest=1";// 排序后第一个视频的时间最长

            sbfilter.append(vtag);
            sbfilter.append(crop);
            sbfilter.append(",");
            sbfilter.append(scale);
            sbfilter.append("[" + vp + "]");// 输出到【%d】
            sbfilter.append(";");
            if (vp == 1) {
                sbfilter.append("[0:v][" + vp + "]" + overlay);
            } else {
                sbfilter.append("[" + i2(vp - 1) + "][" + vp + "]" + overlay);
            }
            sbfilter.append("[" + i2(vp) + "];");// 输出到【%d】

            String volumeOutput = "[volume" + vp + "]";
            String atag = "[" + vp + ":a]";
            String volume = "volume=" + videoItem.getVoice2() + volumeOutput;
            sbfilter.append(atag);
            sbfilter.append(volume);
            sbfilter.append(";");

            sbvolume.append(volumeOutput);
        }

        // 缩放到320x240，并输出到scale
        String scale =
                "[" + i2(videoCount) + "]scale=" + (mCurIsHor ? "480x360" : "544x960") + "[scale];";
        sbfilter.append(scale);

        // 画最外面的白色边框
    /*String pad = "[scale]pad=width=320:height=240:x=2:y=2:color=white[pad];";
    sbfilter.append(pad);*/

        // 添加水印图片
        if (WaterMarkUtils.isPuzzleOpen()) {
            String watermarkPath = WaterMarkUtils.getWatermarkPath(false, mTvMark);
            // 配置水印位置
//            String watermark = "movie="
//                    + watermarkPath
//                    + "[wm];[wm]scale=iw/2:-1[wm2];[scale][wm2]overlay=main_w-overlay_w-10:main_h-overlay_h-10[result];";
            String watermark = "movie="
                    + watermarkPath
                    + "[wm];[wm]scale=iw/2:-1[wm2];[scale][wm2]overlay=main_w / 2-overlay_w / 2:main_h-overlay_h-10[result];";
            sbfilter.append(watermark);
        }

        // 合并所有视频的声音文件，以最长的声音为末尾
        String sbAmixFilter =
                sbvolume + "amix=inputs=" + videoCount + ":duration=longest:dropout_transition=0";
        sbfilter.append(sbAmixFilter);

        cmds.add("-filter_complex");
        cmds.add(sbfilter.toString());

        // 视频流映射
        cmds.add("-map");
        if (WaterMarkUtils.isPuzzleOpen()) {
            cmds.add("[result]");
        } else {
            cmds.add("[scale]");
        }

        cmds.add("-r");// 帧率设置
        cmds.add("25");// 每秒15帧
        cmds.add("-ac");// 声道数
        cmds.add("1");// 单声道
        cmds.add("-b:a");
        cmds.add("32k");
        cmds.add("-b:v");
        cmds.add(compress + "k");//
    /*cmds.add("-acodec");
    cmds.add("aac");*/
    /*cmds.add("-vcodec");
    cmds.add("libx264");*/
        cmds.add("-x264-params");
        cmds.add("threads=20");

        // 最终视频输出路径
        String output = baseImage.getAbsolutePath().replace(".jpg", ".mp4");
        cmds.add(output);
        String[] cmd = cmds.toArray(new String[cmds.size()]);
        System.out.println(Arrays.toString(cmd));
        handleVideo(baseImage, new File(output), cmd);
    }

    @Override
    public void onPieceSelected(PuzzlePiece piece, int index) {
        handlingIndex = index;
        Log.d(TAG,"当前选中border的索引值 = " + index);
        mBtn.setText((index>=0 && index < 2) ? "导入图片" : "导入视频");
    }

    class CompareDuration implements Comparator<VideoItem> {
        @Override public int compare(VideoItem o1, VideoItem o2) {
            long d1 = o1.getDuration();
            long d2 = o2.getDuration();
            if (d1 > d2) {
                return -1;
            } else if (d1 < d2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    // 找出所有视频中时间最长的视频在整个表格中的索引
    private int findLongestVideoIndex() {
        if (findVideoList().isEmpty()) return -1;

        int maxIndex = -1;

        int videoIndex = 0;
        long duration = 0L;
        for (int i = 0; i < mIvv.getBorderSize(); i++) {
            Puzzlebean puzzlebean = mPuzzlebeen.get(i);
            if (puzzlebean.getType() == Puzzlebean.TYPE_VIDEO) {
                long time = puzzlebean.getVideoItem().getDuration();

                videoIndex += 1;
                if (videoIndex == 1) {
                    maxIndex = i;
                    duration = time;
                } else {
                    if (time > duration) {
                        maxIndex = i;
                        duration = time;
                    }
                }
            }
        }
        return maxIndex;
    }

    /**
     * 根据视频处理命令行来处理视频
     *
     * @param image 视频缩略图文件
     * @param video 视频文件
     * @param cmds 视频处理命令行
     */
    private void handleVideo(final File image, final File video, final String[] cmds) {
        try {
            final long duration = mPuzzlebeen.get(findLongestVideoIndex()).getVideoItem().getDuration();
            FFmpeg.getInstance(mAct).execute(cmds, new ExecuteBinaryResponseHandler() {
                @Override public void onStart() {
                    super.onStart();
                    mProgressDialog.setProgress(0);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.show();
                }

                @Override public void onProgress(String message) {
                    super.onProgress(message);
                    Log.d(TAG,message);

                    int progress = VideoProgressUtils.videoProgress(message, duration);
                    if (progress != 0) {
                        mProgressDialog.setProgress(progress);
                        if (mProgressDialog.isIndeterminate()) mProgressDialog.setIndeterminate(false);
                    }
                }

                @Override public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.d(TAG,message);
                    showToast("视频制作成功");
                    Log.d(TAG,"视频地址 = " + video.getAbsolutePath());

                    // 如果是遮罩视频，需要重新生成视频的第一帧图片
//                    if (mIsMask) {
//                        handleVideoFrame(image, video);
//                    } else {
                        forwardVideo(image, video);
//                    }
                }

                @Override public void onFailure(String message) {
                    super.onFailure(message);
                    Log.d(TAG,message);
                    showToast("视频制作失败");
                    mProgressDialog.cancel();
                }

                @Override public void onFinish() {
                    super.onFinish();
//                    if (!mIsMask) {
                        mProgressDialog.setProgress(0);
                        mProgressDialog.cancel();
//                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            showToast("视频制作失败");
        }
    }

    /**
     * 查找指定索引的puzzleBean对象
     */
    private Puzzlebean findPuzzleBean(int index) {
        for (Puzzlebean puzzlebean : mPuzzlebeen) {
            if (puzzlebean.getIndex() == index) return puzzlebean;
        }
        return null;
    }

    private void forwardVideo(File image, File video) {
        CustomVideo customVideo = new CustomVideo();
//        if (mIsMask) {
//            customVideo.setName("遮罩模板");
//        } else {
            customVideo.setName("自定义模板");
//        }
        customVideo.setTime(System.currentTimeMillis());
        customVideo.setCheck(false);
        customVideo.setCustomInfo("预览");
        customVideo.setLocalImagePath(image.getAbsolutePath());
        customVideo.setLocalVideoPath(video.getAbsolutePath());
        customVideo.setOriginal(video.getAbsolutePath());
        customVideo.setId(-1);

//        Bundle bundle = PlayVideoFragment.createForwardBundle(customVideo);
//        mActivity.addFragment(PlayVideoFragment.newInstance(bundle));

        Intent intent = new Intent(mAct, PlayVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", customVideo);
        intent.putExtra("bundle", bundle);
        startActivity(intent);

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
                replaceImage(item.path);
                handlingIndex = mIvv.findHandlingPieceIndex();
                Log.d(TAG,"容器索引 = " + handlingIndex);
                mPuzzlebeen.remove(findPuzzleBean(handlingIndex));
                mPuzzlebeen.add(Puzzlebean.iamge(handlingIndex, item));
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
                Intent intent = new Intent(this, CutVideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data",videoItem);
                bundle.putBoolean("show_edit",true);
                bundle.putBoolean("isforward",true);
                intent.putExtra("budle",bundle);
                startActivityForResult(intent, CUT_VIDEO);
            }
        }
        // 裁剪视频结果
        else if (requestCode == CUT_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {

                VideoItem videoItem = data.getParcelableExtra("data");
                replaceImage(videoItem.getFrame());

                handlingIndex = mIvv.findHandlingPieceIndex();
                mPuzzlebeen.remove(findPuzzleBean(handlingIndex));
                mPuzzlebeen.add(Puzzlebean.video(handlingIndex, videoItem));
            }
        }
    }
}
