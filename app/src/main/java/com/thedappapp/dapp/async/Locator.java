package com.thedappapp.dapp.async;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoLocation;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 10/18/16.
 */
public class Locator extends AsyncTask <Group, Void, Void> {

    private Context context;
    private Group mGroup;
    private DatabaseOperationCodes saveOperation;

    public Locator(Context context, DatabaseOperationCodes saveOperation) {
        this.context = context;
        this.saveOperation = saveOperation;
    }

    @Override
    protected Void doInBackground(@NonNull Group... groups) {
        mGroup = groups[0];

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocUpdateListener listener = new LocUpdateListener(manager);

        boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Looper.prepare();
        if (networkEnabled) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener);
        }
        if (gpsEnabled) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener);
        }
        Looper.loop();

        return null;
    }



    private class LocUpdateListener implements LocationListener {

        private LocationManager mManager;

        public LocUpdateListener(LocationManager theManager) {
            mManager = theManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            Looper.myLooper().quit();
            mManager.removeUpdates(this);
            mGroup.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
            mGroup.saveToFirebase(saveOperation);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }
}
