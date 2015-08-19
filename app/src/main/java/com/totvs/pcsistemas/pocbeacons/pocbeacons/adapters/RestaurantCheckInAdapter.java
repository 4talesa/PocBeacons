package com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.R;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.RestaurantCheckIn;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rond.borges on 18/08/2015.
 */
public class RestaurantCheckInAdapter extends FirebaseListAdapter<RestaurantCheckIn>{

    public RestaurantCheckInAdapter(Query ref, Activity activity, int layout, String fireBase_url) {
        super(ref, RestaurantCheckIn.class, layout, activity, fireBase_url);
    }

    @Override
    protected void populateView(View view, RestaurantCheckIn model) {
        // Map a Chat object to an entry in our listview

        String transaction = model.getTransaction();
        TextView transactionText = (TextView) view.findViewWithTag("list_check_in_transaction");
        transactionText.setText(transaction);

        String table = model.getTable();
        EditText tableText = (EditText) view.findViewWithTag("list_check_in_table");
        tableText.setText(table);

        String beaconIdentifier = model.getBeaconIdentifier();
        TextView beaconIdentifierText = (TextView) view.findViewWithTag("list_check_in_beaconIdentifier");
        beaconIdentifierText.setText(beaconIdentifier);

        String status = model.getStatus();
        TextView statusText = (TextView) view.findViewWithTag("list_check_in_status");
        statusText.setText(status);

        String customerName = model.getCustomerName();
        TextView customerNameText = (TextView) view.findViewWithTag("list_check_in_customerName");
        customerNameText.setText(customerName);

        String pictureUrl = model.getPictureUrl();
        ImageView imageImageView =(ImageView) view.findViewWithTag("list_check_in_image");
        Ion.with(imageImageView)
                .fitCenter()
                .load(pictureUrl);

        Button btnCheckIn = (Button) view.findViewWithTag("list_check_in_check_in");
        if (status.equals("CheckInRequested")){
            btnCheckIn.setText("Check In to " + customerName);
            btnCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView transactionText = (TextView) ((View) v.getParent()).findViewWithTag("list_check_in_transaction");

                        Firebase mFirebaseCheckIn = new Firebase(getFireBase_url()).child("checkin").child(transactionText.getText().toString());

                        Map<String, Object> updates = new HashMap<String, Object>();

                        updates.put("status", "CheckInSucess");
                        updates.put("table", (int)(Math.random() * 99) + 1);
                        mFirebaseCheckIn.updateChildren(updates);
                        //Toast.makeText(v.getContext(), "Check In Id " + transactionText.getText(), Toast.LENGTH_LONG).show();
                    }catch (Exception e){

                    }
                }
            });
        }
        else if (status.equals("CheckOutRequested")){
            btnCheckIn.setText("Check Out to " + customerName);
            btnCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView transactionText = (TextView) ((View) v.getParent()).findViewWithTag("list_check_in_transaction");

                        Firebase mFirebaseCheckIn = new Firebase(getFireBase_url()).child("checkin").child(transactionText.getText().toString());

                        Map<String, Object> updates = new HashMap<String, Object>();

                        updates.put("status", "CheckOutSucess");
                        mFirebaseCheckIn.updateChildren(updates);
                        //Toast.makeText(v.getContext(), "Check Out Id " + transactionText.getText(), Toast.LENGTH_LONG).show();
                    }catch (Exception e){

                    }
                }
            });
        }
        else {
            btnCheckIn.setText("Not available");
            btnCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Not available", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
