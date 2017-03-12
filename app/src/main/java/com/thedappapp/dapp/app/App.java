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

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

    public static final String USER_DATA = "com.thedappapp.dapp.pref.USER_DATA";

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




    private volatile Group currentGroup;

    private final Object currentGroupLock = new Object();

    public DatabaseReference GROUPS;
    public DatabaseReference USER;
    public DatabaseReference CHAT;
    public DatabaseReference USER_REQUEST;

    private final RequestStorage requestStorage = new RequestStorage();
    private final ChatStorage chatStorage = new ChatStorage();


    public Group getCurrentGroup () {
        synchronized (currentGroupLock) {
            return currentGroup;
        }
    }

    public void setCurrentGroup (Group g) {
        synchronized (currentGroupLock) {
            currentGroup = g;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(me().getUid()).child("group");
            if (g == null)
                ref.setValue(null);
            else ref.setValue(g.getMeta().getUid());
        }
    }

    public boolean hasCurrentGroup () {
        return currentGroup != null;
    }

    public ChatStorage getChatStorage () {
        return chatStorage;
    }

    public RequestStorage getRequestStorage () {
        return requestStorage;
    }

    public void sendRequest (Request request) {
        Map<String, Object> val;
        DatabaseReference outgoing = FirebaseDatabase.getInstance()
                                                     .getReference("requests")
                                                     .child(me().getUid())
                                                     .child("outgoing-pending")
                                                     .push();
        val = new HashMap<>();
        val.put("to", request.getTo());
        val.put("id", request.getMeta().getUid());
        outgoing.setValue(val);

        DatabaseReference incoming = FirebaseDatabase.getInstance()
                                                     .getReference("requests")
                                                     .child(request.getTo())
                                                     .child("incoming-pending")
                                                     .push();
        val = new HashMap<>();
        val.put("from", request.getFrom());
        val.put("id", request.getMeta().getUid());
        incoming.setValue(val);
    }

    public void requestLocationPermissions (final Activity context) {
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

    public boolean hasLocationPermissions () {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }

    public void requestFilePermissions (final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("We need permission to take your selfie.");
        builder.setMessage("In order to take a selfie of your group, we need file access permission, so that we can save your photo.");
        builder.setPositiveButton("You got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(context, new String [] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Negative option clicked.");
            }
        });
        builder.create().show();
    }

    public boolean hasFilePermissions () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }
}



