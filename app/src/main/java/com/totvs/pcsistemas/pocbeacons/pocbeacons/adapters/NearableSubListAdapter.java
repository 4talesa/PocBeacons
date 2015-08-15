package com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.koushikdutta.ion.Ion;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.R;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.models.NearableListContext;

/**
 * Created by rond.borges on 12/08/2015.
 */
public class NearableSubListAdapter extends FirebaseListAdapter<NearableListContext>{

    public NearableSubListAdapter(Query ref, Activity activity, int layout) {
        super(ref, NearableListContext.class, layout, activity);
    }

    @Override
    protected void populateView(View view, NearableListContext model) {
        // Map a Chat object to an entry in our listview
        String title = model.getTitle();
        TextView titleText = (TextView) view.findViewWithTag("list_context_title");
        titleText.setText(title);

        String description = model.getDescription();
        TextView descriptionText =(TextView) view.findViewWithTag("list_context_description");
        descriptionText.setText(description);

        String pictureUrl = model.getPictureUrl();
        ImageView imageImageView =(ImageView) view.findViewWithTag("list_context_image");
        Ion.with(imageImageView)
                .fitCenter()
                .load(pictureUrl);
    }
}
