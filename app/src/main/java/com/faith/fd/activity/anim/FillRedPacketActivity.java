package com.faith.fd.activity.anim;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.faith.fd.R;
import com.faith.fd.activity.base.BaseActivity;

/**
 * 仿QQ塞红包动画
 * @autor hongbing
 * @date 2017/8/7
 */
public class FillRedPacketActivity extends BaseActivity {


    private int mScreenW;
    private View ovalV;
    private ObjectAnimator mAnimator;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_fill_redpacket;
    }

    @Override
    protected void initView() {
        mScreenW = getResources().getDisplayMetrics().widthPixels;

        ovalV = findViewById(R.id.id_ovalV);


//        TranslateAnimation ta = new TranslateAnimation(0,mScreenW,0,0);
//        ta.setDuration(300);
//        ta.setInterpolator(new LinearInterpolator());
//        ta.start();

        mAnimator = ObjectAnimator.ofFloat(ovalV, "translationX", 0, mScreenW);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(2500);



        findViewById(R.id.id_bgClk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnimator.start();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnimator.cancel();
    }
}
