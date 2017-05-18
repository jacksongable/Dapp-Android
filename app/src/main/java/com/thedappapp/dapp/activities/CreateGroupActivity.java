package com.thedappapp.dapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Compressor;
import com.thedappapp.dapp.app.camera.DappCamera;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.fragments.CreateGroupPage1Fragment;
import com.thedappapp.dapp.fragments.CreateGroupPage2Fragment;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.group.GroupFactory;

import java.util.List;

public class CreateGroupActivity extends DappActivity
        implements CreateGroupPage1Fragment.Page1FragmentInteractionListener, CreateGroupPage2Fragment.Page2FragmentInteractionListener {

    private static final String TAG = CreateGroupActivity.class.getSimpleName();
    private static final int CAMERA_FILE_READ_WRITE_REQUEST_CODE = 0;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int CAMERA_INTENT_REQUEST_CODE = 3;
    private static final Intent APP_SETTINGS_INTENT;

    public static final String ACTION_CREATE;
    public static final String ACTION_EDIT;

    static {
        APP_SETTINGS_INTENT = new Intent();
        APP_SETTINGS_INTENT.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        APP_SETTINGS_INTENT.addCategory(Intent.CATEGORY_DEFAULT);
        APP_SETTINGS_INTENT.setData(Uri.parse("package:com.thedappapp.dapp"));
        APP_SETTINGS_INTENT.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        APP_SETTINGS_INTENT.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        APP_SETTINGS_INTENT.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        ACTION_CREATE = "com.thedappapp.dapp.activities.actions.CREATE_GROUP";
        ACTION_EDIT = "com.thedappapp.dapp.activities.actions.EDIT_GROUP";
    }


    private FragmentManager fragmentManager;
    private int fragment;
    private DappCamera camera;
    private CreateGroupPage1Fragment page1;
    private CreateGroupPage2Fragment page2;
    private Bundle page1Bundle, page2Bundle;
    private boolean editMode;
    private Group group;
    private SaveKeys code;

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
        camera = new DappCamera(this);

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
    public void onPage1Interaction() {
        finalizePage1();
        startPage2();
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
    public void onPage2Interaction() {
        finalizePage2();
        buildGroup();
    }

    protected void finalizePage2() {
        page2Bundle = page2.pullInfo();
    }

    protected void buildGroup() {
        String name = page1Bundle.getString("name");
        String bio = page1Bundle.getString("bio");
        String photo = page1Bundle.getString("photo");

        List<String> interests = page2Bundle.getStringArrayList("interests");

        GroupFactory factory = new GroupFactory();

        group = factory.withName(name)
                .withBio(bio)
                .withLeaderId(App.me().getUid())
                .withLeaderName(App.me().getDisplayName())
                .withInterests(interests)
                .withPic(photo)
                .build();

        if (editMode) code = SaveKeys.UPDATE;
        else code = SaveKeys.CREATE;




        if (App.hasLocationPermissions())
            startLocationUpdates(group, code);
        else
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
    }

    private void startLocationUpdates (Group group, SaveKeys code) {
        android.location.LocationManager manager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocUpdateListener listener = new LocUpdateListener(manager);

        boolean gpsEnabled = manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean networkEnabled = manager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

        if (!networkEnabled && !gpsEnabled) {
            Log.w(TAG, "Location not available. Saving without location.");
            group.setLocationEnabled(false);
            group.save(code);
            startActivity(new Intent(this, MyGroupActivity.class));
            finish();
        }
        manager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, listener);
        manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0L, 0f, listener);

    }

    private class LocUpdateListener implements LocationListener {
        private android.location.LocationManager mManager;

        private LocUpdateListener(android.location.LocationManager theManager) {
            mManager = theManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            mManager.removeUpdates(this);
            group.getLocation().put("latitude", location.getLatitude());
            group.getLocation().put("longitude", location.getLongitude());
            group.setLocationEnabled(true);
            group.save(code);
            startActivity(new Intent(CreateGroupActivity.this, MyGroupActivity.class));
            finish();
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {}
        @Override
        public void onProviderDisabled(String s) {}
    }
}