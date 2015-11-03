package com.khmelenko.lab.bluetootharduino.connectivity;

import android.bluetooth.BluetoothDevice;

/**
 * Connection listener interface
 *
 * @author Dmytro Khmelenko
 */
public interface OnConnectionListener {

    /**
     * Called when connection to the device established
     *
     * @param device Connected device
     */
    void onConnected(BluetoothDevice device);

    /**
     * Called when connection to device failed
     */
    void onFailed();
}
