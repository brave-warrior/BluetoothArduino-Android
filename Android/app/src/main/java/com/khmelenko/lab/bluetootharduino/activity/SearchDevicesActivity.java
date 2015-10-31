package com.khmelenko.lab.bluetootharduino.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.khmelenko.lab.bluetootharduino.R;
import com.khmelenko.lab.bluetootharduino.adapter.DevicesListAdapter;
import com.khmelenko.lab.bluetootharduino.adapter.OnListItemListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity for searching bluetooth devices
 *
 * @author Dmytro Khmelenko
 */
public class SearchDevicesActivity extends AppCompatActivity implements OnListItemListener {

    @Bind(R.id.search_devices_recycler_view)
    RecyclerView mDevicesRecyclerView;

    @Bind(R.id.progressbarview)
    View mProgressBar;

    private DevicesListAdapter mDevicesListAdapter;
    private List<BluetoothDevice> mDevices;

    private BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        ButterKnife.bind(this);
        initToolbar();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevices = new ArrayList<>();
        mDevicesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDevicesRecyclerView.setLayoutManager(layoutManager);

        mDevicesListAdapter = new DevicesListAdapter(this, mDevices, this);
        mDevicesRecyclerView.setAdapter(mDevicesListAdapter);

        registerSearchResultsReceiver();
    }

    /**
     * Registers the broadcast receiver for the search results
     */
    private void registerSearchResultsReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBtAdapter.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBtAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onItemSelected(int position) {
        // TODO Connect to selected
    }

    /**
     * Initializes toolbar
     */
    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(MainActivity.TAG, "Receiver action: " + action);

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        // TODO Handle different states
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevices.add(device);
                    mDevicesListAdapter.notifyDataSetChanged();
                    Log.d(MainActivity.TAG, String.format("Found device: %s, %s", device.getName(), device.getAddress()));
                    break;
            }
        }
    };

}
