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
    public void onPage1Interaction(CreateGroupPage1Fragment.RequestCode code) {
        switch (code) {
            case DISPATCH_CAMERA:
                dispatchCameraIfPossible();
                break;
            case DONE:
                finalizePage1();
                startPage2();
                break;
            default:
                throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        page1.onPictureTaken(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void dispatchCameraIfPossible () {
        if (App.hasFilePermissions()) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);

            //Intent camera = new Intent(this, CameraActivity.class);
            //startActivityForResult(camera, CAMERA_INTENT_REQUEST_CODE);

            //camera.dispatch();
            //page1.onPictureTaken(camera.getCapturedImageFile());
        }
        else
            ActivityCompat.requestPermissions(this, new String [] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, CAMERA_FILE_READ_WRITE_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length <= 0) {
            Log.w(TAG, "Permission result array has 0 indicies.");
        } else {
            switch (requestCode) {
                case CAMERA_FILE_READ_WRITE_REQUEST_CODE:
                    onFilePermissionsResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    break;
                case LOCATION_REQUEST_CODE:
                    onLocationPermissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal request code.");
            }
        }
    }

    private void onFilePermissionsResult (boolean granted) {
        if (granted) {
            dispatchCameraIfPossible();
            //camera.dispatch();
            //page1.onPictureTaken(camera.getCapturedImageFile());
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Oops! We need permission to take your selfie!")
                    .setMessage("In order to upload your selfie and it to your phone, Dapp needs file writing permissions. You can do this in Settings.")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            startActivity(APP_SETTINGS_INTENT);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.w(TAG, "File permission denied.");
                        }
                    }).show();
        }
    }

    private void onLocationPermissionResult (boolean granted) {
        if (granted)
            startLocationUpdates(group, code);
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Get featured on our map!!")
                    .setMessage("We'd like to use your location to feature your group on our map, so" +
                            " other Dapp users know you're nearby. You can give us permissions in Settings.")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            startActivity(APP_SETTINGS_INTENT);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.w(TAG, "Location permission denied. Saving without location.");
                            group.setLocationEnabled(false);
                            group.save(code);
                            startActivity(new Intent(CreateGroupActivity.this, MyGroupActivity.class));
                            finish();
                        }
                    }).show();
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

        group = factory.withName(name)
                .withBio(bio)
                .withLeaderId(App.me().getUid())
                .withLeaderName(App.me().getDisplayName())
                .withInterests(interests)
                .withPic(camera.getCapturedImagePath())
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
        //else if (gpsEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, listener);
        //}
       // else if (networkEnabled) {
            manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0L, 0f, listener);
        //}
    }

    private class LocUpdateListener implements LocationListener {
        private android.location.LocationManager mManager;

        private LocUpdateListener(android.location.LocationManager theManager) {
            mManager = theManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (App.hasLocationPermissions()) {
                mManager.removeUpdates(this);
                group.getLocation().put("latitude", location.getLatitude());
                group.getLocation().put("longitude", location.getLongitude());
                group.setLocationEnabled(true);
                group.save(code);
                startActivity(new Intent(CreateGroupActivity.this, MyGroupActivity.class));
                finish();
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