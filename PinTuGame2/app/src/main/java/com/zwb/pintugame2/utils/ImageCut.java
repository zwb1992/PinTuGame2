package com.zwb.pintugame2.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by zwb
 * Description 切图工具
 * Date 2017/6/9.
 */

public class ImageCut {

    private Bitmap mBitmap;

    public ImageCut(Bitmap mBitmap) {
        this.mBitmap = scaleBitmap(mBitmap);
    }

    /**
     * 默认行列相同
     *
     * @param count
     * @return
     */
    public Bitmap[][] cutBitmap(int count) {
        int pieceWidth = mBitmap.getWidth() / count;
        Bitmap[][] bitmaps = new Bitmap[count][count];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                bitmaps[i][j] = Bitmap.createBitmap(mBitmap, j * pieceWidth, i * pieceWidth, pieceWidth, pieceWidth);
            }
        }
        return bitmaps;
    }

    /**
     * 使图片缩放至宽高相等
     *
     * @param bitmap
     * @return
     */
    private Bitmap scaleBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == h) {
            return bitmap;
        }
        int minSize = Math.min(w, h);
        float scaleW = minSize * 1.0f / w;
        float scaleH = minSize * 1.0f / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }
}
