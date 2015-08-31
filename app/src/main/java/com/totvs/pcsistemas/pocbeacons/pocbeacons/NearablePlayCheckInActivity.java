package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
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
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.drivers.FirebaseConn;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.OwnerInfo;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.RestaurantCheckIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.util.UUID;


public class NearablePlayCheckInActivity extends ActionBarActivity {

    private static final String TAG = "NearableProximity";

    private static final int REQUEST_ENABLE_BT = 1234;

    private BeaconManager beaconManager;
    private String scanId;
    private Nearable selectedNearable = null;

    private RestaurantCheckIn checkIn;

    private OwnerInfo ownerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_play_check_in);
        Firebase.setAndroidContext(this);

        Bundle bundle = getIntent().getExtras();

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        final Button btnCheckOut = (Button)findViewById(R.id.btnCheckOut);
        btnCheckOut.setText("Not available");
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkIn.getStatus().equals(getResources().getString(R.string.lbl_CheckInSucess))) {
                    Firebase mFirebaseCheckIn = new FirebaseConn().child("checkin").child(checkIn.getTransaction());

                    Map<String, Object> updates = new HashMap<String, Object>();

                    updates.put("status", getResources().getString(R.string.lbl_CheckOutRequest));
                    mFirebaseCheckIn.updateChildren(updates);

                    Toast.makeText(NearablePlayCheckInActivity.this, "Check out requested", Toast.LENGTH_LONG).show();
                }
            }
        });

        ownerInfo = new OwnerInfo(this);

        selectedNearable = null;

        performPlay();
    }

    private void performPlay(){
        connectToService();

        final TextView textViewCheckInTable = (TextView) findViewById(R.id.check_in_table);
        textViewCheckInTable.setText(R.string.lbl_loading);

        final TextView textViewBeaconIdentifier = (TextView) findViewById(R.id.check_in_beaconIdentifier);
        textViewBeaconIdentifier.setText(R.string.lbl_loading);

        final TextView textViewCheckStatus = (TextView) findViewById(R.id.check_in_status);
        textViewCheckStatus.setText(R.string.lbl_loading);

        final TextView textViewCustomerName = (TextView) findViewById(R.id.check_in_customerName);
        textViewCustomerName.setText(ownerInfo.name);

        final TextView textViewBill = (TextView) findViewById(R.id.check_in_bill);
        textViewBill.setText(R.string.lbl_loading);

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

            checkIn = new RestaurantCheckIn(0
                    , selectedNearable.identifier
                    , "http://dadrix.com.br/apps/image/prof_pic.jpg"
                    , getResources().getString(R.string.lbl_CheckInRequest)
                    , ownerInfo.name
                    , UUID.randomUUID().toString()
                    , 0.00);
            watchCheckin(checkIn.getTransaction(), checkIn);

            try {
                Context context = NearablePlayCheckInActivity.this;

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle("Title");
                builder.setContentText("Text");
                builder.setSmallIcon(R.drawable.nearables_example01);
                builder.setAutoCancel(true);

                // OPTIONAL create soundUri and set sound:
                //builder.setSound(soundUri);

                Intent newIntent = new Intent(context, MainActivity.class);
                newIntent.putExtra(context.getString(R.string.navigation_from_notification), true);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pi = PendingIntent.getActivity(context, 0, newIntent, 0);

                builder.setContentIntent(pi);

                notificationManager.notify("MyTag", 0, builder.build());
            }catch (Exception e){
                Toast.makeText(NearablePlayCheckInActivity.this, "NotificationManager " + e.toString(), Toast.LENGTH_LONG).show();
            }
            /*try {
                String message = "Nearable found " + selectedNearable.identifier;
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("recipientId", 1);
                params.put("message", message);
                ParseCloud.callFunctionInBackground("sendPushToUser", params, new FunctionCallback() {
                    @Override
                    public void done(Object o, Throwable throwable) {
                        Toast.makeText(NearablePlayCheckInActivity.this, "done Throwable " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void done(Object o, ParseException e) {
                        Toast.makeText(NearablePlayCheckInActivity.this, "done ParseException " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }catch (Exception e){
                Toast.makeText(NearablePlayCheckInActivity.this, "ParseCloud Exception " +e.toString(), Toast.LENGTH_LONG).show();
            }*/
        }
    }

    private void watchCheckin(String transactionId, RestaurantCheckIn newCheckin){
        Firebase mFirebaseCheckInRequest = new FirebaseConn().child("checkin").child(transactionId);
        if (newCheckin!=null) {
            mFirebaseCheckInRequest.setValue(newCheckin);
        }

        mFirebaseCheckInRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {

                    checkIn = snapshot.getValue(RestaurantCheckIn.class);

                    final TextView textViewCheckInTable = (TextView) findViewById(R.id.check_in_table);
                    textViewCheckInTable.setText(checkIn.getTable().toString());

                    final TextView textViewBeaconIdentifier = (TextView) findViewById(R.id.check_in_beaconIdentifier);
                    textViewBeaconIdentifier.setText(checkIn.getBeaconIdentifier());

                    final TextView textViewCheckStatus = (TextView) findViewById(R.id.check_in_status);
                    textViewCheckStatus.setText(checkIn.getStatus());

                    final TextView textViewCustomerName = (TextView) findViewById(R.id.check_in_customerName);
                    textViewCustomerName.setText(checkIn.getCustomerName());

                    final TextView textViewBill = (TextView) findViewById(R.id.check_in_bill);
                    textViewBill.setText(checkIn.getBill().toString());

                    final ImageView imageViewCheckIn = (ImageView) findViewById(R.id.check_in_image);
                    imageViewCheckIn.setImageResource(R.drawable.keep_calm_and_wait);
                    Ion.with(imageViewCheckIn)
                            .fitCenter()
                            .load(checkIn.getPictureUrl());

                    final Button btnCheckOut = (Button)findViewById(R.id.btnCheckOut);
                    if (checkIn.getStatus().equals(getResources().getString(R.string.lbl_CheckInSucess))){
                        btnCheckOut.setText("Check Out");
                    }else{
                        btnCheckOut.setText("Not available");
                    }

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

        if (checkIn != null) {
            watchCheckin(checkIn.getTransaction(), checkIn);
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
