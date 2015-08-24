package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.*;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.estimote.sdk.*;
import com.estimote.sdk.Region;
import com.estimote.sdk.cloud.*;
import com.estimote.sdk.cloud.model.NearableInfo;
import com.estimote.sdk.exception.EstimoteServerException;
import com.firebase.client.*;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters.NearableListAdapter;

import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.drivers.FirebaseConn;

public class NearableListActivity extends ActionBarActivity {

    private static final String TAG = "NearableList";

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final int TIME_BETWEEN_IN_SECONDS = 25;

    private BeaconManager beaconManager;
    private NearableListAdapter adapter;
    private String scanId;
    private EstimoteCloud estimoteCloud;

    public Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_list);
        Firebase.setAndroidContext(this);

        Bundle bundle = getIntent().getExtras();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        //beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(TIME_BETWEEN_IN_SECONDS), 0);

        // Configure device list.
        adapter = new NearableListAdapter(this);
        ListView list = (ListView) findViewById(R.id.listNearable);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createOnItemClickListener());

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(30), 0);

        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(final List<Nearable> rangedNearables) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        // Just in case if there are multiple beacons with the same uuid, major, minor.
                        Nearable foundNearable = null;
                        for (Nearable rangedNearable : rangedNearables) {
                            foundNearable = rangedNearable;
                            updateNearableFound(foundNearable);
                        }

                        adapter.replaceWith(rangedNearables);
                        final TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText("Nearables found: " + rangedNearables.size());
                    }
                });
            }
        });
    }

    private void updateNearableFound(Nearable foundNearable) {

        updateNearableFoundToFirebase(foundNearable);

    }

    private void updateNearableFoundToFirebase(Nearable foundNearable) {
        try{
            //Toast.makeText(this, "I found nearable Identifier: "+foundNearable.identifier, Toast.LENGTH_LONG).show();

            Firebase mFirebaseInputBeacon = new FirebaseConn().child("nearable").child(foundNearable.identifier);

            Map<String, Object> updates = new HashMap<String, Object>();

            updates.put("Identifier", foundNearable.identifier);
            updates.put("Major", foundNearable.region.getMajor());
            updates.put("Minor", foundNearable.region.getMinor());
            updates.put("Advertising interval", "2000");
            updates.put("Broadcasting power", foundNearable.power.powerInDbm);
            updates.put("Battery level", foundNearable.batteryLevel.toString());
            updates.put("Firmware", foundNearable.firmwareVersion);
            updates.put("Temperature", String.format("%.1f\u00b0C", foundNearable.temperature));
            updates.put("In Motion", foundNearable.isMoving ? "Yes" : "No");
            updates.put("Last Motion State Duration", foundNearable.lastMotionStateDuration);
            updates.put("Current Motion State Duration", foundNearable.currentMotionStateDuration);
            updates.put("Motion Data", String.format("x: %.0f   y: %.0f   z: %.0f", foundNearable.xAcceleration, foundNearable.yAcceleration, foundNearable.zAcceleration));
            updates.put("Orientation", foundNearable.orientation.toString());
            mFirebaseInputBeacon.updateChildren(updates);

            try {
                EstimoteCloud.getInstance().fetchNearableDetails(foundNearable.identifier,
                        new CloudCallback<NearableInfo>() {

                            @Override
                            public void success(NearableInfo nearableInfo) {
                                Firebase mFirebaseInputBeacon = new FirebaseConn().child("nearable").child(nearableInfo.identifier);
                                Map<String, Object> updates = new HashMap<String, Object>();
                                updates.put("Identifier", nearableInfo.identifier);
                                updates.put("Identifier Type", nearableInfo.type.toString());
                                updates.put("Color", nearableInfo.color.toString());
                                mFirebaseInputBeacon.updateChildren(updates);
                            }

                            @Override
                            public void failure(EstimoteServerException e) {

                            }
                        });
            }catch(Exception e){
                //do nothing
                //Toast.makeText(NearableListActivity.this, "updateNearableFoundToFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            //do nothing
            //Toast.makeText(NearableListActivity.this, "updateNearableFoundToFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearable_list, menu);
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
    protected void onDestroy() {
        // When no longer needed. Should be invoked in #onDestroy.
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
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

    @Override
    protected void onStop() {
        beaconManager.disconnect();
        // Should be invoked in #onStop.
        beaconManager.stopNearableDiscovery(scanId);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        adapter.replaceWith(Collections.<Nearable>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                beaconManager.startNearableDiscovery();

            }
        });
    }

    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Nearable nearableClicked = (Nearable)adapter.getItem(position);
                //Create a list item
                Firebase mFirebaseInputBeacon = new FirebaseConn().child("nearable").child(nearableClicked.identifier).child("list").push();
                Map<String, Object> updates = new HashMap<String, Object>();

                updates.put("title", "Title "+ (int)(Math.random() * 999)+1);
                updates.put("description", "Description "+ (int)(Math.random() * 999)+1);
                updates.put("pictureUrl", "http://lorempixel.com/75/75/food/");

                mFirebaseInputBeacon.setValue(updates);
            }
        };
    }
}
