package com.thedappapp.dapp.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.thedappapp.dapp.activities.CreateGroupActivity;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 3/28/17.
 */

public class GroupLocationUploader extends AsyncTask<Group, Void, Void> {

    private static final String TAG = GroupLocationUploader.class.getSimpleName();

    private Group group;
    private DappActivity context;
    private SaveKeys code;

    public GroupLocationUploader(DappActivity context, SaveKeys code) {
        this.context = context;
        this.code = code;
    }

    @Override
    protected Void doInBackground(Group... groups) {
        if (hasAuth()) onAuthGranted();
        else
            getAuth();

        return null;
    }

    private boolean hasAuth() {
        return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                ==
                PackageManager.PERMISSION_GRANTED)

                &&

                (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        ==
                        PackageManager.PERMISSION_GRANTED);
    }


    private void getAuth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Get featured on our map!");
        builder.setMessage("We'd like to use your location to feature your group on our map, so other Dapp users know you're nearby. What do you say?");
        builder.setPositiveButton("Sure thing!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(context, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
                if (hasAuth())
                    onAuthGranted();
            }
        });
        builder.setNegativeButton("No thanks!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Negative option clicked.");
                group.save(code);
            }
        });
        builder.create().show();
    }

    private void onAuthGranted() {
        android.location.LocationManager manager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocUpdateListener listener = new LocUpdateListener(manager);

        boolean gpsEnabled = manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean networkEnabled = manager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

        if (!networkEnabled && !gpsEnabled) {
            Log.w(TAG, "Location not available. Saving without location.");
            group.save(code);
        } else if (gpsEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, listener);
        } else if (networkEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0L, 0f, listener);
        }
    }


    private class LocUpdateListener implements LocationListener {

        private android.location.LocationManager mManager;

        private LocUpdateListener(android.location.LocationManager theManager) {
            mManager = theManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                mManager.removeUpdates(this);

                group.getLocation().put("latitude", location.getLatitude());
                group.getLocation().put("longitude", location.getLongitude());
                group.save(code);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }


}
