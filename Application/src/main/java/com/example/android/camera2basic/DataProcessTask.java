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
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataProcessTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "DataProcessTask";
    private byte[] data;
    private final Surface surface;
    private Paint bitmapPaint;
    private int mHeight;
    private int mWidth;
    private boolean IsCapture = false;
    private boolean IsContinue = true;

    public DataProcessTask(byte[] data, int width, int height, Surface surface) {
        mWidth = width;
        mHeight = height;
        bitmapPaint = new Paint();
        bitmapPaint.setColor(Color.BLACK);
        this.data = data;
        this.surface = surface;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(TAG, "data.length = " + data.length + ", previewSize w,h: " + mWidth + "," + mHeight);
        //(TAG, "previewSize = " + previewSize.width + "," + previewSize.height);
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 90, baos);

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
            Rect rect = new Rect(0, 0, mWidth, mHeight);//
            if (IsContinue) {
                IsContinue = false;
                Canvas canvas = surface.lockCanvas(rect);
                Matrix matrix = canvas.getMatrix();
                matrix.postRotate(180, rect.width() / 2, rect.height() / 2);
                canvas.setMatrix(matrix);

                Log.d(TAG, "doInBackground: draw bitmap");
                canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
                surface.unlockCanvasAndPost(canvas);
                surface.release();
                IsContinue = true;
                baos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
