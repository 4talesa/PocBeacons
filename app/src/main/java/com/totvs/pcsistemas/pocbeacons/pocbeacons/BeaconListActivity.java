package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.firebase.client.*;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters.BeaconListAdapter;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.drivers.FirebaseConn;

import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class BeaconListActivity extends ActionBarActivity {

    private static final String TAG = "BeaconList";

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    private BeaconManager beaconManager;
    private BeaconListAdapter adapter;

    public Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_list);
        Firebase.setAndroidContext(this);

        Bundle bundle = getIntent().getExtras();
        final TextView textView = (TextView) findViewById(R.id.textView);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();

        // Configure device list.
        adapter = new BeaconListAdapter(this);
        ListView list = (ListView) findViewById(R.id.listBeacon);
        list.setAdapter(adapter);
        //list.setOnItemClickListener(createOnItemClickListener());

        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(30), 0);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        // Just in case if there are multiple beacons with the same uuid, major, minor.
                        Beacon foundBeacon = null;
                        for (Beacon rangedBeacon : rangedBeacons) {
                            foundBeacon = rangedBeacon;
                            updateBeaconFound(foundBeacon);
                        }

                        adapter.replaceWith(rangedBeacons);
                        textView.setText("Beacons found: " + rangedBeacons.size());
                    }
                });
            }
        });

    }

    private void updateBeaconFound(Beacon foundBeacon) {

        try{
            //Toast.makeText(this, "I found beacon MacAddress: "+foundBeacon.getMacAddress(), Toast.LENGTH_LONG).show();

            Firebase mFirebaseInputBeacon = new FirebaseConn().child("beacon").child(foundBeacon.getProximityUUID());
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("proximityUUID", foundBeacon.getProximityUUID());
            updates.put("macAddress", foundBeacon.getMacAddress());
            updates.put("nameBeacon", foundBeacon.getName());
            updates.put("major", foundBeacon.getMajor());
            updates.put("minor", foundBeacon.getMajor());
            updates.put("measuredPower", foundBeacon.getMajor());
            updates.put("rssi", foundBeacon.getMajor());
            mFirebaseInputBeacon.updateChildren(updates);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Falha ao enviar ao firebase " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /*private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
                    try {
                        Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
                        Intent intent = new Intent(BeaconListActivity.this, clazz);
                        intent.putExtra(EXTRAS_BEACON, adapter.getItem(position));
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Finding class by name failed", e);
                    }
                }
            }
        };
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override protected void onDestroy() {
        beaconManager.disconnect();
        mFirebaseRef.goOffline();

        super.onDestroy();
    }

    @Override protected void onStart() {
        super.onStart();

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }

    }

    @Override protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        //Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show();
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(BeaconListActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }
}
