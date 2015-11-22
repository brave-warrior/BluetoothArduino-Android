package com.khmelenko.lab.bluetootharduino.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.khmelenko.lab.bluetootharduino.BtApplication;

/**
 * Application settings
 *
 * @author Dmytro Khmelenko
 */
public final class AppSettings {

    private static final String DEVICE_MAC_ADDRESS = "DeviceMacAddress";

    // denied constructor
    private AppSettings() {

    }

    /**
     * Gets shared preferences
     *
     * @return Shared preferences
     */
    private static SharedPreferences getPreferences() {
        Context context = BtApplication.getAppContext();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return defaultSharedPreferences;
    }

    /**
     * Gets device MAC address
     *
     * @return MAC address
     */
    public static String getDeviceMacAddress() {
        SharedPreferences pref = getPreferences();
        return pref.getString(DEVICE_MAC_ADDRESS, "");
    }

    /**
     * Puts device MAC address to settings
     *
     * @param macAddress MAC address
     */
    public static void putDeviceMacAddress(String macAddress) {
        SharedPreferences pref = getPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DEVICE_MAC_ADDRESS, macAddress);
        editor.commit();
    }

}

