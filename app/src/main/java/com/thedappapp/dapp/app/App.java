package com.thedappapp.dapp.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.Request;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackson on 7/5/16.
 */
public final class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    public static final String PREFERENCES = "com.thedappapp.dapp.pref.PREFERENCES";

    private static App singleton;

    public static App getApp() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        FirebaseApp.initializeApp(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                super.set(imageView, uri, placeholder);
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });
    }

    public FirebaseUser me () {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public boolean hasUser () {
        return me() != null;
    }


    public DatabaseReference GROUPS;
    public DatabaseReference USER;
    public DatabaseReference USER_REQUEST;

    public void setHasCurrentGroup (boolean has) {
        SharedPreferences pref = App.getApp().getSharedPreferences(App.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("hasGroup", has);
        editor.commit();
    }

    public boolean hasCurrentGroup () {
        return getSharedPreferences(PREFERENCES, MODE_PRIVATE).getBoolean("hasGroup", false);
    }

    public static void requestLocationPermissions (final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Get featured on our map!");
        builder.setMessage("We'd like to use your location to feature your group on our map, so other Dapp users know you're nearby. What do you say?");
        builder.setPositiveButton("Sure thing!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(context, new String[] {
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION },
                        0);
            }
        });
        builder.setNegativeButton("No thanks!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Negative option clicked.");
            }
        });
        builder.create().show();
    }

    public static boolean hasLocationPermissions () {
        return
                (ContextCompat.checkSelfPermission(singleton, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        ==
                PackageManager.PERMISSION_GRANTED)

                        &&

                (ContextCompat.checkSelfPermission(singleton, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        ==
                PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasFilePermissions () {
        return
                (ContextCompat.checkSelfPermission(singleton, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==
                PackageManager.PERMISSION_GRANTED)

                        &&

                (ContextCompat.checkSelfPermission(singleton, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ==
                PackageManager.PERMISSION_GRANTED);

    }
}



