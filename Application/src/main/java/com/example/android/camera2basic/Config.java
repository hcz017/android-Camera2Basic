package com.example.android.camera2basic;

import android.graphics.ImageFormat;
import android.util.Size;

public class Config {
    public static String BACK_CAM_ID = "0";
    public static String FRONT_CAM_ID = "1";
    public static String MAIN_CAM_ID = "1";

    public static final boolean FLIP_PREVIEW = true;

    // use fixed preview size @see #getFixedPreviewSize()
    public static boolean USE_FIXED_PRE_SIZE = false;
    // use fixed capture picture size @see #getFixedPictureSize()
    public static boolean USE_FIXED_PIC_SIZE = false;

    public static Size getFixedPreviewSize() {
        return new Size(1280, 720);
    }

    public static Size getFixedPictureSize() {
        return new Size(1280, 720);
    }

    static class MainCamCfg {
        // whether to take a raw pic when taking pic
        public static boolean SNAPSHOT_SEC_FORMAT = false;
        // whether to get raw data when init preview
        public static boolean PREVIEW_SEC_FORMAT = false;

        public static int SEC_FORMAT = ImageFormat.RAW_SENSOR;
    }

    static class AuxCamCfg {
        // show a second preview on screen,
        // todo: currently it's for main camera to show another preview on gl surface view
        //  NOT the aux camera
        public static boolean ADD_AUX_CAM_PREVIEW = true;
    }
}
