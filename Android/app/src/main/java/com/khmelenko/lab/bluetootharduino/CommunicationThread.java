package com.khmelenko.lab.bluetootharduino;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Thread for communication with connected device
 *
 * @author Dmytro Khmelenko
 */
public class CommunicationThread extends Thread {

    private Handler mHandler;

    private final InputStream mInStream;
    private final OutputStream mOutStream;

    final int RECIEVE_MESSAGE = 1;

    /**
     * Constructor
     *
     * @param socket  Communication socket
     * @param handler UI handler
     */
    public CommunicationThread(BluetoothSocket socket, Handler handler) {
        mHandler = handler;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInStream = inputStream;
        mOutStream = outputStream;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[256];
        int bytes;

        // reading data in infinite loop
        while (true) {
            try {
                bytes = mInStream.read(buffer);
                mHandler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Sends the message to the receiver
     *
     * @param message Message for sending
     */
    public void send(String message) {
        Log.d(MainActivity.TAG, "Write message: " + message);
        byte[] msgBuffer = message.getBytes();
        try {
            mOutStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "Unable to send:\n" + e.getMessage());
        }
    }

}
