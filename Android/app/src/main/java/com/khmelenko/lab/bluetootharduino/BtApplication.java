package com.khmelenko.lab.bluetootharduino;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

/**
 * Main application class
 *
 * @author Dmytro Khmelenko
 */
public class BtApplication extends Application {

    public static final String TAG = "Arduino_BT";

    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    /**
     * Gets application context
     *
     * @return Application context
     */
    public static Context getAppContext() {
        return mContext;
    }
}
