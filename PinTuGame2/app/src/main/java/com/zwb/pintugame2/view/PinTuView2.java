package com.zwb.pintugame2.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zwb.pintugame2.GameData;
import com.zwb.pintugame2.R;
import com.zwb.pintugame2.utils.ImageCut;

import java.util.Random;

/**
 * Created by zwb
 * Description 美女平图控件--RelativeLayout
 * Date 2017/6/9.
 */

public class PinTuView2 extends RelativeLayout implements View.OnClickListener {
    private Bitmap bitmap;
    /**
     * 设置Item的数量n*n；默认为3
     */
    private int mColumn = 3;

    private ImageView[][] imageViews = null;
    private ImageView mStartImageView;//第一次点击的imageView
    private boolean init = false;
    private PinTuView2.CallBack callBack;
    private boolean gameOver = false;//游戏结束，此时不能操作拼图了
    private boolean isAnim = false;//如果正在执行动画，不能点击

    /**
     * 拼图控件的宽高
     */
    private int mWidth;
    private boolean once = false;
    private int padding;
    /**
     * 每个图块之间的间距
     */
    private int mMargin = 3;
    private RelativeLayout mAnimLayout = null;//动画层
    /**
     * 每个图块的宽高
     */
    private int mChildWidth;

    public PinTuView2(Context context) {
        this(context, null);
    }

    public PinTuView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinTuView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PinTuView2, defStyleAttr, 0);
        int id = a.getResourceId(R.styleable.PinTuView2_bitmap, R.mipmap.ic_launcher);
        bitmap = BitmapFactory.decodeResource(getResources(), id);
        mColumn = a.getInteger(R.styleable.PinTuView2_mColumn, 3);
        padding = min(getPaddingBottom(), getPaddingTop(), getPaddingLeft(), getPaddingRight());
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMargin, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
        if (!once) {
            mChildWidth = (mWidth - padding * 2 - (mColumn - 1) * mMargin) / mColumn;
            initImageViews();
            once = true;
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    /**
     * 取最小内边距
     *
     * @param params padding
     * @return int
     */
    private int min(int... params) {
        int result = params[0];
        for (int padding : params) {
            result = Math.min(padding, result);
        }
        return result;
    }

    private void initImageViews() {
        imageViews = new ImageView[mColumn][mColumn];
        Bitmap[][] bitmaps = new ImageCut(bitmap).cutBitmap(mColumn);
        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mColumn; j++) {
                imageViews[i][j] = new ImageView(getContext());
                imageViews[i][j].setOnClickListener(this);
                imageViews[i][j].setImageBitmap(bitmaps[i][j]);
                imageViews[i][j].setTag(new GameData(i, j, bitmaps[i][j]));
                int id = i * mColumn + j + 1;//id不能为0，所以要加上1
                imageViews[i][j].setId(id);
                RelativeLayout.LayoutParams lp = new LayoutParams(mChildWidth, mChildWidth);
                if (i != 0) {//不是第一行
                    lp.topMargin = mMargin;
                    lp.addRule(RelativeLayout.BELOW, imageViews[i - 1][0].getId());
                }
                if (j != 0) {//不是第一列
                    lp.leftMargin = mMargin;
                    lp.addRule(RelativeLayout.RIGHT_OF, imageViews[0][j - 1].getId());
                }
                addView(imageViews[i][j], lp);
            }
        }
        init = false;
        randomPosition();
        init = true;
    }

    /**
     * 打乱顺序
     */
    private void randomPosition() {
        //打乱的次数
        int count = 50;
        for (int i = 0; i < count; i++) {
            mStartImageView = imageViews[mColumn - 1][mColumn - 1];
            int x = new Random().nextInt(mColumn);
            int y = new Random().nextInt(mColumn);
            GameData blankData = (GameData) mStartImageView.getTag();
            //与最后一个图块交换位置。如果是同一个位置就不交换
            if (x != blankData.getX() || y != blankData.getY()) {
                switchData(imageViews[x][y]);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (gameOver || isAnim) {
            return;
        }
        ImageView srcImageView = (ImageView) v;
        if (mStartImageView == null) {
            mStartImageView = srcImageView;
            mStartImageView.setColorFilter(Color.parseColor("#55FF0000"));
            return;
        }
        //点击的是同一张图片，取消原来选中的图片
        if (mStartImageView == srcImageView) {
            mStartImageView.setColorFilter(null);
            mStartImageView = null;
            return;
        }
        srcImageView.setColorFilter(Color.parseColor("#55FF0000"));
        isAnim = true;
        changePosition(srcImageView);
    }

    /**
     * 与空白块交换位置位置
     *
     * @param srcImageView 要交换位置的imageView
     */
    private void changePosition(final ImageView srcImageView) {
        mStartImageView.setColorFilter(null);
        srcImageView.setColorFilter(null);
        int startX = mStartImageView.getLeft();
        int startY = mStartImageView.getTop();
        int endX = srcImageView.getLeft();
        int endY = srcImageView.getTop();
        setUpAnimLayout();
        final ImageView firstView = new ImageView(getContext());
        firstView.setImageBitmap(((BitmapDrawable) mStartImageView.getDrawable()).getBitmap());
        RelativeLayout.LayoutParams lp = new LayoutParams(mChildWidth, mChildWidth);
        lp.leftMargin = mStartImageView.getLeft() - getPaddingLeft();
        lp.topMargin = mStartImageView.getTop() - getPaddingTop();
        mAnimLayout.addView(firstView, lp);

        final ImageView secondView = new ImageView(getContext());
        secondView.setImageBitmap(((BitmapDrawable) srcImageView.getDrawable()).getBitmap());
        lp = new LayoutParams(mChildWidth, mChildWidth);
        lp.leftMargin = srcImageView.getLeft() - getPaddingLeft();
        lp.topMargin = srcImageView.getTop() - getPaddingTop();
        mAnimLayout.addView(secondView, lp);

        TranslateAnimation startAnim = new TranslateAnimation(0, endX - startX, 0, endY - startY);
        startAnim.setDuration(2000);
        startAnim.setFillAfter(true);
        startAnim.setInterpolator(new LinearInterpolator());
        startAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnim = true;
                mStartImageView.setVisibility(INVISIBLE);
                srcImageView.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                switchData(srcImageView);
                firstView.clearAnimation();
                secondView.clearAnimation();
                mAnimLayout.removeAllViews();
                isAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        firstView.startAnimation(startAnim);
        TranslateAnimation endAnim = new TranslateAnimation(0, startX - endX, 0, startY - endY);
        endAnim.setDuration(2000);
        endAnim.setInterpolator(new LinearInterpolator());
        endAnim.setFillAfter(true);
        secondView.startAnimation(endAnim);
    }

    private void setUpAnimLayout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    /**
     * 交换数据
     *
     * @param srcImageView 最后点击的图块
     */
    private void switchData(ImageView srcImageView) {
        mStartImageView.clearAnimation();
        srcImageView.clearAnimation();
        GameData srcData = (GameData) srcImageView.getTag();
        GameData dstData = (GameData) mStartImageView.getTag();
        Bitmap bitmap = ((BitmapDrawable) srcImageView.getDrawable()).getBitmap();
        dstData.setBitmap(bitmap);
        srcData.setBitmap(((BitmapDrawable) mStartImageView.getDrawable()).getBitmap());
        int tempX = srcData.getP_x();
        int tempY = srcData.getP_y();
        srcData.setP_x(dstData.getP_x());
        srcData.setP_y(dstData.getP_y());
        dstData.setP_x(tempX);
        dstData.setP_y(tempY);
        srcImageView.setImageBitmap(((BitmapDrawable) mStartImageView.getDrawable()).getBitmap());
        mStartImageView.setImageBitmap(bitmap);
        mStartImageView.setVisibility(VISIBLE);
        srcImageView.setVisibility(VISIBLE);
        mStartImageView = null;
        if (isCompleted() && init) {
            gameOver = true;
            if (callBack != null) {
                callBack.completed();
            }
        }
    }

    /**
     * 是否完成平图
     *
     * @return true
     */
    private boolean isCompleted() {
        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mColumn; j++) {
                GameData gameData = (GameData) imageViews[i][j].getTag();
                if (!gameData.isRightPosition()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setCallBack(PinTuView2.CallBack callBack) {
        this.callBack = callBack;
    }


    public interface CallBack {
        void gameOver();

        void completed();
    }
}
