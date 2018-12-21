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
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DisplayDepth implements Runnable {
    private static final String TAG = "DisplayDepth";
    /**
     * The JPEG image
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final TextureView mTextureView;

    private Surface mSurface;
    private boolean IsCapture = false;
    private int mHeight;
    private int mWidth;
    private Paint bitmapPaint;

    DisplayDepth(Image image, int width, int height, TextureView textureView) {
        mHeight = height;
        mWidth = width;
        mImage = image;
        bitmapPaint = new Paint();
        bitmapPaint.setColor(Color.BLACK);
        mTextureView = textureView;
        mSurface = new Surface(textureView.getSurfaceTexture());
        Log.d(TAG, "Saved: " + mTextureView.toString());
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        Log.d(TAG, "ImageSaver: image length: " + data.length
                + ", format: " + mImage.getFormat() + ", w " + mImage.getWidth()
                + ", h " + mImage.getHeight());
        buffer.get(data);
        mImage.close();
        Log.d(TAG, "onPreviewFrame 00000=" + data.length);
        Log.d(TAG, "previewSize = " + mWidth + "," + mHeight);
        //(TAG, "previewSize = " + previewSize.width + "," + previewSize.height);
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 90, baos);

        //long t3 = System.currentTimeMillis() - t2 - time1;
        byte[] jpegData = baos.toByteArray();
        int scaleRate = 1;//2
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inSampleSize = scaleRate;
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, newOpts);
            if (IsCapture) {
                //System.arraycopy(data, 0, filedata, 0, 819200);
                saveBitmap(bitmap);
                writeFile(data);
            }
            Log.d(TAG, "data.length = " + data.length);

            Rect rect = new Rect(0, 0, mWidth, mHeight);//

            Log.d("doInBackground", "beforeDraw");
            //if (IsContinue) {
//            synchronized(surface)
//            {
            Log.d("doInBackground", "onDraw");
            Canvas canvas = mSurface.lockCanvas(rect);
            Matrix matrix = canvas.getMatrix();
//            matrix.postScale(-1, 1, rect.width()/2, rect.height()/2);
//            matrix.postRotate(0, rect.width()/2, rect.height()/2);
            matrix.postRotate(90, rect.width() / 2, rect.height() / 2);
            canvas.setMatrix(matrix);
//            int width = bitmap.getWidth();
//            int height = bitmap.getHeight();

            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
/*                Rect rectSrc = new Rect(0, 0, previewSize.height, previewSize.width);
                Rect rectDst = new Rect(0, 0, previewSize.height, previewSize.width);
                 canvas.drawBitmap(bitmap,rectSrc,rectDst,bitmapPaint);*/
            //canvas.scale(5.0f, 5.0f);
            mSurface.unlockCanvasAndPost(canvas);
            mSurface.release();
            Log.d("doInBackground", "afterDraw");
//            }

        } finally {
//                if (baos != null) {
//                    try {
//                        baos.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
        }
        //long t4 = System.currentTimeMillis() - t3 - t2 - time1;
    }


    public void saveBitmap(Bitmap mBitmap) {
        File filePic;
        String path = CameraUtil.createJpegName(System.currentTimeMillis());
        IsCapture = false;
        Log.d(TAG, "saveBitmap");
        try {
            filePic = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + path + "_depth_IR" + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeFile(byte[] data) {
        String path = CameraUtil.createJpegName(System.currentTimeMillis());
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + path + "_depth_IR" + ".raw");
        Log.d(TAG, "writeFile");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "writeFile FileNotFoundException");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "writeFile IOException");
        }

    }

    private byte[] readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/" + "20180101194650_2304x1728_203.raw");
        if (file.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int len = 0;
                while ((len = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                return outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}