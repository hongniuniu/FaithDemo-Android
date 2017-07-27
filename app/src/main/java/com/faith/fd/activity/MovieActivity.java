package com.faith.fd.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;
import com.faith.fd.bean.Puzzlebean;
import com.faith.fd.imageloader.GlideImageLoader;
import com.faith.fd.utils.CommonUtil;
import com.faith.fd.utils.DensityUtil;
import com.faith.fd.utils.FileUtil;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.xiaopo.flying.puzzle.PuzzleLayout;
import com.xiaopo.flying.puzzle.PuzzlePiece;
import com.xiaopo.flying.puzzle.PuzzleUtil;
import com.xiaopo.flying.puzzle.PuzzleView;
import com.xiaopo.flying.puzzle.SquarePuzzleView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频合成测试类
 * @autor hongbing
 * @date 2017/7/25
 */
public class MovieActivity extends BaseActivity implements PuzzleView.OnPieceSelectedListener,PuzzleView.OnReplaceListener{

    public static final int PIECE_SIZE = 3;
    public static final int THEME_ID = 1;
    // 回调参数
    private static final int SELECT_IMG = 0X001; // 选择图片
    private static final int SELECT_VIDEO = 0x003; // 选择视频
    private static final int CUT_VIDEO = 0X002; // 裁剪视频

    private static final String TAG = "MovieActivity";
    private Button bgBtn,compoundClk,txtBtn;
    private TextView mWaterMark; // 水印
    private RelativeLayout rlContent; // 被保存被bitmao的视图对象

    private PuzzleLayout mPuzzleLayout;
    private SquarePuzzleView mPuzzleView;
    private List<Puzzlebean> mPuzzlebeen = new ArrayList<>();
    private ImagePicker imagePicker;
    private int handlingIndex; // 正在处理的view的索引

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initView();
        initData();
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

    private void initView() {
        bgBtn = (Button) findViewById(R.id.id_bgBtn);
        compoundClk = (Button) findViewById(R.id.id_compoundClk);
        txtBtn = (Button) findViewById(R.id.id_txtBtn);
        mWaterMark = (TextView) findViewById(R.id.tv_show_watermark);

        rlContent = (RelativeLayout) findViewById(R.id.rl_content);
        mPuzzleLayout = PuzzleUtil.getPuzzleLayout(PIECE_SIZE,THEME_ID);
        mPuzzleView = (SquarePuzzleView) findViewById(R.id.puzzle_view);
        mPuzzleView.setPuzzleLayout(mPuzzleLayout);
        mPuzzleView.setMoveLineEnable(true);
        mPuzzleView.setExtraSize(100);
        mPuzzleView.setBorderWidth(DensityUtil.dp2px(this,3));
        mPuzzleView.setBorderColor(Color.GREEN);
        mPuzzleView.setSelectedBorderColor(ContextCompat.getColor(this,R.color.color_ff0000));
        mPuzzleView.setReplaceListener(this);
        mPuzzleView.setSelectedListener(this);

        mPuzzleView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= 16) {
                            mPuzzleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mPuzzleView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        int halfDeviceWidth = DensityUtil.getDeviceWidth(mAct) / 2;
                        int marginRight = halfDeviceWidth - mPuzzleView.getWidth() / 2;
                        int tenDp = DensityUtil.dp2px(mAct, 10);
                        RelativeLayout.LayoutParams tvLp =
                                (RelativeLayout.LayoutParams) mWaterMark.getLayoutParams();
                        tvLp.setMargins(tenDp, tenDp, marginRight + tenDp, tenDp);
                        mWaterMark.setLayoutParams(tvLp);
                    }
                });
    }

    private void initData() {
        initImagePicker();
        contralBtnVis(View.GONE);
        mWaterMark.setText("再歪一点");

        // 根据板块的个数填充默认图片
        for (int i = 0; i < mPuzzleLayout.getBorderSize(); i++) {
            mPuzzlebeen.add(Puzzlebean.defa(i));
            // mPuzzleView中必须要添加piece，否则点击没有效果
            mPuzzleView.addPiece(BitmapFactory.decodeResource(getResources(), R.mipmap.template_default_img));
        }
        // 为画布添加背景
        mPuzzleView.setBackgroundResource(R.mipmap.wx_red_package_dialog_bg);

    }

    /**
     * 控制四个按钮的显隐
     * @param visibility
     */
    private void contralBtnVis(int visibility){
        bgBtn.setVisibility(visibility);
        compoundClk.setVisibility(visibility);
        txtBtn.setVisibility(visibility);
    }

    public void onBgClk(View v){
        // 注意配置权限，6.0及以上版本
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, SELECT_IMG);
    }

    public void onTxtClk(View v) {
        showToast("编辑文字...");
    }

    public void onCompoundClk(View v) {
        Log.d(TAG,"开始合成...");
        String baseImageName = CommonUtil.getMD5("" + System.currentTimeMillis()) + ".jpg";
        final File file = new File(FileUtil.SD_CUSTOM_IMAGE_DIR(),baseImageName);
        compoundImg(file);
    }

    public Bitmap createBitmap() {
        //在创建图片的时候将边框隐藏
        mPuzzleView.setNeedDrawBorder(false);
        mPuzzleView.setNeedDrawOuterBorder(false);
        Bitmap bitmap =
                Bitmap.createBitmap(rlContent.getWidth(), rlContent.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        rlContent.draw(canvas);
        int _10dp = DensityUtil.dp2px(mAct, 30);
        RectF rectF = new RectF(_10dp, _10dp, rlContent.getWidth() - _10dp * 2,
                rlContent.getHeight() - _10dp * 2);
        canvas.clipRect(rectF);

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
            Toast.makeText(this,"错误逻辑...",Toast.LENGTH_SHORT).show();
            return;
        }
        int imgW = (int) mPuzzleLayout.getBorder(handlingIndex).getRect().width();
        int imgH = (int) mPuzzleLayout.getBorder(handlingIndex).getRect().height();
        Log.d("容器大小","宽 = " + imgW + "-->高 = " + imgH);
        Glide.with(this).load(imagePath).asBitmap().into(new SimpleTarget<Bitmap>(imgW,imgH) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mPuzzleView.replace(resource);
            }
        });
    }

    @Override
    public void onPieceSelected(PuzzlePiece piece, int index) {
        Log.d(TAG, "onPieceSelected-->piece = + " + piece + "-->index = " + index);
        if (index >= 0) {
            contralBtnVis(View.VISIBLE);
        }
    }

    @Override
    public void onReplace() {
        Log.d(TAG,"onReplace...");
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
                handlingIndex = mPuzzleView.findHandlingPieceIndex();
                Log.d(TAG,"容器索引 = " + handlingIndex);
                replaceImage(item.path);
//                mPuzzlebeen.remove(findPuzzleBean(handlingIndex));
//                mPuzzlebeen.add(Puzzlebean.iamge(handlingIndex, item));
            }
        }
    }

}
