package com.faith.fd.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.faith.fd.R;

import static android.animation.ObjectAnimator.ofFloat;

/**
 * 属性动画知识点介绍
 * @autor hongbing
 * @date 2017/7/21
 */
public class PropAnimActivity extends AppCompatActivity {

    private ImageView mTargeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prop_anim);
        mTargeView = (ImageView) findViewById(R.id.id_img);
        findViewById(R.id.id_translation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translationAnim();
            }
        });
        findViewById(R.id.id_rotation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationAnim();
            }
        });
        findViewById(R.id.id_scale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleAnim();
            }
        });
        findViewById(R.id.id_totalAnim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalAnim();
            }
        });
    }

    /**
     * 水平移动
     */
    private void translationAnim(){
        ObjectAnimator animator = ofFloat(mTargeView,View.TRANSLATION_X,200f,0f,-200f,0f);
        animator.setInterpolator(new BounceInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 旋转动画
     */
    private void rotationAnim(){
        ObjectAnimator animator = ofFloat(mTargeView,View.ROTATION,0,360);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 缩放动画
     */
    private void scaleAnim(){
        ObjectAnimator animator = ofFloat(mTargeView,View.SCALE_X,0f,1.25f,1f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 组合动画
     */
    private void totalAnim(){
        ObjectAnimator tranAnim = ofFloat(mTargeView,View.TRANSLATION_Y,0f,300f);
        ObjectAnimator rotaAnim = ofFloat(mTargeView,View.ROTATION,0,360);
        ObjectAnimator scaleAnim = ofFloat(mTargeView,View.SCALE_Y,0f,1.25f,1f);
        ObjectAnimator endAnim = ofFloat(mTargeView,View.TRANSLATION_Y,300f,0f);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(500);
        // 按顺序执行
        set.playSequentially(tranAnim,rotaAnim,scaleAnim,endAnim);
        set.start();

    }
}
