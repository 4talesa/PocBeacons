package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.drivers.FirebaseConn;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.RestaurantCheckIn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class NearablePlayRestaurantCheckInActivity extends ActionBarActivity {

    private String transaction;
    private RestaurantCheckIn checkInEdit = new RestaurantCheckIn(0, "", "", "", "", "", 0.00);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearable_play_restaurant_check_in);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("transaction")){
            transaction = bundle.getString("transaction");

            Firebase mFirebaseCheckInRequest = new FirebaseConn().child("checkin").child(transaction);

            mFirebaseCheckInRequest.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {

                        RestaurantCheckIn checkInUpdate = snapshot.getValue(RestaurantCheckIn.class);

                        final EditText textViewCheckInTable = (EditText) findViewById(R.id.restaurant_check_in_table);
                        textViewCheckInTable.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                try {
                                    checkInEdit.setTable(Integer.parseInt(s.toString()));
                                }catch (NumberFormatException  e){
                                    checkInEdit.setTable(0);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        textViewCheckInTable.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                // If the event is a key-down event on the "enter" button
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    // Perform action on key press
                                    //final TextView textViewCheckStatus = (TextView) findViewById(R.id.restaurant_check_in_status);
                                    if (checkInEdit.getStatus().equals(getResources().getString(R.string.lbl_CheckInRequest))){
                                        final Button btnCheckInConfirmation = (Button)findViewById(R.id.btnCheckInConfirmation);
                                        btnCheckInConfirmation.performClick();
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });

                        final EditText textViewCheckInBill = (EditText) findViewById(R.id.restaurant_check_in_bill);
                        textViewCheckInBill.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                try{
                                    checkInEdit.setBill(Double.parseDouble(s.toString()));
                                }catch (NumberFormatException e){
                                    checkInEdit.setBill(0.00);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        textViewCheckInBill.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                // If the event is a key-down event on the "enter" button
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    // Perform action on key press

                                    final Button btnCheckInConfirmation = (Button)findViewById(R.id.btnCheckInConfirmation);
                                    btnCheckInConfirmation.performClick();

                                    return true;
                                }
                                return false;
                            }
                        });

                        if (checkInEdit.getTransaction().isEmpty()){
                            checkInEdit.Copy(checkInUpdate);
                            textViewCheckInBill.setText(checkInEdit.getBill().toString());
                            textViewCheckInTable.setText(checkInEdit.getTable().toString());
                        }

                        final TextView textViewBeaconIdentifier = (TextView) findViewById(R.id.restaurant_check_in_beaconIdentifier);
                        textViewBeaconIdentifier.setText(checkInUpdate.getBeaconIdentifier());

                        final TextView textViewCheckStatus = (TextView) findViewById(R.id.restaurant_check_in_status);
                        textViewCheckStatus.setText(checkInUpdate.getStatus());

                        final TextView textViewCustomerName = (TextView) findViewById(R.id.restaurant_check_in_customerName);
                        textViewCustomerName.setText(checkInUpdate.getCustomerName());

                        final ImageView imageViewCheckIn = (ImageView) findViewById(R.id.restaurant_check_in_image);
                        imageViewCheckIn.setImageResource(R.drawable.keep_calm_and_wait);
                        Ion.with(imageViewCheckIn)
                                .fitCenter()
                                .load(checkInUpdate.getPictureUrl());

                        final Button btnCheckInConfirmation = (Button)findViewById(R.id.btnCheckInConfirmation);
                        btnCheckInConfirmation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Firebase mFirebaseCheckIn = new FirebaseConn().child("checkin").child(transaction);

                                    Map<String, Object> updates = new HashMap<String, Object>();

                                    if (textViewCheckStatus.getText().equals(getResources().getString(R.string.lbl_CheckInRequest))){
                                        updates.put("status", getResources().getString(R.string.lbl_CheckInSucess));
                                    }
                                    else if (textViewCheckStatus.getText().equals(getResources().getString(R.string.lbl_CheckOutRequest))){
                                        updates.put("status", getResources().getString(R.string.lbl_CheckOutSucess));
                                    }
                                    updates.put("table", checkInEdit.getTable());
                                    updates.put("bill", checkInEdit.getBill());
                                    mFirebaseCheckIn.updateChildren(updates);
                                    //Toast.makeText(v.getContext(), "Check In Confirmed! Id: " + transaction, Toast.LENGTH_LONG).show();

                                    NearablePlayRestaurantCheckInActivity.this.finish();
                                } catch (Exception e) {
                                    Toast.makeText(v.getContext(), "OnClickListener: " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

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
        getMenuInflater().inflate(R.menu.menu_nearable_play_restaurant_check_in, menu);
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
