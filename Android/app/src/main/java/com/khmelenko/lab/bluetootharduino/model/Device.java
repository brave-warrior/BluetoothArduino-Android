package com.khmelenko.lab.bluetootharduino.model;

/**
 * Device model
 *
 * @author Dmytro Khmelenko
 */
public final class Device {

    private final String mName;
    private final String mMacAddress;

    public Device(String name, String macAddress) {
        mName = name;
        mMacAddress = macAddress;
    }

    public String getName() {
        return mName;
    }

    public String getMacAddress() {
        return mMacAddress;
    }
}
