package com.khmelenko.lab.bluetootharduino;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.khmelenko.lab.bluetootharduino.connectivity.reactive.ConnectionService;

/**
 * Main application class
 *
 * @author Dmytro Khmelenko
 */
public class BtApplication extends Application {

    public static final String TAG = "Arduino_BT";

    private static Context sContext;

    private ConnectionService mConnectionService;

    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, mServiceConnector, BIND_AUTO_CREATE);
    }

    /**
     * Gets application context
     *
     * @return Application context
     */
    public static Context getAppContext() {
        return sContext;
    }

    /**
     * Gets connectivity service
     *
     * @return Connectivity service
     */
    public ConnectionService getConnectionService() {
        return mConnectionService;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mServiceConnector = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder) service;
            mConnectionService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // do nothing
        }
    };

}
