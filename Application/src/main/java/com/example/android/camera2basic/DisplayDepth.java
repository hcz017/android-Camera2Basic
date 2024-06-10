package com.example.android.camera2basic;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.view.Surface;

import com.example.android.camera2basic.gles.CameraGLSurfaceRender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DisplayDepth implements Runnable {
    private static final String TAG = "DisplayDepth";
    private byte[] mData;
    private Surface mSurface;
    private Surface mGLSurface;
    private Paint bitmapPaint;
    private int mHeight;
    private int mWidth;
    CameraGLSurfaceRender mGLRender;

    DisplayDepth(byte[] data, int width, int height, Surface surface, Surface glSurface, CameraGLSurfaceRender glrender) {
        mHeight = height;
        mWidth = width;
        bitmapPaint = new Paint();
        bitmapPaint.setColor(Color.BLACK);
        mData = data;
        mSurface = surface;
        mGLSurface = glSurface;
        mGLRender = glrender;
    }

    @Override
    public void run() {
        if (mData == null) {
            return;
        }
        YuvImage yuvimage = new YuvImage(mData, ImageFormat.NV21, mWidth, mHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 90, baos);

        byte[] jpegData = baos.toByteArray();
        int scaleRate = 1;//2
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inSampleSize = scaleRate;
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, newOpts);

            bitmap = rotateBitmapByDegree(bitmap, Config.DEPTH_PREVIEW_ROTATE, Config.FLIP_PREVIEW);
            Rect rect = new Rect(0, 0, mWidth, mHeight);//
            Canvas canvas = mSurface.lockCanvas(rect);
            Matrix matrix = canvas.getMatrix();
            float ratio = (float) canvas.getHeight() / bitmap.getHeight();
            matrix.postScale(ratio, ratio);
            canvas.setMatrix(matrix);
            canvas.drawColor(Color.BLUE);
            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
            mSurface.unlockCanvasAndPost(canvas);
            mSurface.release();
            if (mGLRender != null) {
                mGLRender.setImageBitMap(bitmap);
            }
//            Canvas glcanvas = mGLSurface.lockCanvas(rect);
//            matrix = glcanvas.getMatrix();
//            ratio = (float)glcanvas.getHeight() / bitmap.getHeight();
//            matrix.postScale(ratio, ratio);
//            glcanvas.setMatrix(matrix);
//            glcanvas.drawColor(Color.RED);
//            glcanvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
//            mGLSurface.unlockCanvasAndPost(glcanvas);
//            mGLSurface.release();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree, boolean flip) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        if (flip) {
            matrix.postScale(-1, 1);
        }
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        return returnBm;
    }
}
