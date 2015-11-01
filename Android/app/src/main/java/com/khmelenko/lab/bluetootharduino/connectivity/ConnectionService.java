package com.khmelenko.lab.bluetootharduino.connectivity;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.khmelenko.lab.bluetootharduino.BtApplication;

import java.io.IOException;
import java.util.UUID;

/**
 * Service for connectivity
 *
 * @author Dmytro Khmelenko
 */
public final class ConnectionService extends Service {

    public static final int RECEIVE_MESSAGE = 1;

    private static final UUID CLIENT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final IBinder mBinder = new ConnectionBinder();

    private BluetoothSocket mBtSocket;
    private CommunicationThread mConnectedThread;

    /**
     * Binder for communication with the service
     */
    public class ConnectionBinder extends Binder {

        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Establishes connection with the device
     *
     * @param device  Device for connection
     * @param handler Handler
     */
    public void connect(BluetoothDevice device, Handler handler) {

        // disconnect previous connection
        disconnect();

        try {
            mBtSocket = device.createRfcommSocketToServiceRecord(CLIENT_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(BtApplication.TAG, "Socket create failed\n" + e.getMessage());
        }

        // Establish the connection.  This will block until it connects.
        Log.d(BtApplication.TAG, "Connecting...");
        try {
            mBtSocket.connect();
            Log.d(BtApplication.TAG, "Connected");
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }

        // start communication thread
        mConnectedThread = new CommunicationThread(mBtSocket, handler);
        mConnectedThread.start();
    }

    /**
     * Disconnects connected device
     */
    public void disconnect() {
        if (mBtSocket != null) {
            closeConnection();
            mBtSocket = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.interrupt();
            mConnectedThread = null;
        }
    }

    /**
     * Sends data to the connected device
     *
     * @param data Data for sending
     */
    public void send(String data) {
        if (mConnectedThread != null) {
            mConnectedThread.send(data);
        }
    }

    /**
     * Checks whether device connected or not
     *
     * @return True, if device connected. False otherwise
     */
    public boolean isConnected() {
        return mBtSocket != null && mBtSocket.isConnected();
    }

    /**
     * Closes socket connection
     */
    private void closeConnection() {
        try {
            mBtSocket.close();
        } catch (IOException e) {
            Log.d(BtApplication.TAG, "Failed to close connection\n" + e.getMessage());
        }
    }
}
