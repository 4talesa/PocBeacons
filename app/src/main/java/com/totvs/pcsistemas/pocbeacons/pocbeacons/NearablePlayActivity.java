package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.estimote.sdk.*;
import com.firebase.client.*;

import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters.NearableSubListAdapter;

public class NearablePlayActivity extends ActionBarActivity {

    private static final String TAG = "NearablePlay";

    private static final int REQUEST_ENABLE_BT = 1234;

    private BeaconManager beaconManager;
    private String scanId;
    private Nearable selectedNearable = null;

    private String FIREBASE_URL;
    private Firebase mFirebaseRef;
    private NearableSubListAdapter nearableSubListAdapter;

    private boolean offerShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_play);
        Firebase.setAndroidContext(this);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("FIREBASE_URL")){
            FIREBASE_URL = bundle.getString("FIREBASE_URL");
        }

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        performPlay();

        final ImageButton btnPlay = (ImageButton)findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlay();
            }
        });
    }

    private void performPlay(){
        connectToService();

        selectedNearable = null;
        offerShow = false;

        final TextView textViewNearableDescription = (TextView) findViewById(R.id.textViewNearableDescription);
        textViewNearableDescription.setText(R.string.lbl_loading);

        final TextView textViewNearableLongDescription = (TextView) findViewById(R.id.textViewNearableLongDescription);
        textViewNearableLongDescription.setText(R.string.lbl_loading);

        final ListView listViewNearableContext = (ListView) findViewById(R.id.listViewNearableContext);
        listViewNearableContext.setAdapter(null);

        final ImageView imageViewNearable = (ImageView) findViewById(R.id.imageViewNearableContext);
        imageViewNearable.setImageResource(R.drawable.keep_calm_and_wait);
    }

    private void updateNearableFound(Nearable foundNearable) {

        try{
            if (selectedNearable == null) {
                updateNearableFoundFromFirebase(foundNearable);
            }else {
                if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE") || Utils.computeProximity(foundNearable).toString().equals("NEAR")) {
                    updateNearableFoundFromFirebase(foundNearable);
                }
            }
        }catch (Exception e){}
    }

    private void updateNearableFoundFromFirebase(Nearable foundNearable) {

        //Toast.makeText(NearablePlayActivity.this, "updateNearableFoundFromFirebase executed", Toast.LENGTH_LONG).show();
        if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE") || Utils.computeProximity(foundNearable).toString().equals("NEAR")) {

            if (selectedNearable != null && foundNearable != null) {
                if (foundNearable.identifier.equals(selectedNearable.identifier)) {
                    evaluateBeaconProximity(foundNearable);
                }
            } else if (selectedNearable == null) {
                //Reset only when click Play again
                //offerShow = false;
                selectedNearable = foundNearable;
                evaluateBeaconProximity(foundNearable);

                Firebase mFirebaseBeaconContextList = new Firebase(FIREBASE_URL).child("nearable").child(foundNearable.identifier).child("list");
                final ListView listViewNearableContext = (ListView) findViewById(R.id.listViewNearableContext);
                nearableSubListAdapter = new NearableSubListAdapter(mFirebaseBeaconContextList.limit(50), NearablePlayActivity.this, R.layout.nearable_list_context);
                listViewNearableContext.setAdapter(nearableSubListAdapter);
                nearableSubListAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        listViewNearableContext.setSelection(nearableSubListAdapter.getCount() - 1);
                    }
                });

                Firebase mFirebaseIncomingBeacon = new Firebase(FIREBASE_URL).child("nearable").child(foundNearable.identifier);

                mFirebaseIncomingBeacon.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {

                            final TextView textViewNearableDescription = (TextView) findViewById(R.id.textViewNearableDescription);
                            String selectedBeaconDescription = snapshot.child("Description").getValue().toString();
                            textViewNearableDescription.setText(selectedBeaconDescription);

                            final TextView textViewNearableLongDescription = (TextView) findViewById(R.id.textViewNearableLongDescription);
                            String selectedBeaconLongDescription = snapshot.child("LongDescription").getValue().toString();
                            textViewNearableLongDescription.setText(selectedBeaconLongDescription);

                            final ImageView imageViewNearable = (ImageView) findViewById(R.id.imageViewNearableContext);
                            String selectedBeaconLogoPictureUrl = snapshot.child("Picture").getValue().toString();
                            Ion.with(imageViewNearable)
                                    .fitCenter()
                                    .load(selectedBeaconLogoPictureUrl);

                        } catch (Exception e) {
                            Toast.makeText(NearablePlayActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        // do nothing
                    }
                });
            }
        }
    }

    private void evaluateBeaconProximity(Nearable foundNearable){
        //IMMEDIATE, NEAR, FAR
        if (!offerShow) {
            if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE")) {

                offerShow = true;
                Firebase mFirebaseIncomingBeacon = new Firebase(FIREBASE_URL).child("nearable").child(foundNearable.identifier);

                mFirebaseIncomingBeacon.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {

                            if (selectedNearable != null && !snapshot.child("Offer").getValue().toString().isEmpty()&& !snapshot.child("OfferPicture").getValue().toString().isEmpty()) {
                                Intent it = new Intent(NearablePlayActivity.this, NearablePlayProximity.class);


                                it.putExtra("FIREBASE_URL", FIREBASE_URL);
                                it.putExtra("beaconIdentifier", selectedNearable.identifier);
                                it.putExtra("beaconTitle", snapshot.child("Description").getValue().toString());
                                it.putExtra("beaconPictureUrl", snapshot.child("Picture").getValue().toString());
                                it.putExtra("beaconOffer", snapshot.child("Offer").getValue().toString());
                                it.putExtra("beaconOfferPictureUrl", snapshot.child("OfferPicture").getValue().toString());

                                startActivity(it);
                            }

                        } catch (Exception e) {
                            Toast.makeText(NearablePlayActivity.this, "evaluateBeaconProximity: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        // do nothing
                    }
                });
            }
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
        //Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show();

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();

                beaconManager.setNearableListener(new BeaconManager.NearableListener() {
                    @Override
                    public void onNearablesDiscovered(final List<Nearable> rangedNearables) {
                        //Toast.makeText(NearablePlayActivity.this, "onNearablesDiscovered triggered", Toast.LENGTH_LONG).show();
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
