package com.example.android.camera2basic;

import android.graphics.ImageFormat;
import android.util.Size;

public class Config {
    public static String MAIN_CAM_ID = "0";

    // use fixed preview size @see #getFixedPreviewSize()
    public static boolean USE_FIXED_PRE_SIZE = false;
    // use fixed capture picture size @see #getFixedPictureSize()
    public static boolean USE_FIXED_PIC_SIZE = false;

    public static final int DEPTH_PREVIEW_ROTATE = 90;
    public static final boolean FLIP_PREVIEW = false;

    public static Size getFixedPreviewSize() {
        return new Size(1280, 720);
    }

    public static Size getFixedPictureSize() {
        return new Size(1280, 720);
    }

    static class MainCamCfg {
        // whether to take a raw pic when taking pic
        public static boolean TAKE_SEC_FORMAT = false;
        // whether to get raw data when init preview
        public static boolean PREVIEW_SEC_FORMAT = true;

        public static int SEC_FORMAT = ImageFormat.YUV_420_888;
    }

    static class SecCamCfg {
        // show a second preview on screen
        public static boolean ADD_SEC_PREVIEW = false;
    }
}
