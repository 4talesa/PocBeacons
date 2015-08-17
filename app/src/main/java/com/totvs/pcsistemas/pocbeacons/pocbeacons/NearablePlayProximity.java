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

public class NearablePlayProximity extends ActionBarActivity {

    private static final String TAG = "NearableProximity";

    private static final int REQUEST_ENABLE_BT = 1234;

    private BeaconManager beaconManager;
    private String scanId;
    private Nearable selectedNearable = null;

    private String FIREBASE_URL;
    private Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_proximity);
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

        final Button btnProximityAddToCart = (Button)findViewById(R.id.btnProximityAddToCart);
        btnProximityAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NearablePlayProximity.this, "Product add to cart!", Toast.LENGTH_LONG).show();
            }
        });

        if(bundle.containsKey("beaconIdentifier")){
            try {
                String beaconIdentifier = bundle.getString("beaconIdentifier");
                String beaconTitle = bundle.getString("beaconTitle");
                String beaconPictureUrl = bundle.getString("beaconPictureUrl");
                String beaconOffer = bundle.getString("Offer");
                String beaconOfferPictureUrl = bundle.getString("OfferPicture");
                String beaconOfferSponsor = bundle.getString("OfferSponsor");
                String beaconOfferPictureSponsorUrl = bundle.getString("OfferPictureSponsor");

                ShowOffer(beaconOfferSponsor
                        , beaconOfferPictureSponsorUrl
                        , beaconOffer
                        , beaconOfferPictureUrl
                );
            }catch(Exception e){}
        }
        else{

            performPlay();
        }

    }

    private void performPlay(){
        connectToService();

        selectedNearable = null;

        final TextView textViewOfferGreeterProximity = (TextView) findViewById(R.id.textViewOfferGreeterProximity);
        textViewOfferGreeterProximity.setText(R.string.lbl_loading);

        final TextView textViewOfferSponsorProximity = (TextView) findViewById(R.id.textViewOfferSponsorProximity);
        textViewOfferSponsorProximity.setText(R.string.lbl_loading);

        final TextView textViewOfferProximity = (TextView) findViewById(R.id.textViewOfferProximity);
        textViewOfferProximity.setText(R.string.lbl_loading);

        final ImageView imageViewBeaconProximity = (ImageView) findViewById(R.id.imageViewBeaconProximity);
        imageViewBeaconProximity.setImageResource(R.drawable.keep_calm_and_wait);

        final ImageView imageViewBeaconSponsorProximity = (ImageView) findViewById(R.id.imageViewBeaconSponsorProximity);
        imageViewBeaconSponsorProximity.setImageResource(R.drawable.keep_calm_and_wait);
    }

    private void updateNearableFound(Nearable foundNearable) {

        try{
            updateNearableFoundFromFirebase(foundNearable);

        }catch (Exception e){}
    }

    private void updateNearableFoundFromFirebase(Nearable foundNearable) {

        //Toast.makeText(NearablePlayProximity.this, "updateNearableFoundFromFirebase executed", Toast.LENGTH_LONG).show();
        if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE")) {

            if (selectedNearable != null && foundNearable != null) {
                if (foundNearable.identifier.equals(selectedNearable.identifier)) {
                    return;
                }
            }

            selectedNearable = foundNearable;

            Firebase mFirebaseIncomingBeacon = new Firebase(FIREBASE_URL).child("nearable").child(foundNearable.identifier);

            mFirebaseIncomingBeacon.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {

                        ShowOffer(
                                    snapshot.child("OfferSponsor").getValue().toString()
                                  , snapshot.child("OfferPictureSponsor").getValue().toString()
                                  , snapshot.child("Offer").getValue().toString()
                                  , snapshot.child("OfferPicture").getValue().toString()
                        );

                    } catch (Exception e) {
                        //Toast.makeText(NearablePlayProximity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // do nothing
                }
            });

        }
    }

    private void ShowOffer(String OfferSponsor, String OfferPictureSponsorUrl, String Offer, String OfferPictureUrl){
        final TextView textViewOfferGreeterProximity = (TextView) findViewById(R.id.textViewOfferGreeterProximity);
        textViewOfferGreeterProximity.setText("Hi Rondy!");

        try {
            final TextView textViewOfferSponsorProximity = (TextView) findViewById(R.id.textViewOfferSponsorProximity);
            String selectedBeaconOfferSponsor = "Buy a " + OfferSponsor;
            textViewOfferSponsorProximity.setText(selectedBeaconOfferSponsor);
        } catch (Exception e) {
            //Toast.makeText(NearablePlayProximity.this, "OfferSponsor: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        try {
            final TextView textViewOfferProximity = (TextView) findViewById(R.id.textViewOfferProximity);
            String selectedBeaconOffer = "And get a " + Offer;
            textViewOfferProximity.setText(selectedBeaconOffer);
        } catch (Exception e) {
            //Toast.makeText(NearablePlayProximity.this, "Offer: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        try{
            final ImageView imageViewBeaconProximity = (ImageView) findViewById(R.id.imageViewBeaconProximity);
            String selectedBeaconOfferPicturePictureUrl = OfferPictureUrl;
            imageViewBeaconProximity.setImageResource(R.drawable.no_image_available);
            Ion.with(imageViewBeaconProximity)
                    .fitCenter()
                    .load(selectedBeaconOfferPicturePictureUrl);
        } catch (Exception e) {
            //Toast.makeText(NearablePlayProximity.this, "OfferPicture: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        try{
            final ImageView imageViewBeaconSponsorProximity = (ImageView) findViewById(R.id.imageViewBeaconSponsorProximity);
            String selectedBeaconOfferSponsorPictureUrl = OfferPictureSponsorUrl;
            imageViewBeaconSponsorProximity.setImageResource(R.drawable.no_image_available);
            Ion.with(imageViewBeaconSponsorProximity)
                    .fitCenter()
                    .load(selectedBeaconOfferSponsorPictureUrl);
        } catch (Exception e) {
            //Toast.makeText(NearablePlayProximity.this, "OfferPictureSponsor: " + e.toString(), Toast.LENGTH_LONG).show();
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
                        //Toast.makeText(NearablePlayProximity.this, "onNearablesDiscovered triggered", Toast.LENGTH_LONG).show();
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
