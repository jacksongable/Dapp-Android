package com.thedappapp.dapp.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.thedappapp.dapp.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.bumptech.glide.Glide;
import com.thedappapp.dapp.activities.CreateGroupActivity;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.objects.Notification;
import com.thedappapp.dapp.objects.chat.ChatMetaShell;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.services.NotificationService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jackson on 7/5/16.
 */
public final class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    public static final String PREFERENCES = "com.thedappapp.dapp.pref.PREFERENCES";

    private static App singleton;

    private Intent bellService;

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

        /*
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.thedappapp.dapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        } */
    }

    public static Intent bellService () {
        if (singleton.bellService == null)
            singleton.bellService = new Intent(singleton, NotificationService.class);
        return singleton.bellService;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(this.bellService);
    }

    public static FirebaseUser me() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void handleDbErr(DatabaseError error) {
        StringBuilder builder = new StringBuilder();
        builder.append("Database Exception: ");
        builder.append(error.getMessage());
        builder.append("\nDetails: ");
        builder.append(error.getDetails().isEmpty() ? "No details." : error.getDetails());
        builder.append("\nStack Trace: ");
        builder.append(Log.getStackTraceString(error.toException()));

        Log.e(TAG, builder.toString());
    }

    public static void exception(String tag, Exception e) {
        Log.e(tag, Log.getStackTraceString(e));
    }

    public static boolean hasUser () {
        return me() != null;
    }


    public void setCurrentGroupUid (String uid) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("gid", uid);
        editor.commit();
    }

    public String getCurrentGroupUidOffline () {
        return getSharedPreferences(PREFERENCES, MODE_PRIVATE).getString("gid", null);
    }

    public void setCurrentGroupNameOffline (String name) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("group_name", name);
        editor.commit();
    }

    public String getCurrentGroupNameOffline () {
        return getSharedPreferences(PREFERENCES, MODE_PRIVATE).getString("group_name", null);
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

    public static boolean hasCameraPermission () {
        return
                (ContextCompat.checkSelfPermission(singleton, Manifest.permission.CAMERA)
                        ==
                        PackageManager.PERMISSION_GRANTED);

    }

    public static void acceptRequest (final Group theGroup) {
        //Create ChatMetaShell
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats").push();
        ChatMetaShell shell = new ChatMetaShell(reference.getKey(), App.getApp().getCurrentGroupNameOffline(), App.me().getUid());
        shell.save(SaveKeys.CREATE, theGroup.getLeaderId());
        shell = new ChatMetaShell(reference.getKey(), theGroup.getName(), theGroup.getLeaderId());
        shell.save(SaveKeys.CREATE, App.me().getUid());

        //Delete pending_requests/incoming entry
        FirebaseDatabase.getInstance().getReference("users")
                .child(App.me().getUid()).child("pending_requests/incoming")
                .child(theGroup.getUid()).setValue(null);

        //Delete pending_requests/outgoing entry from other user's node
        FirebaseDatabase.getInstance().getReference("users")
                .child(theGroup.getLeaderId()).child("pending_requests/outgoing")
                .child(App.getApp().getCurrentGroupUidOffline()).setValue(null);

        //Add to "friends list" of both users' nodes
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid())
                .child("friends").child(theGroup.getUid()).setValue(true);
        FirebaseDatabase.getInstance().getReference("users").child(theGroup.getLeaderId())
                .child("friends").child(App.getApp().getCurrentGroupUidOffline()).setValue(true);

        //Send notification to other user
        FirebaseDatabase.getInstance().getReference("groups").child(App.getApp().getCurrentGroupUidOffline()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group current = dataSnapshot.getValue(Group.class);
                Notification notification = new Notification(current.getName().concat(" accepted your chat request!")
                        , theGroup.getLeaderId(), Notification.Types.REQUEST_ACCEPTED, current.getPhoto());
                notification.putMetadata("conversation_key", reference.getKey());
                notification.save(SaveKeys.CREATE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public static void sendRequest (final Group theGroup, final DappActivity context) {
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String MY_GROUP = dataSnapshot.getValue(String.class);
                if (MY_GROUP == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Whoops!");
                    builder.setMessage("Sorry, you need to create a group before you can chat.");
                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(singleton, CreateGroupActivity.class);
                            intent.setAction(CreateGroupActivity.ACTION_CREATE);
                            singleton.startActivity(intent);
                        }
                    });
                    builder.show();
                    return;
                }

                FirebaseDatabase.getInstance().getReference("groups").child(MY_GROUP).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group current = dataSnapshot.getValue(Group.class);

                        Notification notification = new Notification(current.getName()
                                .concat(" sent you a chat request!"),
                                theGroup.getLeaderId(),
                                Notification.Types.NEW_REQUEST, current.getPhoto());
                        notification.save(SaveKeys.CREATE);

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(App.me().getUid())
                                .child("pending_requests/outgoing")
                                .child(theGroup.getUid())
                                .setValue(true);

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(theGroup.getLeaderId())
                                .child("pending_requests/incoming")
                                .child(MY_GROUP)
                                .setValue(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        App.handleDbErr(databaseError);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                App.handleDbErr(databaseError);
            }
        });


    }
}



