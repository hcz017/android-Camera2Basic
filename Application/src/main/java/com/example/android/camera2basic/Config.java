package com.example.android.camera2basic;

import android.graphics.ImageFormat;
import android.util.Size;

public class Config {
    public static String MAIN_CAM_ID = "0";

    // use fixed preview size @see #getFixedPreviewSize()
    public static boolean USE_FIXED_PRE_SIZE = false;
    // use fixed capture picture size @see #getFixedPictureSize()
    public static boolean USE_FIXED_PIC_SIZE = false;

    // if open front camera, you may need set this to 270
    public static final int DEPTH_PREVIEW_ROTATE = 90;
    // if open front camera, you may need set this to true
    public static final boolean FLIP_PREVIEW = false;

    public static Size getFixedPreviewSize() {
        return new Size(1280, 720);
    }

    public static Size getFixedPictureSize() {
        return new Size(1280, 720);
    }

    static class MainCamCfg {
        // whether to take and save #SEC_FORMAT image when snapshot
        public static boolean SNAPSHOT_SEC_FORMAT = false;
        // whether to get and preview #SEC_FORMAT image(on TextureView)
        public static boolean PREVIEW_SEC_FORMAT = true;

        public static int SEC_FORMAT = ImageFormat.YUV_420_888;
    }

    static class GLSurfaceCfg {
        // show a gl surface view preview on screen
        // Todo:: it works when #MainCamCfg.SNAPSHOT_SEC_FORMAT and #MainCamCfg.PREVIEW_SEC_FORMAT both false
        public static boolean ADD_GL_SURFACE_PREVIEW = false;
        // works when open front camera #Config.FRONT_CAM_ID
        public static final boolean FLIP_GL_PREVIEW = true;
    }
}
