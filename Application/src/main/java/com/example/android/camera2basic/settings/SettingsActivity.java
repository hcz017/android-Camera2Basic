package com.example.android.camera2basic.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // move photos to Classified folder after test
    public static final String CUT_MODE = "cut_mode";
    // keep big photos in origin folder and add a "dump_" prefix
    public static final String DUMP_BIG_PHOTO = "dump_big_photos";

    private static boolean mIsCutMode = false;
    private static boolean dumpBigPhotos = false;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        initSharedPreference();
    }

    private void initSharedPreference() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsCutMode = mSharedPreferences.getBoolean(CUT_MODE, false);
        dumpBigPhotos = mSharedPreferences.getBoolean(DUMP_BIG_PHOTO, false);
        Log.d(TAG, "onCreate: mIsCutMode: " + mIsCutMode + ", dumpBigPhotos: " + dumpBigPhotos);

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(CUT_MODE)) {
                    mIsCutMode = mSharedPreferences.getBoolean(CUT_MODE, false);
                }

                if (key.equals(DUMP_BIG_PHOTO)) {
                    dumpBigPhotos = mSharedPreferences.getBoolean(DUMP_BIG_PHOTO, false);
                }
                Log.d(TAG, "onSharedPreferenceChanged: mIsCutMode: " + mIsCutMode
                        + ", dumpBigPhotos: " + dumpBigPhotos);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }

    public static boolean isCutMOde() {
        return mIsCutMode;
    }

    public static boolean needDumpBigPhotos() {
        return dumpBigPhotos;
    }

    public static void loadSettings(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mIsCutMode = sharedPreferences.getBoolean(CUT_MODE, false);
        dumpBigPhotos = sharedPreferences.getBoolean(DUMP_BIG_PHOTO, false);
        Log.d(TAG, "loadSettings: mIsCutMode: " + mIsCutMode
                + ", dumpBigPhotos: " + dumpBigPhotos);
    }
}