package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by rond.borges on 27/08/2015.
 */
public class NearableParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String PARSE_DATA_KEY = "com.parse.Data";

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Intent it = new Intent(context, NearablePlayCheckInActivity.class);
        it.putExtras(intent.getExtras());
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(it);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Title");
        builder.setContentText("Text");
        builder.setSmallIcon(R.drawable.nearables_example01);
        builder.setAutoCancel(true);

        // OPTIONAL create soundUri and set sound:
        //builder.setSound(soundUri);

        Intent newIntent=new Intent(context,NearablePlayCheckInActivity.class);
        newIntent.putExtra(context.getString(R.string.navigation_from_notification),true);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi=PendingIntent.getActivity(context, 0, newIntent, 0);

        builder.setContentIntent(pi);

        notificationManager.notify("MyTag", 0, builder.build());

    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
        } catch (JSONException e) {
            // Json was not readable...
        }
        return data;
    }
}
