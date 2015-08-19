package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.OwnerInfo;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.RestaurantCheckIn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import java.util.UUID;


public class NearablePlayCheckInActivity extends ActionBarActivity {

    private static final String TAG = "NearableProximity";

    private static final int REQUEST_ENABLE_BT = 1234;

    private BeaconManager beaconManager;
    private String scanId;
    private Nearable selectedNearable = null;

    private String FIREBASE_URL;

    private OwnerInfo ownerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_play_check_in);
        Firebase.setAndroidContext(this);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("FIREBASE_URL")){
            FIREBASE_URL = bundle.getString("FIREBASE_URL");
        }

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        final Button btnCheckOut = (Button)findViewById(R.id.btnCheckOut);
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NearablePlayCheckInActivity.this, "Not available", Toast.LENGTH_LONG).show();
            }
        });

        ownerInfo = new OwnerInfo(this);

        performPlay();
    }

    private void performPlay(){
        connectToService();

        selectedNearable = null;

        final TextView textViewCheckInTable = (TextView) findViewById(R.id.check_in_table);
        textViewCheckInTable.setText(R.string.lbl_loading);

        final TextView textViewBeaconIdentifier = (TextView) findViewById(R.id.check_in_beaconIdentifier);
        textViewBeaconIdentifier.setText(R.string.lbl_loading);

        final TextView textViewCheckStatus = (TextView) findViewById(R.id.check_in_status);
        textViewCheckStatus.setText(R.string.lbl_loading);

        final TextView textViewCustomerName = (TextView) findViewById(R.id.check_in_customerName);
        textViewCustomerName.setText(ownerInfo.name);

        final ImageView imageViewCheckIn = (ImageView) findViewById(R.id.check_in_image);
        imageViewCheckIn.setImageResource(R.drawable.keep_calm_and_wait);
    }

    private void updateNearableFound(Nearable foundNearable) {

        try{
            updateNearableFoundFromFirebase(foundNearable);

        }catch (Exception e){}
    }

    private void updateNearableFoundFromFirebase(Nearable foundNearable) {

        //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase executed", Toast.LENGTH_LONG).show();
        if ((selectedNearable == null)&&(Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE") || Utils.computeProximity(foundNearable).toString().equals("NEAR"))) {

            /*if (selectedNearable != null && foundNearable != null) {
                if (foundNearable.identifier.equals(selectedNearable.identifier)) {
                    return;
                }
            }*/

            selectedNearable = foundNearable;

            final TextView textViewCheckStatus = (TextView) findViewById(R.id.check_in_status);
            textViewCheckStatus.setText("checkInRequested");

            RestaurantCheckIn checkIn = new RestaurantCheckIn("0", selectedNearable.identifier, "http://lorempixel.com/75/75/people/", "checkInRequested", ownerInfo.name, UUID.randomUUID().toString());
            Firebase mFirebaseCheckInRequest = new Firebase(FIREBASE_URL).child("checkin").child(checkIn.getTransaction());
            mFirebaseCheckInRequest.setValue(checkIn);

            mFirebaseCheckInRequest.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {

                        RestaurantCheckIn checkIn = snapshot.getValue(RestaurantCheckIn.class);

                        final TextView textViewCheckInTable = (TextView) findViewById(R.id.check_in_table);
                        textViewCheckInTable.setText(checkIn.getTable());

                        final TextView textViewBeaconIdentifier = (TextView) findViewById(R.id.check_in_beaconIdentifier);
                        textViewBeaconIdentifier.setText(checkIn.getBeaconIdentifier());

                        final TextView textViewCheckStatus = (TextView) findViewById(R.id.check_in_status);
                        textViewCheckStatus.setText(checkIn.getStatus());

                        final TextView textViewCustomerName = (TextView) findViewById(R.id.check_in_customerName);
                        textViewCustomerName.setText(checkIn.getCustomerName());

                        final ImageView imageViewCheckIn = (ImageView) findViewById(R.id.check_in_image);
                        imageViewCheckIn.setImageResource(R.drawable.keep_calm_and_wait);
                        Ion.with(imageViewCheckIn)
                                .fitCenter()
                                .load(checkIn.getPictureUrl());

                    } catch (Exception e) {
                        //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // do nothing
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearable_play_check_in, menu);
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
            performPlay();
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

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();

                beaconManager.setNearableListener(new BeaconManager.NearableListener() {
                    @Override
                    public void onNearablesDiscovered(final List<Nearable> rangedNearables) {
                        //Toast.makeText(NearablePlayProximityActivity.this, "onNearablesDiscovered triggered", Toast.LENGTH_LONG).show();
                        // Note that results are not delivered on UI thread.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Note that beacons reported here are already sorted by estimated
                                // distance between device and beacon.
                                // Just in case if there are multiple beacons with the same uuid, major, minor.
                                //IMMEDIATE, NEAR, FAR = if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE"))

                                Nearable foundNearable = null;
                                for (Nearable rangedNearable : rangedNearables) {
                                    foundNearable = rangedNearable;
                                    updateNearableFound(foundNearable);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
