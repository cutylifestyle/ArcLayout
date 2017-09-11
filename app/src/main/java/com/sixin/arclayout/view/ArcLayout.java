package com.sixin.arclayout.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.sixin.arclayout.R;

public class ArcLayout extends ViewGroup {

    private static final String TAG = ArcLayout.class.getName();

    /**
     * 默认中心点选按钮的位置在左上角
     * */
    private Position mPosition = Position.LEFT_TOP;

    /**
     * 是否首次布局
     * */
    private boolean mFirstLayout;

    /**
     * 子view的个数
     * */
    private int mChildCount;

    /**
     * 菜单选项之间的夹角
     * */
    private double mAngle;

    /**
     * 1/4圆弧的半径
     * */
    private int mRadius;

    private enum Position{
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM
    }

    public ArcLayout(Context context) {
        super(context);
    }

    public ArcLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcLayout);
        int position = a.getInt(R.styleable.ArcLayout_position, 0);
        switch (position) {
            case 0:
                mPosition = Position.LEFT_TOP;
                break;
            case 1:
                mPosition = Position.LEFT_BOTTOM;
                break;
            case 2:
                mPosition = Position.RIGHT_TOP;
                break;
            case 3:
                mPosition = Position.RIGHT_BOTTOM;
                break;
        }

        mRadius = a.getDimensionPixelSize(R.styleable.ArcLayout_radius, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, getResources().getDisplayMetrics()));

        a.recycle();

    }

    public ArcLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // TODO: 2017/9/8 对这部分的枚举进行理解，这个东西的用途似乎很大,对这部分的自定义属性需要深入理解
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mChildCount = getChildCount();
        if(mChildCount > 2 ){
            mAngle = (Math.PI/2)/(mChildCount - 2);
        }

        for(int i = 0 ; i < mChildCount ; i++){
            View child = getChildAt(i);
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed && !mFirstLayout){
            mFirstLayout = true;
            //布局中心view
            layoutCenterView();

            //布局菜单选项
            layoutMenus();

            //设置中心view的点击事件
            setCenterViewClick();
        }
    }

    /**
     * 中心view的点击事件
     * */
    private void setCenterViewClick() {
        View childCenter = getChildAt(0);
        if(childCenter != null){
            childCenter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //执行一组属性动画：中心菜单需要旋转一周，选项菜单从中心菜单平移到原来的位置
                    executeAnimatorSet(v);
                }
            });
        }
    }

    /**
     * 执行一组属性动画
     * */
    private void executeAnimatorSet(View v) {

        //中心view执行旋转动画
        ObjectAnimator centerViewAnimator = ObjectAnimator.ofFloat(v, "rotation", 0f, 180f);
        centerViewAnimator.setInterpolator(new LinearInterpolator());
        centerViewAnimator.setDuration(500);
        centerViewAnimator.start();

        //菜单view执行位移动画
        for(int i = 1 ; i < mChildCount ; i++){
            View viewMenu =getChildAt(i);
            if(viewMenu != null){
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(viewMenu, "translationX", -viewMenu.getLeft(), 0f)
                                            ,ObjectAnimator.ofFloat(viewMenu,"translationY",-viewMenu.getTop(),0f));
                animatorSet.setInterpolator(new OvershootInterpolator());
                animatorSet.setDuration(500);
                animatorSet.start();

//                TranslateAnimation translateAnimation = new TranslateAnimation(-viewMenu.getLeft(), 0, -viewMenu.getTop(), 0);
//                translateAnimation.setInterpolator(new OvershootInterpolator());
//                translateAnimation.setDuration(500);
//                viewMenu.startAnimation(translateAnimation);
            }
        }
    }

    /**
     * 布局菜单选项
     * */
    private void layoutMenus() {
        for(int i = 1 ; i < mChildCount ; i++){
            View childMenu = getChildAt(i);
            if(childMenu != null){
                int childMenuWidth = childMenu.getMeasuredWidth();
                int childMenuHeight = childMenu.getMeasuredHeight();
                int left = (int) (mRadius * Math.cos((i - 1) * mAngle));
                int top = (int) (mRadius * Math.sin((i - 1) * mAngle));
                childMenu.layout(left,top,left+childMenuWidth,top+childMenuHeight);
            }
        }
    }

    /**
     * 布局中心view
     * */
    private void layoutCenterView() {
        View childCenter = getChildAt(0);
        if(childCenter != null){
            int viewWidth = childCenter.getMeasuredWidth();
            int viewHeight = childCenter.getMeasuredHeight();
            int left = 0;
            int top = 0;
            switch (mPosition) {
                case LEFT_TOP:
                    left = 0 ;
                    top  = 0 ;
                    break;
                case LEFT_BOTTOM:
                    left = 0 ;
                    top  = getMeasuredHeight() - viewHeight;
                    break;
                case RIGHT_TOP:
                    left = getMeasuredWidth() - viewWidth;
                    top = 0;
                    break;
                case RIGHT_BOTTOM:
                    left = getMeasuredWidth() - viewWidth;
                    top = getMeasuredHeight() - viewWidth;
                    break;
            }
            childCenter.layout(left,top,left+viewWidth,top+viewHeight);
        }

    }
}
