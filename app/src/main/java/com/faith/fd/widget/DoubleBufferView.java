package com.faith.fd.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义view理解双缓冲的定义和优缺点
 * 参考链接：http://mp.weixin.qq.com/s/Xl1ab8qI0mqQrqL6NSn0Jg
 * @autor hongbing
 * @date 2017/6/6
 */
public class DoubleBufferView extends View {

    public DoubleBufferView(Context context) {
        super(context);
    }

    private class Point {
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;
    }

    // 未启用双缓冲
//    private Paint mPaint;
//    private List<Point> mPoints;
//
//    public DoubleBufferView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        setBackgroundColor(Color.WHITE);
//        // 设置抗锯齿和防抖动
//        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(Color.GREEN);
//        mPoints = new ArrayList<>();
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                mPoints.add(new Point((int) event.getX(), (int) event.getY()));
//                break;
//            case MotionEvent.ACTION_UP:
//                invalidate();
//                break;
//        }
//        return true;
//    }
//
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        for (Point point : mPoints){
//            canvas.drawCircle(point.x,point.y,50,mPaint);
//        }
//    }


    // 启动双缓冲区的实例代码
    private Paint mPaint;
    private Canvas mBufferCanvas;
    private Bitmap mBufferBmp;

    public DoubleBufferView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.WHITE);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.GREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                if(mBufferBmp == null){
                    mBufferBmp = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
                    mBufferCanvas = new Canvas(mBufferBmp);
                }
                mBufferCanvas.drawCircle((int)event.getX(),(int)event.getY(),50,mPaint);
                break;
            case MotionEvent.ACTION_UP:
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBufferBmp == null) {
            return;
        }
        canvas.drawBitmap(mBufferBmp, 0, 0, null);
    }


    /**
     * 结论：
     * 优点：
     *  1.在绘制数量较小时，不使用双缓冲，GPU的负荷更低，即绘制性能更高；
     *  2.在绘制数量较大时，使用双缓冲绘图，绘制性能明显高于不使用双缓冲的情况
     * 缺点：
     *  使用双缓冲会增加内存消耗.
      */

}
