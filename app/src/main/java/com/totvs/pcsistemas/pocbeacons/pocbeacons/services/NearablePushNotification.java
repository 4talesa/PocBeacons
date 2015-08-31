package com.totvs.pcsistemas.pocbeacons.pocbeacons.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.MainActivity;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.NearablePlayCheckInActivity;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.R;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.OwnerInfo;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.RestaurantCheckIn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

public class NearablePushNotification extends IntentService {

    public static final String REQUEST_TAG = "REQUEST_TAG";

    private static final int REQUEST_ENABLE_BT = 1234;

    private BeaconManager beaconManager;
    private String scanId;
    private Nearable selectedNearable = null;

    private RestaurantCheckIn checkIn;

    private OwnerInfo ownerInfo;

    Handler handler;

    public NearablePushNotification() {
        super("NearablePushNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String requestString = intent.getStringExtra(REQUEST_TAG);
        pushNotification(intent, "Hi, Rond.", "I'm read to be used! I was called by " + requestString);

    }

    @Override
    public void onDestroy(){
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onCreate(){
        handler = new Handler();

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        ownerInfo = new OwnerInfo(this);

        selectedNearable = null;

        if (beaconManager.isBluetoothEnabled()) {
            connectToService();
        }

        super.onCreate();
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

                        /*Intent msgIntent = new Intent(getApplicationContext(), NearablePushNotification.class);

                        msgIntent.putExtra(NearablePushNotification.REQUEST_TAG, "connectToService");
                        startService(msgIntent);*/

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Note that beacons reported here are already sorted by estimated
                                // distance between device and beacon.
                                // Just in case if there are multiple beacons with the same uuid, major, minor.
                                //IMMEDIATE, NEAR, FAR = if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE"))

                                pushNotification(null, "Hi, Rond.", "I'm read to be used! I was called by rangedNearable");
                                Nearable foundNearable = null;
                                for (Nearable rangedNearable : rangedNearables) {
                                    foundNearable = rangedNearable;
                                    // updateNearableFound(foundNearable);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void pushNotification(Intent intent, String title, String content){
        try {
            Context context = NearablePushNotification.this;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title);
            builder.setContentText(content);
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
            Toast.makeText(NearablePushNotification.this, "NotificationManager " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

}
