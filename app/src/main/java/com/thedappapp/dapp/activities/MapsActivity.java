package com.thedappapp.dapp.activities;

import android.graphics.Color;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
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

        if (App.getApp().hasLocationPermissions())
            mMap.setMyLocationEnabled(true);
        else App.getApp().requestLocationPermissions(this);

        addGroupMarkers();

        final LatLng MANHATTAN = new LatLng(40.782832, -73.965387);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MANHATTAN, 12));
    }

    public void addGroupMarkers() {
        final Map<String, Group> hashMap = new HashMap<>();
/*
        GeoFire fire = new GeoFire(App.getApp().GROUPS);
        GeoQuery query = fire.queryAtLocation(App.getApp().getCurrentGroup().getLocation(), 100);
        GeoQueryListener listener = new GeoQueryListener();
        query.addGeoQueryEventListener(listener); */

        MapInfoWindowAdapter adapter = new MapInfoWindowAdapter(MapsActivity.this, hashMap);
        mMap.setInfoWindowAdapter(adapter);
        mMap.setOnInfoWindowClickListener(adapter);
    }

    private MarkerOptions getMarkerOptions (LatLng location, boolean isMine) {
        float[] hsv = new float[3];
        if (isMine)
            Color.colorToHSV(Color.parseColor("blue"), hsv);
        else
            Color.colorToHSV(Color.rgb(249, 177, 88), hsv);
        return new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
    }

    private class GeoQueryListener implements GeoQueryEventListener {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            /*Group group = iterator.next();
            GeoLocation point = group.getLocation();
            LatLng location = locationManager.withParseGeoPoint(point).asLatLng();

            Marker marker = mMap.addMarker(getMarkerOptions(location, group.isMine()));

            hashMap.put(marker.getId(), group); */
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    }

    @Override
    public void onStop () {
        super.onStop();

    }

}
