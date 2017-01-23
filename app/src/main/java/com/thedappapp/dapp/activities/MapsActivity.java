package com.thedappapp.dapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseGeoPoint;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.adapters.MapInfoWindowAdapter;
import com.thedappapp.dapp.app.Dapp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends DappActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        addGroupMarkers();

        // Add a marker in Sydney and move the camera
        final LatLng MANHATTAN = new LatLng(40.782832, -73.965387);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MANHATTAN, 12));
    }

    public void addGroupMarkers() {
        final LocationManager locationManager = new LocationManager(this);
        final Map<String, Group> hashMap = new HashMap<>();

        Log.i(TAG, "Querying database...");
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("active", true);
        query.whereEqualTo("map_active", true);
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (list == null) return;
                Iterator<Group> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Group group = iterator.next();
                    ParseGeoPoint point = group.getGroupLocation();
                    LatLng location = locationManager.withParseGeoPoint(point).asLatLng();

                    Marker marker = mMap.addMarker(getMarkerOptions(location, group.isMine()));
                    hashMap.put(marker.getId(), group);
                }

                MapInfoWindowAdapter adapter = new MapInfoWindowAdapter(MapsActivity.this, hashMap);
                mMap.setInfoWindowAdapter(adapter);
                mMap.setOnInfoWindowClickListener(adapter);
            }
        });
    }

    private MarkerOptions getMarkerOptions (LatLng location, boolean isMine) {
        float[] hsv = new float[3];
        if (isMine)
            Color.colorToHSV(Color.parseColor("blue"), hsv);
        else
            Color.colorToHSV(Color.rgb(249, 177, 88), hsv);
        return new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
    }

}
