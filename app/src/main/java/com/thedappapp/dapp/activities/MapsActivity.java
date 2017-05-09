package com.thedappapp.dapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.adapters.MapInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends DappActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private MapInfoWindowAdapter adapter;
    private MapsListener listener;
    private Map<String, Marker> markerMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markerMap = new HashMap<>();
        listener = new MapsListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (App.getApp().hasLocationPermissions())
            mMap.setMyLocationEnabled(true);
        else App.requestLocationPermissions(this);

        FirebaseDatabase.getInstance().getReference("groups").addChildEventListener(listener);
        adapter = new MapInfoWindowAdapter(this, new HashMap<String, Group>());

        mMap.setInfoWindowAdapter(adapter);
        mMap.setOnInfoWindowClickListener(adapter);

        final LatLng MANHATTAN = new LatLng(40.782832, -73.965387);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MANHATTAN, 12));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private MarkerOptions getMarkerOptions (LatLng location, boolean isMine) {
        float[] hsv = new float[3];
        if (isMine)
            Color.colorToHSV(Color.parseColor("blue"), hsv);
        else
            Color.colorToHSV(Color.rgb(249, 177, 88), hsv);
        return new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
    }

    private class MapsListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            /*MapsDataWrapper wrapper = dataSnapshot.getValue(MapsDataWrapper.class);

            LatLng location = new LatLng(wrapper.getLatitude(), wrapper.getLongitude());
            Marker marker = mMap.addMarker(getMarkerOptions(location, wrapper.getOwnerId().equals(App.getApp().me().getUid())));
            markerMap.put(wrapper.getKey(), marker);
            adapter.put(marker.getId(), wrapper); */

            Group group = dataSnapshot.getValue(Group.class);
            if (group.hasLocation() && !group.getLeaderId().equals(App.me().getUid())) {
                LatLng location = new LatLng(group.getLocation().get("latitude"), group.getLocation().get("longitude"));
                Marker marker = mMap.addMarker(getMarkerOptions(location, group.getLeaderId().equals(App.me().getUid())));
                markerMap.put(group.getUid(), marker);
                adapter.put(marker.getId(), group);
            } else {
                Log.w(TAG, "No location data. Skipping...");
                return;
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Group group = dataSnapshot.getValue(Group.class);
            Marker marker = markerMap.get(group.getUid());
            adapter.remove(marker.getId());
            marker.remove();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Cancelled.");
        }
    }

    @Override
    public void onStop () {
        super.onStop();
        FirebaseDatabase.getInstance().getReference("groups").removeEventListener(listener);
    }



}
