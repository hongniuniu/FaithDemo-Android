package com.xiaopo.flying.puzzle;

import android.content.Context;
import android.util.AttributeSet;

/**
 * the square puzzle view
 * Created by snowbean on 16-8-16.
 */
public class SquarePuzzleView extends PuzzleView {
  private int w = -1;
//  private float wRatio = 1f, hRatio = 0.75f;
  private float wRatio = 1f, hRatio = 1f; // 保持正方形1:1比例

  public SquarePuzzleView(Context context) {
    super(context);
  }

  public SquarePuzzleView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquarePuzzleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (w == -1) w = MeasureSpec.getSize(widthMeasureSpec);

    int newW = (int) (w * wRatio);
    int newH = (int) (newW * hRatio);


    setMeasuredDimension(newW, newH);
  }

  /**
   * 设置宽度高度比例</b>
   * 宽度比例是基于原始宽度的
   * 高度比例是基于新的宽度
   */
  public void setRatio(float wRatio, float hRatio) {
    this.wRatio = wRatio;
    this.hRatio = hRatio;
    setNeedOnDrawResetLayout(true);
    requestLayout();
  }
}
