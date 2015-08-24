package com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.totvs.pcsistemas.pocbeacons.pocbeacons.MainActivity;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.NearablePlayRestaurantCheckInActivity;
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

        String transaction = model.getTransaction();
        TextView transactionText = (TextView) view.findViewWithTag("list_check_in_transaction");
        transactionText.setText(transaction);

        String table = model.getTable().toString();
        TextView tableText = (TextView) view.findViewWithTag("list_check_in_table");
        tableText.setText(table);

        String beaconIdentifier = model.getBeaconIdentifier();
        TextView beaconIdentifierText = (TextView) view.findViewWithTag("list_check_in_beaconIdentifier");
        beaconIdentifierText.setText(beaconIdentifier);

        String status = model.getStatus();
        TextView statusText = (TextView) view.findViewWithTag("list_check_in_status");
        statusText.setText(status);

        String bill = model.getBill().toString();
        TextView billText = (TextView) view.findViewWithTag("list_check_in_bill");
        billText.setText(bill);

        String customerName = model.getCustomerName();
        TextView customerNameText = (TextView) view.findViewWithTag("list_check_in_customerName");
        customerNameText.setText(customerName);

        String pictureUrl = model.getPictureUrl();
        ImageView imageImageView =(ImageView) view.findViewWithTag("list_check_in_image");
        Ion.with(imageImageView)
                .fitCenter()
                .load(pictureUrl);

        if (status.equals(view.getResources().getString(R.string.lbl_CheckInRequest))){
            view.setBackgroundColor(Color.parseColor(view.getResources().getString(R.string.color_bg_CheckInRequest)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView transactionText = (TextView) v.findViewWithTag("list_check_in_transaction");

                        Intent it = new Intent(v.getContext(), NearablePlayRestaurantCheckInActivity.class);

                        it.putExtra("FIREBASE_URL", getFireBase_url());
                        it.putExtra("transaction", transactionText.getText().toString());

                        v.getContext().startActivity(it);
                    }catch (Exception e){

                    }
                }
            });
        }
        else if (status.equals(view.getResources().getString(R.string.lbl_CheckOutRequest))){
            view.setBackgroundColor(Color.parseColor(view.getResources().getString(R.string.color_bg_CheckOutRequest)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView transactionText = (TextView) v.findViewWithTag("list_check_in_transaction");

                        Intent it = new Intent(v.getContext(), NearablePlayRestaurantCheckInActivity.class);

                        it.putExtra("FIREBASE_URL", getFireBase_url());
                        it.putExtra("transaction", transactionText.getText().toString());

                        v.getContext().startActivity(it);
                    }catch (Exception e){

                    }
                }
            });
        }
        else {
            view.setBackgroundColor(Color.parseColor("#e2e2e2"));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), "Not available", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
