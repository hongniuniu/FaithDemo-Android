package com.faith.fd.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

/**
 * 仿滴滴等车倒计时
 * 参考链接：http://blog.csdn.net/wangrain1/article/details/73287908
 * @autor hongbing
 * @date 2017/7/17
 */
public class DiDiView extends View {

    /**
     * 重要函数讲解：
     * getPosTan：
     * 参数1：距离(相对于绘制起点的距离)
     * 参数2：距离起点的坐标点
     * 参数3：正弦函数tan@角度
     *
     * getSegment：
     * 参数1：开始的距离
     * 参数2：结束的距离
     * 参数3：返回的路径
     * 参数4：起始点是否使用moveTo用于保证截取的path第一个位置点不变
     *
     */

    private static final String TAG = "DiDiView";
    private Paint mPaint;
    private int w,h; // 屏幕宽高
    private Path mPathCicle; // 外圈的圆环 路径
    private PathMeasure mPathMeasure; // 测量的类
    private Path mWorkePath; // 做过的圆环路径
    private float mPathLength; // 路径的总长度
    private float xy[]; // 圆的x，y坐标--也就是球心的坐标
    private long time = 0; // 时间
    private String mOutText; // 倒计时的文本

    public DiDiView(Context context) {
        this(context,null);
    }

    public DiDiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        w = wm.getDefaultDisplay().getWidth();
        h = wm.getDefaultDisplay().getHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 抗锯齿
        mPaint.setDither(true); // 防抖动
        // 初始化path
        initPath();
    }

    private void initPath() {
        mPathCicle = new Path();
        RectF rectF = new RectF(-300,-300,300,300);
        mPathCicle.addArc(rectF,270,359.9f); // 第二个参数不能填360，不然设置起始角度无效
        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(mPathCicle,false);
        mWorkePath = new Path();
        mPathLength = mPathMeasure.getLength();
        Log.d(TAG,"路径总长度 = " + mPathLength);
        xy = new float[2];
        Log.d(TAG,"初始化：xy[0] = " + xy[0] + "-->xy[1] = " + xy[1]);
        initAnimation();
    }

    /**
     * 动画
     */
    private void initAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0,mPathLength);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currLength = (float) animation.getAnimatedValue();
                // 获取当前路径长度的终点
                mPathMeasure.getPosTan(currLength,xy,null);
                // 截取路径
                mPathMeasure.getSegment(0,currLength,mWorkePath,true);
                // 获取动画时长
                time = animation.getDuration() - animation.getCurrentPlayTime();
                if(time > 0){
                    mOutText = "00:0" + (time / 1000 + 1);
                }else{
                    mOutText = "00:00";
                }
                postInvalidate();
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(8 * 1000);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1.移动画布圆心
        canvas.translate(w / 2,h / 2 - 150);

        //2.固定圆环
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.parseColor("#f5dcc0"));
        canvas.drawCircle(0,0,300,mPaint);

        //3.绘制走过的路径
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(9);
        mPaint.setColor(Color.parseColor("#f4bf69"));
        canvas.drawPath(mWorkePath,mPaint);

        //4.绘制移动的圆
        mPaint.setColor(Color.parseColor("#f19734"));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(xy[0],xy[1],50,mPaint);

        //5.绘制移动的时间
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(35);
        Log.d(TAG,"xy[0] = " + xy[0] + "-->xy[1] = " + xy[1]);
        canvas.drawText(mOutText,xy[0] - 39,xy[1] + 10,mPaint);
    }
}
