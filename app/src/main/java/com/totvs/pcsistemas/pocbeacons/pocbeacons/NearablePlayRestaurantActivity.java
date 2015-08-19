package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.database.DataSetObserver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters.RestaurantCheckInAdapter;


public class NearablePlayRestaurantActivity extends ActionBarActivity {

    private static final String TAG = "NearablePlayRestaurantActivity";

    private String FIREBASE_URL;
    RestaurantCheckInAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_play_restaurant);

        Firebase.setAndroidContext(this);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("FIREBASE_URL")){
            FIREBASE_URL = bundle.getString("FIREBASE_URL");
        }

        Firebase mFirebaseCheckIn = new Firebase(FIREBASE_URL).child("checkin");
        final ListView listTables = (ListView) findViewById(R.id.listTables);
        adapter = new RestaurantCheckInAdapter(mFirebaseCheckIn.limit(50), NearablePlayRestaurantActivity.this, R.layout.restaurant_check_in_item, FIREBASE_URL);
        listTables.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listTables.setSelection(adapter.getCount() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearable_play_restaurant, menu);
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
