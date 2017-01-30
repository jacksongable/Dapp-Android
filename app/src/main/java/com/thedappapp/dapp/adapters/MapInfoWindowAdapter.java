package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.activities.MapsActivity;
import com.thedappapp.dapp.objects.group.Group;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by jackson on 8/20/16.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private final Map<String, Group> map;
    private final View mView;
    private final Context mContext;

    public MapInfoWindowAdapter (MapsActivity activity, Map<String, Group> map) {
        mContext = activity;
        this.map = map;
        mView = activity.getLayoutInflater().inflate(R.layout.content_map_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Group selectedGroup = map.get(marker.getId());

        TextView name = (TextView) mView.findViewById(R.id.name);
        TextView bio = (TextView) mView.findViewById(R.id.bio);
        LinearLayout ll = (LinearLayout) mView.findViewById(R.id.interest_holder);
        ll.removeAllViews();

        name.setText(selectedGroup.getName());
        bio.setText(selectedGroup.getBio());

        if (selectedGroup.hasInterest("food")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_food);
            ll.addView(image);
        }
        if (selectedGroup.hasInterest("entertainment")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_events);
            ll.addView(image);
        }
        if (selectedGroup.hasInterest("music")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_music);
            ll.addView(image);
        }
        if (selectedGroup.hasInterest("gaming")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_gaming);
            ll.addView(image);
        }
        if (selectedGroup.hasInterest("party")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_party);
            ll.addView(image);
        }
        if (selectedGroup.hasInterest("sports")) {
            ImageView image = new ImageView(mContext);
            image.setImageResource(R.drawable.profile_sports);
            ll.addView(image);
        }

        return mView;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent (mContext, GroupDetailsActivity.class);
        intent.putExtra("group", map.get(marker.getId()));
        mContext.startActivity(intent);
    }
}
