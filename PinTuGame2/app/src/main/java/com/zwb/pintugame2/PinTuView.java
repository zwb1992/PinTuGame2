package com.zwb.pintugame2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by zwb
 * Description 美女平图控件
 * Date 2017/6/9.
 */

public class PinTuView extends GridLayout {
    private Bitmap bitmap;
    private int rowCount = 3;//行数
    private int columnCount = 3;//列数
    private ImageView[][] imageViews = null;
    private ImageView mStartImageView;//第一次点击的imageView
    private boolean init = false;
    private CallBack callBack;
    private boolean gameOver = false;//游戏结束，此时不能操作拼图了
    private boolean isAnim = false;//如果正在执行动画，不能点击

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public PinTuView(Context context) {
        this(context, null);
    }

    public PinTuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinTuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PinTuView, defStyleAttr, 0);
        int id = a.getResourceId(R.styleable.PinTuView_bitmap, R.mipmap.ic_launcher);
        bitmap = BitmapFactory.decodeResource(getResources(), id);
        rowCount = getRowCount();
        columnCount = getColumnCount();
        initImageViews();
    }

    /**
     * 初始化图片
     */
    private ImageView imageView;

    private void initImageViews() {
        imageViews = new ImageView[rowCount][columnCount];
        Bitmap bitmap = scaleBitmapToScreen();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tempWidth = width / columnCount;
        int tempHeight = height / rowCount;
        Bitmap tempBitmap;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                tempBitmap = Bitmap.createBitmap(bitmap, j * tempWidth, i * tempHeight, tempWidth, tempHeight);
                imageView = new ImageView(getContext());
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(tempBitmap);
                imageView.setTag(new GameData(i, j, tempBitmap));
                imageViews[i][j] = imageView;
                addView(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (gameOver || isAnim) {
                            return;
                        }
                        Log.e("info", "--onclick---" + v + "--mStart--" + mStartImageView);
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
                });
            }
        }
        init = false;
//        randomPosition();
        init = true;//打乱顺序完成
    }

    /**
     * 把bitmap缩放到与屏幕一样宽
     */
    private Bitmap scaleBitmapToScreen() {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //减去这12是因为有3行3列，每一个imageView设置的padding是2，减去之后图片才能完全显示
        int screenW = getResources().getDisplayMetrics().widthPixels - columnCount * 4;
        int screenH = getResources().getDisplayMetrics().heightPixels - rowCount * 4;
        float scale = Math.min(screenH * 1.0f / height, screenW * 1.0f / width);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 与空白块交换位置位置
     *
     * @param srcImageView 要交换位置的imageView
     */
    private void changePosition(final ImageView srcImageView) {
        int startX = mStartImageView.getLeft();
        int startY = mStartImageView.getTop();
        int endX = srcImageView.getLeft();
        int endY = srcImageView.getTop();
        TranslateAnimation startAnim = new TranslateAnimation(0, endX - startX, 0, endY - startY);
        startAnim.setDuration(2000);
        startAnim.setFillAfter(true);
        startAnim.setInterpolator(new LinearInterpolator());
        startAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnim = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                switchData(srcImageView);
                isAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mStartImageView.startAnimation(startAnim);
        TranslateAnimation endAnim = new TranslateAnimation(0, startX - endX, 0, startY - endY);
        endAnim.setDuration(2000);
        endAnim.setInterpolator(new LinearInterpolator());
        endAnim.setFillAfter(true);
        srcImageView.startAnimation(endAnim);
//        switchData(srcImageView);
    }

    /**
     * 交换数据
     *
     * @param srcImageView 最后点击的图块
     */
    private void switchData(ImageView srcImageView) {
        mStartImageView.clearAnimation();
        srcImageView.clearAnimation();
        mStartImageView.setColorFilter(null);
        srcImageView.setColorFilter(null);
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
        mStartImageView = null;
        if (isGameOver() && init) {
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
    private boolean isGameOver() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                GameData gameData = (GameData) imageViews[i][j].getTag();
                if (!gameData.isRightPosition()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 打乱顺序
     */
    private void randomPosition() {
        //打乱的次数
        int count = 50;
        for (int i = 0; i < count; i++) {
            int x = new Random().nextInt(rowCount);
            int y = new Random().nextInt(columnCount);
            GameData blankData = (GameData) imageViews[rowCount - 1][columnCount - 1].getTag();
            //与最后一个图块交换位置。如果是同一个位置就不交换
            if (x != blankData.getX() || y != blankData.getY()) {
                changePosition(imageViews[x][y]);
            }
        }
    }


    public void setRowColumn(int rowCount, int columnCount) {
        if (rowCount != 0 && columnCount != 0) {
            removeAllViews();
            gameOver = false;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            super.setRowCount(rowCount);
            super.setColumnCount(columnCount);
            if (bitmap != null) {
                initImageViews();
            }
        }
    }

    public interface CallBack {
        void gameOver();

        void completed();
    }

}
