package com.example.android.camera2basic;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

import java.nio.ByteBuffer;

import static android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT;
import static android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW;

class CameraUtil {
    private static final String TAG = "CameraUtil";
    public static int RWA_CAP = REQUEST_AVAILABLE_CAPABILITIES_RAW;
    public static int DEPTH_CAP = REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT;

    public static boolean isCapSupport(CameraCharacteristics cameraCharacteristics, int capability) {
        int[] supportCapability = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        assert supportCapability != null;
        for (int cap : supportCapability) {
            if (cap == capability) {
                return true;
            }
        }
        return false;
    }

    public static String[][] getOutputFormat(int[] supportFormat) {
        String[][] formatStr = new String[2][supportFormat.length];
        for (int i = 0; i < supportFormat.length; i++) {
            formatStr[0][i] = format2String(supportFormat[i]);
            formatStr[1][i] = String.valueOf(supportFormat[i]);
        }
        return formatStr;
    }

    public static String format2String(int format) {
        switch (format) {
            case ImageFormat.RGB_565:
                return "RGB_565";
            case ImageFormat.NV16:
                return "NV16";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.YV12:
                return "YV12";
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.YUV_422_888:
                return "YUV_422_888";
            case ImageFormat.YUV_444_888:
                return "YUV_444_888";
            case ImageFormat.FLEX_RGB_888:
                return "FLEX_RGB_888";
            case ImageFormat.FLEX_RGBA_8888:
                return "FLEX_RGBA_8888";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR";
            case ImageFormat.RAW_PRIVATE:
                return "RAW_PRIVATE";
            case ImageFormat.RAW10:
                return "RAW10";
            case ImageFormat.RAW12:
                return "RAW12";
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.PRIVATE:
                return "PRIVATE";
            default:
                return "ERROR FORMAT";
        }
    }

    /**
     * convert YUV_420_888 to NV21 data
     * @param image
     * @return NV21 byre[] data
     */
    public static byte[] getNV21DataFromImage(Image image) {
        long startTime = System.currentTimeMillis();
        if (image == null) {
            Log.e(TAG, "getNV21DataFromImage: image null", new Exception());
            return null;
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;

                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        long tookTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "getNV21DataFromImage: end: " + tookTime);
        return data;
    }

    /**
     * convert YUV_420_888 to NV21 data
     * @param image
     * @return NV12 bytes data
     */
    public static byte[] getNV12DataFromImage(Image image) {
        long startTime = System.currentTimeMillis();
        int totalSize = 0;
        ByteBuffer totalBuffer;
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            totalSize = image.getPlanes()[0].getBuffer().remaining()
                    + image.getPlanes()[1].getBuffer().remaining();
            totalBuffer = ByteBuffer.allocate(totalSize);
            totalBuffer.put(image.getPlanes()[0].getBuffer());
            totalBuffer.put(image.getPlanes()[1].getBuffer());
        } else {
            for (Image.Plane plane : image.getPlanes()) {
                totalSize += plane.getBuffer().remaining();
            }
            totalBuffer = ByteBuffer.allocate(totalSize);
            for (Image.Plane plane : image.getPlanes()) {
                totalBuffer.put(plane.getBuffer());
            }
        }
        image.close();
        long tookTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "getNV12DataFromImage: end: " + tookTime);

        return totalBuffer.array();
    }

    /**
     * @param nv21: true = NV21, false = NV12
     * @return NV21 or NV12 data
     */
    public static byte[] YUV_420_888toNV(Image image, boolean nv21) {
        long startTime = System.currentTimeMillis();

        byte[] nv;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv = new byte[ySize + uSize + vSize];

        yBuffer.get(nv, 0, ySize);
        if (nv21) {//U and V are swapped
            vBuffer.get(nv, ySize, vSize);
            uBuffer.get(nv, ySize + vSize, uSize);
        } else {
            uBuffer.get(nv, ySize, uSize);
            vBuffer.get(nv, ySize + uSize, vSize);
        }
        long tookTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "YUV_420_888toNV: end: " + tookTime);
        return nv;
    }

    /**
     *
     * @param image
     * @return NV21 data
     */
    public static byte[] YUV_420_888toNV21(Image image) {
        long startTime = System.currentTimeMillis();

        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        long tookTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "YUV_420_888toNV21: end: " + tookTime);
        return nv21;
    }

    public static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        YuvImage im = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        im.compressToJpeg(new Rect(0, 0, width, height), 95, bos);
        byte[] bytes = bos.toByteArray();
        long tookTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "nv21ToJpeg: end: " + tookTime);
        return bytes;
    }

    public static byte[] getByteFromReader(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static byte[] getByteFromReader(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        int totalSize = 0;
        ByteBuffer totalBuffer;
        if (reader.getImageFormat() == ImageFormat.YUV_420_888) {
            totalSize = image.getPlanes()[0].getBuffer().remaining()
                    + image.getPlanes()[1].getBuffer().remaining();
            totalBuffer = ByteBuffer.allocate(totalSize);
            totalBuffer.put(image.getPlanes()[0].getBuffer());
            totalBuffer.put(image.getPlanes()[1].getBuffer());
        } else {
            for (Image.Plane plane : image.getPlanes()) {
                totalSize += plane.getBuffer().remaining();
            }
            totalBuffer = ByteBuffer.allocate(totalSize);
            for (Image.Plane plane : image.getPlanes()) {
                totalBuffer.put(plane.getBuffer());
            }
        }
        image.close();
        return totalBuffer.array();
    }

}
