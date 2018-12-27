package com.faith.fd.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.faith.fd.utils.DensityUtil;

/**
 * 仿qq塞红包动画
 * @autor hongbing
 * @date 2017/8/7
 */
public class FillRedPacketAnimView extends View implements Runnable{

    private static final String TAG = "FillRedPacketAnimView";

    private Context mContext;
    private int mScreenW;
    private float mOvalW,mOvalH;
    private float mOvalL,mOvalT;
    private Handler mHandler = new Handler();
    private Paint mPaint;
    private Paint mLinePaint;

    public FillRedPacketAnimView(Context context) {
        this(context,null);
    }

    public FillRedPacketAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mScreenW = context.getResources().getDisplayMetrics().widthPixels;
        mOvalL = 10;
        mOvalT = 0;
        mOvalW = 20;
        mOvalH = 15;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setAlpha(80);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(DensityUtil.dp2px(mContext,2));
        mLinePaint.setColor(Color.BLUE);

        setBackgroundColor(Color.RED);

        mHandler.postDelayed(this, 200);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                result = DensityUtil.dp2px(mContext, 18);
                break;
        }
        setMeasuredDimension(widthMeasureSpec, result);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw -->mOvalL = " + mOvalL);

        canvas.drawLine(0,getHeight() / 2,mScreenW,getHeight() / 2,mLinePaint);

        if (Build.VERSION.SDK_INT >= 21) {
            canvas.drawOval(mOvalL, getHeight() / 2 + mOvalT, mOvalL + mOvalW, getHeight() / 2 + mOvalT + mOvalH, mPaint);
        } else {
            RectF rectF = new RectF(mOvalL, mOvalT, mOvalL + mOvalW, mOvalT + mOvalH);
            canvas.drawOval(rectF, mPaint);
        }
    }

    @Override
    public void run() {
        if (mOvalL >= mScreenW) {
            mOvalL = 10;
        } else {
            mOvalL = mOvalL + 8;
        }
        invalidate();
        mHandler.postDelayed(this,80);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mHandler != null){
            mHandler.removeCallbacks(this);
        }
    }
}
