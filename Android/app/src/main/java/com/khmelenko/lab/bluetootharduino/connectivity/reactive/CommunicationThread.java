package com.khmelenko.lab.bluetootharduino.connectivity.reactive;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.khmelenko.lab.bluetootharduino.BtApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Thread for communication with connected device
 *
 * @author Dmytro Khmelenko
 */
public class CommunicationThread {

    private final InputStream mInStream;
    private final OutputStream mOutStream;

    private Subscription mActiveSubscription;

    /**
     * Constructor
     *
     * @param socket     Communication socket
     * @param subscriber Subscriber for getting responses
     */
    public CommunicationThread(BluetoothSocket socket, Subscriber<byte[]> subscriber) {

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

        mActiveSubscription = subscribeForCommunication(subscriber);
    }

    /**
     * Subscribes for communication
     *
     * @param subscriber Subscriber
     * @return Subscription
     */
    private Subscription subscribeForCommunication(Subscriber<byte[]> subscriber) {
        return generateObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * Generates observable for communication
     *
     * @return Observable object
     */
    private Observable<byte[]> generateObservable() {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {

                byte[] data = new byte[256];
                int bytes = 0;

                while (true) {
                    try {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        bytes = mInStream.read(data);
                        buffer.write(data, 0, bytes);

                        Log.d(BtApplication.TAG, "New data arrived: " + buffer.toString());
                        subscriber.onNext(buffer.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                        // notify about error
                        subscriber.onError(e);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Sends the message to the receiver
     *
     * @param message Message for sending
     */
    public void send(String message) {
        Log.d(BtApplication.TAG, "Send message: " + message);
        byte[] msgBuffer = message.getBytes();
        try {
            mOutStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(BtApplication.TAG, "Unable to send:\n" + e.getMessage());
        }
    }

    /**
     * Stops communication thread
     */
    public void stop() {
        mActiveSubscription.unsubscribe();
    }

}
