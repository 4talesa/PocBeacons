package com.totvs.pcsistemas.pocbeacons.pocbeacons.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Nearable;
import com.estimote.sdk.Utils;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rond.borges on 11/08/2015.
 */
public class NearableListAdapter  extends BaseAdapter {
    private ArrayList<Nearable> nearables;
    private LayoutInflater inflater;

    public NearableListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.nearables = new ArrayList<>();
    }

    public void replaceWith(Collection<Nearable> newNearables) {
        this.nearables.clear();
        this.nearables.addAll(newNearables);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return nearables.size();
    }

    @Override
    public Nearable getItem(int position) {
        return nearables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view);
        return view;
    }

    private void bind(Nearable nearable, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.macTextView.setText(String.format("ID: %s (%s)", nearable.identifier, Utils.computeProximity(nearable).toString()));
        holder.majorTextView.setText("Major: " + nearable.region.getMajor());
        holder.minorTextView.setText("Minor: " + nearable.region.getMinor());
        holder.measuredPowerTextView.setText("MPower: " + nearable.power.powerInDbm);
        holder.rssiTextView.setText("RSSI: " + nearable.rssi);
        holder.imageView.setImageResource(R.drawable.nearables_generic);
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.nearable_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    static class ViewHolder {
        final TextView macTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView measuredPowerTextView;
        final TextView rssiTextView;
        final ImageView imageView;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            majorTextView = (TextView) view.findViewWithTag("major");
            minorTextView = (TextView) view.findViewWithTag("minor");
            measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
            rssiTextView = (TextView) view.findViewWithTag("rssi");
            imageView = (ImageView) view.findViewWithTag("image");
        }
    }
}
