package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.estimote.sdk.EstimoteSDK;
import com.firebase.client.Firebase;


public class MainActivity extends ActionBarActivity{

    private ImageButton btnListBeacons;
    private ImageButton btnListNearables;

    private static final String ESTIMOTEAPPID = "pocbeacons";
    private static final String ESTIMOTETOKEN = "1c9283b168671ce2a55c07095a9ac983";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        final ImageButton btnListBeacons = (ImageButton)findViewById(R.id.btnListBeacons);
        btnListBeacons.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, BeaconListActivity.class);

                startActivity(it);
            }
        });

        final ImageButton btnListNearables = (ImageButton)findViewById(R.id.btnListNearables);
        btnListNearables.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, NearableListActivity.class);

                startActivity(it);
            }
        });

        final ImageButton btnPlay = (ImageButton)findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, NearablePlayActivity.class);

                startActivity(it);
            }
        });

        final ImageButton btnPlayNotify = (ImageButton)findViewById(R.id.btnPlayNotify);
        btnPlayNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, NearablePlayProximityActivity.class);

                startActivity(it);
            }
        });

        final ImageButton btnCheckin = (ImageButton)findViewById(R.id.btnCheckin);
        btnCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, NearablePlayCheckInActivity.class);

                startActivity(it);
            }
        });

        final ImageButton btnPlayRestaurant = (ImageButton)findViewById(R.id.btnPlayRestaurant);
        btnPlayRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, NearablePlayRestaurantActivity.class);

                startActivity(it);
            }
        });

        EstimoteSDK.initialize(this, ESTIMOTEAPPID, ESTIMOTETOKEN);
        EstimoteSDK.enableDebugLogging(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
