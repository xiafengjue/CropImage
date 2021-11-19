package com.sora.cropimage.widget.cropiwa.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;

import com.sora.cropimage.widget.cropiwa.config.CropIwaSaveConfig;
import com.sora.cropimage.widget.cropiwa.shape.CropIwaShapeMask;
import com.sora.cropimage.widget.cropiwa.util.CropIwaUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Yaroslav Polyakov on 22.03.2017.
 * https://github.com/polyak01
 */

class CropImageTask extends AsyncTask<Void, Void, Throwable> {

    private Context context;
    private CropArea cropArea;
    private CropIwaShapeMask mask;
    private Uri srcUri;
    private CropIwaSaveConfig saveConfig;

    public CropImageTask(
            Context context, CropArea cropArea, CropIwaShapeMask mask,
            Uri srcUri, CropIwaSaveConfig saveConfig) {
        this.context = context;
        this.cropArea = cropArea;
        this.mask = mask;
        this.srcUri = srcUri;
        this.saveConfig = saveConfig;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            Bitmap bitmap = CropIwaBitmapManager.get().loadToMemory(context, srcUri);
            if (bitmap == null) {
                return new NullPointerException("Failed to load bitmap");
            }
            Bitmap cropped = cropArea.applyCropTo(bitmap);
            Bitmap resizeBmp = null;
            cropped = mask.applyMaskTo(cropped);
            Uri dst = saveConfig.getDstUri();
            OutputStream os = context.getContentResolver().openOutputStream(dst);
//            if (saveConfig.getWidth() == CropIwaBitmapManager.SIZE_UNSPECIFIED || saveConfig.getHeight() == CropIwaBitmapManager.SIZE_UNSPECIFIED) {
//                //不需要缩放
//                cropped.compress(saveConfig.getCompressFormat(), saveConfig.getQuality(), os);
//            } else {
                int w = cropped.getWidth();
                int h = cropped.getHeight();
                float sx = saveConfig.getWidth() / (float) w;
                float sy = saveConfig.getHeight() / (float) h;
                Matrix matrix = new Matrix();
                //也可以按两者之间最大的比例来设置放大比例，这样不会是图片压缩
//                float bigerS = Math.max(sx,sy);
//                matrix.postScale(bigerS,bigerS);
                matrix.postScale(sx, sy); // 长和宽放大缩小的比例
                resizeBmp = Bitmap.createBitmap(
                        cropped, 0, 0, w,
                        h, matrix, true
                );
                resizeBmp.compress(saveConfig.getCompressFormat(), saveConfig.getQuality(), os);
//            }
            CropIwaUtils.closeSilently(os);
            bitmap.recycle();
            cropped.recycle();
            if (resizeBmp != null)
                resizeBmp.recycle();

        } catch (IOException e) {
            return e;
        }
        return null;
    }

    private Bitmap zoomBitmap(Bitmap bitmap, float width, float height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float sx = width / (float) w;
        float sy = height / (float) h;
        Matrix matrix = new Matrix();
        //也可以按两者之间最大的比例来设置放大比例，这样不会是图片压缩
//        float bigerS = Math.max(sx,sy);
//        matrix.postScale(bigerS,bigerS);
        matrix.postScale(sx, sy); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(
                bitmap, 0, 0, w,
                h, matrix, true
        );
        return resizeBmp;
    }

    @Override
    protected void onPostExecute(Throwable throwable) {
        if (throwable == null) {
            CropIwaResultReceiver.onCropCompleted(context, saveConfig.getDstUri());
        } else {
            CropIwaResultReceiver.onCropFailed(context, throwable);
        }
    }
}