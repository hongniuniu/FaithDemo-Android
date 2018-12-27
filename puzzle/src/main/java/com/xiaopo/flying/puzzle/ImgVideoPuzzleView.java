package com.xiaopo.flying.puzzle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义控件：装载图片和视频
 *
 * @autor hongbing
 * @date 2017/7/27
 */
public class ImgVideoPuzzleView extends View {

    private static final String TAG = "ImgVideoView";

    private enum Mode {
        NONE, DRAG, ZOOM, MOVE, SWAP
    }

    private Mode mCurrentMode = Mode.NONE;

    private Context mContext;

    private Paint mBorderPaint; // 默认边框画笔
    private Paint mBitmapPaint; // 位图画笔
    private Paint mSelectedBorderPaint; // 选中的边框画笔

    private float mBorderWidth = 4; // 边框的线宽
    private int w = -1;
    private float wRatio = 1f, hRatio = 0.74f; // 保持正方形1:1比例,宽高比800 / 1080
    private int mScreenW; // 屏幕宽
    private float mViewH; // view的高度
    private float mBaseMaginLft; // 中间块距离左边的数值
    private float mBaseMaginBtm; // 中间块距离底部的数值
    private float mBigBoderMarginTop; // 最大模块距离顶部的数值
    private RectF mOneRectF; // 区域1
    private RectF mTwoRectF; // 区域2
    private RectF mThreeRectF; // 区域3
    private float mExtraSize = 100;
    private List<Border> mBorders = new ArrayList<>();
    private List<Line> mLines = new ArrayList<>();

    private RectF mBorderRect;
    private Border mOuterBorder;
    private RectF mSelectedRect;

    private float mOldDistance;
    private PointF mMidPoint;

    private List<PuzzlePiece> mPuzzlePieces = new ArrayList<>();
    private List<PuzzlePiece> mChangedPieces = new ArrayList<>();
//    private PuzzleLayout mPuzzleLayout;

    private float mDownX;
    private float mDownY;

    private Handler mHandler;
    private boolean mNeedDrawBorder = true; // 是否需要绘制选中border的边框，默认需要
    private boolean mNeedDrawOuterBorder = true; // 是否需要绘制未选中的border的边框
    private boolean mMoveBorderContentEnable = true; // 是否可以移动border内部区域
    private boolean isShowReplace = false;

    private Border mHandlingBorder;
    private PuzzlePiece mHandlingPiece;
    private PuzzlePiece mPreviewHandlingPiece;
    private PuzzlePiece mReplacePiece;

    public ImgVideoPuzzleView(Context context) {
        this(context, null);
    }

    public ImgVideoPuzzleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /**
     * dp转px
     * @param context context
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取屏幕宽度
     * @param context
     * @return
     */
    public static int getScreenW(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    private void init() {
        mHandler = new Handler();
        mScreenW = getScreenW(mContext);
        mViewH = mScreenW * hRatio;
        mBaseMaginLft = mScreenW - mScreenW / 2 - dp2px(mContext, 80);
        mBaseMaginBtm = mViewH - mViewH / 4;
        mBigBoderMarginTop = dp2px(mContext, 12);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setFilterBitmap(true);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.GREEN);
        mBorderPaint.setStrokeWidth(mBorderWidth = dp2px(mContext,3));

        mSelectedBorderPaint = new Paint();

        mSelectedBorderPaint.setAntiAlias(true);
        mSelectedBorderPaint.setStyle(Paint.Style.STROKE);
        mSelectedBorderPaint.setStrokeWidth(mBorderWidth);

        mBorderRect = new RectF();
        mSelectedRect = new RectF();

        mOneRectF = new RectF(dp2px(mContext, 20),
                dp2px(mContext, 40) + mBigBoderMarginTop,
                mScreenW / 4 + dp2px(mContext, 10),
                mBaseMaginBtm - dp2px(mContext, 20));
        mTwoRectF = new RectF(mOneRectF.right,
                mOneRectF.top - dp2px(mContext, 20),
                mOneRectF.right + mOneRectF.width() + mOneRectF.width() * 1 / 3,
                mOneRectF.bottom + dp2px(mContext, 20));
        mThreeRectF = new RectF(mTwoRectF.right,
                mBigBoderMarginTop,
                mScreenW - dp2px(mContext, 20),
                mTwoRectF.bottom + dp2px(mContext, 20));

        syncBorder();
    }

    public void syncBorder(){
        Border lBorder = new Border(mOneRectF);
        mBorders.add(lBorder);
//        Line line = BorderUtil.createLine(lBorder,);


        Border mBorder = new Border(mTwoRectF);
        mBorders.add(mBorder);

        Border rBorder = new Border(mThreeRectF);
        mBorders.add(rBorder);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (w == -1) w = MeasureSpec.getSize(widthMeasureSpec);
        int newW = (int) (w * wRatio);
        int newH = (int) mViewH;
        setMeasuredDimension(newW, newH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(mOneRectF, mPaint);
//        canvas.drawRect(mTwoRectF, mPaint);
//        canvas.drawRect(mThreeRectF, mPaint);

        if(mBorders == null || mBorders.size() <= 0){
            Log.d(TAG,"没有任何东西需要被绘制...");
            return;
        }
        Log.d(TAG,"开始绘制...");

        for (int i = 0; i < getBorderSize(); i++) {
            Border border = getBorder(i);
            if (i >= mPuzzlePieces.size()) {
                break;
            }
            PuzzlePiece piece = mPuzzlePieces.get(i);
            canvas.save();
            canvas.clipRect(border.getRect());
            if (mPuzzlePieces.size() > i) {
                piece.draw(canvas, mBitmapPaint);
            }
            // 绘制边框
            if(mNeedDrawBorder){
                canvas.drawRect(border.getRect(),mBorderPaint);
            }
            canvas.restore();
        }

        //draw selected border
        Log.d(TAG,"mHandlingPiece = " + mHandlingPiece + "-->mCurrentMode = " + mCurrentMode);
        if (mNeedDrawBorder && mHandlingPiece != null && mCurrentMode != Mode.SWAP) {
            Log.d(TAG,"绘制选中border的线条...");
            drawSelectedBorder(canvas, mHandlingPiece);
        }
    }

    public void setSelectedBorderColor(int color) {
        mSelectedBorderPaint.setColor(color);
        invalidate();
    }

    private void drawSelectedBorder(Canvas canvas, PuzzlePiece piece) {
        piece.setSel(true);
        mSelectedRect.set(piece.getBorder().getRect());

        mSelectedRect.left += mBorderWidth / 2f;
        mSelectedRect.top += mBorderWidth / 2f;
        mSelectedRect.right -= mBorderWidth / 2f;
        mSelectedRect.bottom -= mBorderWidth / 2f;

        //绘制四边
        Paint paint = new Paint(mSelectedBorderPaint);
        paint.setStrokeWidth(mBorderWidth * 1.3f);
        canvas.drawRect(mSelectedRect, paint);

        //绘制移动大小的块
        mSelectedBorderPaint.setStyle(Paint.Style.FILL);
//        for (Line line : piece.getBorder().getLines()) {
//            if (mPuzzleLayout.getLines().contains(line)) {
//                if (line.getDirection() == Line.Direction.HORIZONTAL) {
//                    canvas.drawRoundRect(
//                            line.getCenterBound(mSelectedRect.centerX(), mSelectedRect.width(), mBorderWidth,
//                                    line == piece.getBorder().lineTop),
//
//                            mBorderWidth * 2, mBorderWidth * 2, mSelectedBorderPaint);
//                } else if (line.getDirection() == Line.Direction.VERTICAL) {
//                    canvas.drawRoundRect(
//                            line.getCenterBound(mSelectedRect.centerY(), mSelectedRect.height(), mBorderWidth,
//                                    line == piece.getBorder().lineLeft),
//
//                            mBorderWidth * 2, mBorderWidth * 2, mSelectedBorderPaint);
//                }
//            }
//        }
        mSelectedBorderPaint.setStyle(Paint.Style.STROKE);
    }

    public boolean isNeedDrawOuterBorder() {
        return mNeedDrawOuterBorder;
    }

    public void setNeedDrawOuterBorder(boolean needDrawOuterBorder) {
        mNeedDrawOuterBorder = needDrawOuterBorder;
    }

    public boolean isMoveBorderContentEnable() {
        return mMoveBorderContentEnable;
    }

    public void setMoveBorderContentEnable(boolean moveLineEnable) {
        mMoveBorderContentEnable = moveLineEnable;
    }

    public void setOuterBorder(RectF baseRect) {

        PointF one = new PointF(baseRect.left, baseRect.top);
        PointF two = new PointF(baseRect.right, baseRect.top);
        PointF three = new PointF(baseRect.left, baseRect.bottom);
        PointF four = new PointF(baseRect.right, baseRect.bottom);

//        Line lineLeft = new Line(one, three);
//        Line lineTop = new Line(one, two);
//        Line lineRight = new Line(two, four);
//        Line lineBottom = new Line(three, four);

//        mOuterLines.clear();
//
//        mOuterLines.add(lineLeft);
//        mOuterLines.add(lineTop);
//        mOuterLines.add(lineRight);
//        mOuterLines.add(lineBottom);

        mOuterBorder = new Border(baseRect);

        mBorders.clear();
        mBorders.add(mOuterBorder);
    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        mBorderRect.left = getPaddingLeft();
//        mBorderRect.top = getPaddingTop();
//        mBorderRect.right = w - getPaddingRight();
//        mBorderRect.bottom = h - getPaddingBottom();
//
////        if (mPuzzleLayout != null) {
////            mPuzzleLayout.setOuterBorder(mBorderRect);
////            mPuzzleLayout.layout();
////        }
//
//        setOuterBorder(mBorderRect);
//
//
//
//        if (mPuzzlePieces.size() != 0) {
//            for (int i = 0; i < mPuzzlePieces.size(); i++) {
//                PuzzlePiece piece = mPuzzlePieces.get(i);
////                piece.setBorder(mBorders.get(i));
////                piece.getMatrix()
////                        .set(BorderUtil.createMatrix(mBorders.get(i), piece.getWidth(),
////                                piece.getHeight(), mExtraSize));
//                piece.getMatrix()
//                        .set(BorderUtil.createMatrix(piece.getBorder(), piece.getWidth(),
//                                piece.getHeight(), mExtraSize));
//            }
//        }
//        invalidate();
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mMoveBorderContentEnable) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();

//                mHandlingLine = findHandlingLine();
//                if (mHandlingLine != null) {
//                    mCurrentMode = Mode.MOVE;
//                    mChangedPieces.clear();
//                    mChangedPieces.addAll(findChangedPiece());
//
//                    for (int i = 0; i < mChangedPieces.size(); i++) {
//                        mChangedPieces.get(i).getDownMatrix().set(mChangedPieces.get(i).getMatrix());
//                    }
//                } else {
//                    mHandlingPiece = findHandlingPiece();
//                    isShowReplace = true;
//                    if (mHandlingPiece != null) {
//                        mCurrentMode = Mode.DRAG;
//                        mHandlingPiece.getDownMatrix().set(mHandlingPiece.getMatrix());
//
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                isShowReplace = false;
//                                mCurrentMode = Mode.SWAP;
//                                invalidate();
//                            }
//                        }, 1000);
//                    }
//                }

//                mHandlingBorder = findHandlingBorder();

//                if (mHandlingPiece != null && mHandlingPiece.isSel()) {
//                    mCurrentMode = Mode.MOVE;
//                    mChangedPieces.clear();
//                    mChangedPieces.addAll(findChangedPiece());
//
//                    for (int i = 0; i < mChangedPieces.size(); i++) {
//                        mChangedPieces.get(i).getDownMatrix().set(mChangedPieces.get(i).getMatrix());
//                    }
//                } else {
                    mHandlingPiece = findHandlingPiece();
                    isShowReplace = true;
                    if (mHandlingPiece != null) {
                        mCurrentMode = Mode.DRAG;
                        mHandlingPiece.getDownMatrix().set(mHandlingPiece.getMatrix());

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isShowReplace = false;
                                mCurrentMode = Mode.SWAP;
                                invalidate();
                            }
                        }, 1000);
                    }
//                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                mOldDistance = calculateDistance(event);
                mMidPoint = calculateMidPoint(event);

                if (mHandlingPiece != null
                        && isInPhotoArea(mHandlingPiece, event.getX(1), event.getY(1))
                        && mCurrentMode != Mode.MOVE) {
                    mCurrentMode = Mode.ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                switch (mCurrentMode) {
                    case NONE:
                        break;
                    case DRAG:
                        dragPiece(mHandlingPiece, event);
                        break;
                    case ZOOM:
                        zoomPiece(mHandlingPiece, event);
                        break;
                    case MOVE:
//                        moveLine(event);
//                        mPuzzleLayout.update();
                        updatePieceInBorder(event);
                        break;

                    case SWAP:
                        mReplacePiece = findReplacePiece(event);
                        dragPiece(mHandlingPiece, event);

                        Log.d(TAG, "onTouchEvent: replace");
                        break;
                }

                if ((Math.abs(event.getX() - mDownX) > 10 || Math.abs(event.getY() - mDownY) > 10)
                        && mCurrentMode != Mode.SWAP) {
                    mHandler.removeCallbacksAndMessages(null);
                    isShowReplace = false;
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mHandlingBorder = null;
                switch (mCurrentMode) {
                    case DRAG:
                        if (!mHandlingPiece.isFilledBorder()) {
                            moveToFillBorder(mHandlingPiece);
                        }

                        if (mPreviewHandlingPiece == mHandlingPiece
                                && Math.abs(mDownX - event.getX()) < 3
                                && Math.abs(mDownY - event.getY()) < 3) {
                            mHandlingPiece = null;
                        }

                        mPreviewHandlingPiece = mHandlingPiece;
                        break;
                    case ZOOM:
                        if (!mHandlingPiece.isFilledBorder()) {
                            fillBorder(mHandlingPiece);
                            mHandlingPiece.setScaleFactor(0f);
                        }
                        break;

                    case SWAP:
                        if (mHandlingPiece != null && mReplacePiece != null) {
                            Drawable temp = mHandlingPiece.getDrawable();
                            mHandlingPiece.setDrawable(mReplacePiece.getDrawable());
                            mReplacePiece.setDrawable(temp);
                            fillBorder(mHandlingPiece);
                            fillBorder(mReplacePiece);

                            if (mDragReplaceListener != null)
                            //回调拖拽替换的监听
                            {
                                mDragReplaceListener.onDragReplace(mPuzzlePieces.indexOf(mHandlingPiece),
                                        mPuzzlePieces.indexOf(mReplacePiece));
                            }
                        }

                        mHandlingPiece = null;
                        mReplacePiece = null;
                        break;
                }

                mCurrentMode = Mode.NONE;

                mHandler.removeCallbacksAndMessages(null);

                //回调选中图片的监听
                if (mSelectedListener != null
                        && isShowReplace
                        && mHandlingPiece != null
                        && mCurrentMode != Mode.SWAP) {
                    mSelectedListener.onPieceSelected(mHandlingPiece, findHandlingPieceIndex());
                }

                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_UP:

                break;
        }
        return true;
    }

//    private void moveLine(MotionEvent event) {
//        if (mHandlingLine == null) {
//            return;
//        }
//
//        if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
//            mHandlingLine.moveTo(event.getY(), 40);
//        } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
//            mHandlingLine.moveTo(event.getX(), 40);
//        }
//    }

    public int getBorderSize() {
        return mBorders.size();
    }

    public Border getBorder(int index) {
        return mBorders.get(index);
    }

    public void addPiece(final Bitmap bitmap) {
        addPiece(new BitmapDrawable(getResources(), bitmap));
    }

    private float calculateDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    private PointF calculateMidPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    /**
     * 查找正在处理图片的索引
     */
    public int findHandlingPieceIndex() {
        if (mHandlingPiece == null) {
            return -1;
        }
        return mPuzzlePieces.indexOf(mHandlingPiece);
    }

    private boolean isInPhotoArea(PuzzlePiece handlingPhoto, float x, float y) {
        return handlingPhoto.contains(x, y);
    }

    private Border findHandlingBorder(){
        for (Border border : mBorders){
            if(border.getRect().contains(mDownX,mDownY)){
                return border;
            }
        }
        return null;
    }

    public boolean isNeedDrawBorder() {
        return mNeedDrawBorder;
    }

    public void setNeedDrawBorder(boolean needDrawBorder) {
        mNeedDrawBorder = needDrawBorder;
        mHandlingPiece = null;
        mPreviewHandlingPiece = null;
        invalidate();
    }

    private List<PuzzlePiece> findChangedPiece() {
        if (mHandlingBorder == null) return new ArrayList<>();

        List<PuzzlePiece> puzzlePieces = new ArrayList<>();

        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.getBorder().contains(mHandlingBorder)) {
                puzzlePieces.add(piece);
            }
        }

        return puzzlePieces;
    }

    public PuzzlePiece findHandlingPiece() {
        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.contains(mDownX, mDownY)) {
                return piece;
            }
        }
        return null;
    }

    public void addPieces(final List<Bitmap> bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            addPiece(bitmap);
        }

        invalidate();
    }

    public void addPiece(final Drawable drawable) {
        int index = mPuzzlePieces.size();
        if (index >= getBorderSize()) {
            Log.e(TAG, "addPiece: can not add more. the current puzzle layout can contains "
                    + getBorderSize()
                    + " puzzle piece.");
            return;
        }
        Matrix matrix = BorderUtil.createMatrix(getBorder(index), drawable, mExtraSize);
        PuzzlePiece layoutPhoto = new PuzzlePiece(drawable, getBorder(index), matrix);
        mPuzzlePieces.add(layoutPhoto);
        invalidate();
    }

    private void dragPiece(PuzzlePiece piece, MotionEvent event) {
        if (piece != null) {
            piece.getMatrix().set(piece.getDownMatrix());
            piece.getMatrix().postTranslate(event.getX() - mDownX, event.getY() - mDownY);

            piece.setTranslateX(piece.getMappedCenterPoint().x - piece.getBorder().centerX());

            piece.setTranslateY(piece.getMappedCenterPoint().y - piece.getBorder().centerY());
        }
    }

    public void replace(Bitmap bitmap) {
        replace(new BitmapDrawable(getResources(), bitmap));
    }

    public void replace(Drawable bitmapDrawable) {
        if (mHandlingPiece == null) {
            // 没有选中的就默认操作第一个区域块
            mPuzzlePieces.get(0).setDrawable(bitmapDrawable);
            fillBorder(mPuzzlePieces.get(0));
            invalidate();
            return;
        }

        mHandlingPiece.setDrawable(bitmapDrawable);
        fillBorder(mHandlingPiece);

        invalidate();
    }

    private void zoomPiece(PuzzlePiece piece, MotionEvent event) {
        if (piece != null && event.getPointerCount() >= 2) {
            float newDistance = calculateDistance(event);

            piece.getMatrix().set(piece.getDownMatrix());
            piece.getMatrix()
                    .postScale(newDistance / mOldDistance, newDistance / mOldDistance, mMidPoint.x,
                            mMidPoint.y);

            piece.setScaleFactor(piece.getMappedWidth() / piece.getWidth());
            mScaleMap.put(mPuzzlePieces.indexOf(piece), piece.getMappedWidth() / piece.getWidth());
        }
    }

    private PuzzlePiece findReplacePiece(MotionEvent event) {
        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.contains(event.getX(), event.getY()) && piece != mHandlingPiece) {
                return piece;
            }
        }
        return null;
    }

    private Map<Integer, Float> mScaleMap = new HashMap<>();

    public Float findIndexScale(int index) {
        Float aFloat = mScaleMap.get(index);
        return aFloat == null ? 1 : aFloat;
    }

    private void updatePieceInBorder(MotionEvent event) {
        for (PuzzlePiece piece : mChangedPieces) {
            float scale = calculateFillScaleFactor(piece, mOuterBorder);

            if (piece.getScaleFactor() > scale && piece.isFilledBorder()) {
                piece.getMatrix().set(piece.getDownMatrix());

//                if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
                    piece.getMatrix().postTranslate(0, (event.getY() - mDownY) / 2);
//                } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
//                    piece.getMatrix().postTranslate((event.getX() - mDownX) / 2, 0);
//                }
            } else if (piece.isFilledBorder() && (piece.getTranslateX() != 0f
                    || piece.getTranslateY() != 0f)) {
                piece.getMatrix().set(piece.getDownMatrix());

//                if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
                    piece.getMatrix().postTranslate(0, (event.getY() - mDownY) / 2);
//                } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
//                    piece.getMatrix().postTranslate((event.getX() - mDownX) / 2, 0);
//                }
            } else {
                fillBorder(piece);
            }
        }
    }

    private float calculateFillScaleFactor(PuzzlePiece piece) {
        final RectF rectF = piece.getBorder().getRect();
        float scale;
        if (piece.getRotation() == 90 || piece.getRotation() == 270) {
            if (piece.getHeight() * rectF.height() > rectF.width() * piece.getWidth()) {
                scale = (rectF.height() + mExtraSize) / piece.getWidth();
            } else {
                scale = (rectF.width() + mExtraSize) / piece.getHeight();
            }
        } else {
            if (piece.getWidth() * rectF.height() > rectF.width() * piece.getHeight()) {
                scale = (rectF.height() + mExtraSize) / piece.getHeight();
            } else {
                scale = (rectF.width() + mExtraSize) / piece.getWidth();
            }
        }
        return scale;
    }

    private float calculateFillScaleFactor(PuzzlePiece piece, Border border) {
        final RectF rectF = border.getRect();
        float scale;
        if (piece.getWidth() * rectF.height() > rectF.width() * piece.getHeight()) {
            scale = rectF.height() / piece.getHeight();
        } else {
            scale = rectF.width() / piece.getWidth();
        }
        return scale;
    }

    public void flipHorizontally() {
        flipHorizontally(mHandlingPiece, true);
    }

    public void flipVertically() {
        flipVertically(mHandlingPiece, true);
    }

    private void flipHorizontally(PuzzlePiece piece, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setNeedHorizontalFlip(!piece.isNeedHorizontalFlip());
        }
        piece.getMatrix()
                .postScale(-1, 1, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);

        invalidate();
    }

    private void flipVertically(PuzzlePiece piece, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setNeedVerticalFlip(!piece.isNeedVerticalFlip());
        }

        piece.getMatrix()
                .postScale(1, -1, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);

        invalidate();
    }

    private void fillBorder(PuzzlePiece piece) {
        piece.getMatrix().reset();

        final RectF rectF = piece.getBorder().getRect();

        float offsetX = rectF.centerX() - piece.getWidth() / 2;
        float offsetY = rectF.centerY() - piece.getHeight() / 2;

        piece.getMatrix().postTranslate(offsetX, offsetY);
        float scale = calculateFillScaleFactor(piece);

        piece.getMatrix().postScale(scale, scale, rectF.centerX(), rectF.centerY());
        //将测量规模存放到map中
        mScaleMap.put(mPuzzlePieces.indexOf(piece), scale);

        if (piece.getRotation() != 0) {
            rotate(piece, piece.getRotation(), false);
        }

        if (piece.isNeedHorizontalFlip()) {
            flipHorizontally(piece, false);
        }

        if (piece.isNeedVerticalFlip()) {
            flipVertically(piece, false);
        }

        piece.setTranslateX(0f);
        piece.setTranslateY(0f);
        piece.setScaleFactor(0f);
    }

    public void rotate(float rotate) {
        rotate(mHandlingPiece, rotate, true);
    }

    private void rotate(PuzzlePiece piece, float rotate, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setRotation((piece.getRotation() + rotate) % 360f);
        }

        if (needChangeStatus) {
            piece.getMatrix()
                    .postRotate(rotate, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);
            fillBorder(piece);
        } else {
            piece.getMatrix()
                    .postRotate(piece.getRotation(), piece.getMappedCenterPoint().x,
                            piece.getMappedCenterPoint().y);
        }

        invalidate();
    }

    private void moveToFillBorder(PuzzlePiece piece) {
        Border border = piece.getBorder();
        RectF rectF = piece.getMappedBound();
        float offsetX = 0f;
        float offsetY = 0f;

        if (rectF.left > border.left()) {
            offsetX = border.left() - rectF.left;
        }

        if (rectF.top > border.top()) {
            offsetY = border.top() - rectF.top;
        }

        if (rectF.right < border.right()) {
            offsetX = border.right() - rectF.right;
        }

        if (rectF.bottom < border.bottom()) {
            offsetY = border.bottom() - rectF.bottom;
        }

        piece.getMatrix().postTranslate(offsetX, offsetY);

        piece.setTranslateX(border.centerX() - piece.getMappedCenterPoint().x);
        piece.setTranslateY(border.centerY() - piece.getMappedCenterPoint().y);

        if (!piece.isFilledBorder()) {
            fillBorder(piece);
        }
    }

    /**
     * 得到指定位置的图片
     */
    public PuzzlePiece findIndexPiece(int index) {
        return mPuzzlePieces.get(index);
    }

    /***********************************************文件操作模块****************************************************/

    public Bitmap createBitmap() {
        mHandlingPiece = null;
        //先保存之前边框的状态
        boolean borderTemp = mNeedDrawBorder;
        boolean outerBorderTemp = mNeedDrawOuterBorder;

        //在创建图片的时候将边框隐藏
        setNeedDrawBorder(false);
        setNeedDrawOuterBorder(false);
        invalidate();

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        //将边框状态设置为原始状态
        setNeedDrawBorder(borderTemp);
        setNeedDrawOuterBorder(outerBorderTemp);
        return bitmap;
    }

    public void save(File file) {
        save(file, false, 100, null);
    }

    public void save(File file, boolean canScan, Callback callback) {
        save(file, canScan, 100, callback);
    }

    public void save(File file, boolean canScan, int quality, Callback callback) {
        Bitmap bitmap = null;
        FileOutputStream outputStream = null;

        try {
            bitmap = createBitmap();
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            if (!file.exists()) {
                Log.e(TAG, "notifySystemGallery: the file do not exist.");
                return;
            }

            if (canScan) {
                try {
                    MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                            file.getAbsolutePath(), file.getName(), null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                getContext().sendBroadcast(
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }

            if (callback != null) {
                callback.onSuccess();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailed();
            }
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface Callback {
        void onSuccess();

        void onFailed();
    }

    /***********************************************自定义接口回调模块****************************************************/
    //选中
    private OnPieceSelectedListener mSelectedListener;

    public void setSelectedListener(OnPieceSelectedListener selectedListener) {
        mSelectedListener = selectedListener;
    }

    public interface OnPieceSelectedListener {
        void onPieceSelected(PuzzlePiece piece, int index);
    }

    //拖拽替换回调
    private onDragReplaceListener mDragReplaceListener;

    public void setOnDragReplaceListener(onDragReplaceListener onDragListener) {
        mDragReplaceListener = onDragListener;
    }

    public interface onDragReplaceListener {
        void onDragReplace(int handlePic, int replacePic);
    }
}
