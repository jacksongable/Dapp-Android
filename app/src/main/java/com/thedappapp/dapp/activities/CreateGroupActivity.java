package com.thedappapp.dapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Camera;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.fragments.CreateGroupPage1Fragment;
import com.thedappapp.dapp.fragments.CreateGroupPage2Fragment;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.group.GroupFactory;

import java.util.List;

public class CreateGroupActivity extends DappActivity
        implements CreateGroupPage1Fragment.Page1FragmentInteractionListener, CreateGroupPage2Fragment.Page2FragmentInteractionListener {

    private static final String TAG = CreateGroupActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    private int fragment;
    private Camera camera;
    private CreateGroupPage1Fragment page1;
    private CreateGroupPage2Fragment page2;
    private Bundle page1Bundle, page2Bundle;
    private boolean editMode;

    public static final String ACTION_EDIT = "com.thedappapp.dapp.activities.actions.EDIT_GROUP";
    public static final String ACTION_CREATE = "com.thedappapp.dapp.activities.actions.CREATE_GROUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        String action = getIntent().getAction();

        if (ACTION_CREATE.equals(action))
            editMode = false;
        else if (ACTION_EDIT.equals(action))
            editMode = true;

        fragmentManager = getSupportFragmentManager();
        fragment = R.id.fragment;
        camera = new Camera(this);

        if (editMode) {
            page1 = CreateGroupPage1Fragment.newInstance((Group) getIntent().getExtras().getParcelable("edit"));
            page2 = CreateGroupPage2Fragment.newInstance((Group) (getIntent().getExtras().getParcelable("edit")));
        } else {
            page1 = CreateGroupPage1Fragment.newInstance(null);
            page2 = CreateGroupPage2Fragment.newInstance(null);
        }

        startPage1();
    }

    protected void startPage1() {
        fragmentManager.beginTransaction()
                .replace(fragment, page1)
                .commitAllowingStateLoss();
    }

    @Override
    public void onPage1Interaction(CreateGroupPage1Fragment.RequestCode code) {
        switch (code) {
            case DISPATCH_CAMERA:
                page1.onPictureTaken();
                camera.dispatch();
                break;
            case DONE:
                finalizePage1();
                startPage2();
                break;
            default:
                throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    protected void finalizePage1() {
        page1Bundle = page1.pullInfo();
    }

    protected void startPage2() {
        fragmentManager.beginTransaction()
                .replace(fragment, page2)
                .commitAllowingStateLoss();
    }

    @Override
    public void onPage2Interaction(CreateGroupPage2Fragment.RequestCode code) {
        switch (code) {
            case DONE:
                finalizePage2();
                buildGroup();
                break;
            default:
                throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    protected void finalizePage2() {
        page2Bundle = page2.pullInfo();
    }

    protected void buildGroup() {
        String name = page1Bundle.getString("name");
        String bio = page1Bundle.getString("bio");

        List<String> interests = page2Bundle.getStringArrayList("interests");

        GroupFactory factory = new GroupFactory();

        final Group group = factory.withName(name)
                .withBio(bio)
                .withLeaderId(App.getApp().me().getUid())
                .withLeaderName(App.getApp().me().getDisplayName())
                .withInterests(interests)
                .withPic(camera.getCapturedImagePath())
                .build();

        final SaveKeys code;
        if (editMode) code = SaveKeys.UPDATE;
        else code = SaveKeys.CREATE;




        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)
            startLocationUpdates(group, code);

        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Get featured on our map!");
            builder.setMessage("We'd like to use your location to feature your group on our map, so other Dapp users know you're nearby. What do you say?");
            builder.setPositiveButton("Sure thing!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(CreateGroupActivity.this, new String[] {
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION },
                            0);
                    if (ContextCompat.checkSelfPermission(CreateGroupActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(CreateGroupActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED)
                        startLocationUpdates(group, code);
                }
            });
            builder.setNegativeButton("No thanks!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(TAG, "Negative option clicked.");
                    group.save(code);
                    finish();
                }
            });
            builder.create().show();
/*
            if (App.getApp().hasLocationPermissions())
                startLocationUpdates(group, code);
            else {
                Log.w(TAG, "Location permission denied by user. Saving without location.");
                group.save(code);
                finish();
            } */
        }
    }

    private void startLocationUpdates (Group group, SaveKeys code) {
        android.location.LocationManager manager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocUpdateListener listener = new LocUpdateListener(manager, group, code);

        boolean gpsEnabled = manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean networkEnabled = manager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

        if (!networkEnabled && !gpsEnabled) {
            Log.w(TAG, "Location not available. Saving without location.");
            group.save(code);
            finish();
        }
        else if (gpsEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, listener);
        }
        else if (networkEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0L, 0f, listener);
        }
    }

    private class LocUpdateListener implements LocationListener {
        private android.location.LocationManager mManager;
        private Group g;
        private SaveKeys saveCode;

        private LocUpdateListener(android.location.LocationManager theManager, Group g, SaveKeys saveCode) {
            mManager = theManager;
            this.g = g;
            this.saveCode = saveCode;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (ContextCompat.checkSelfPermission(CreateGroupActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(CreateGroupActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                mManager.removeUpdates(this);
                //setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
                g.getLocation().put("latitude", location.getLatitude());
                g.getLocation().put("longitude", location.getLongitude());
                g.save(saveCode);
                finish(); //????????????????????????????????????????
            }
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {}
        @Override
        public void onProviderDisabled(String s) {}
    }
}