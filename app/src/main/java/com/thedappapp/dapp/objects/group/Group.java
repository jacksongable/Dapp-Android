package com.thedappapp.dapp.objects.group;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Compressor;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.DappObject;
import com.thedappapp.dapp.objects.Metadata;

/**
 * Created by jackson on 8/20/16.
 */
public class Group extends DappObject implements Parcelable {

    @Exclude
    private static final String TAG = Group.class.getSimpleName();

    private String name, bio, leaderId, leaderName, photo;
    private Map<String, Boolean> interests;
    private Map<String, Double> location;
    private boolean onMap;

    public Group () {}

    Group (String name, String bio, String leaderId, String leaderName, Map<String, Boolean> interests, String picPath) {
        this.name = name;
        this.bio = bio;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.interests = interests;
        this.photo = picPath;
        location = new HashMap<>();
        onMap = false;
    }

    private Group(Parcel in) {
        name = in.readString();
        bio = in.readString();
        leaderId = in.readString();
        interests = new HashMap<>();
        in.readMap(interests, Map.class.getClassLoader());
        meta = in.readParcelable(Metadata.class.getClassLoader());

        //double lat = in.readDouble();
        //double lon = in.readDouble();
        location = new HashMap<>();
        in.readMap(location, HashMap.class.getClassLoader());
        //location.put("latitude", lat);
        //location.put("longitude", lon);
        //location = new GeoLocation(lat, lon);
    }

    public void setName (String newName) {
        this.name = newName;
    }
    public void setBio (String newBio) {
        this.bio = newBio;
    }

    public void setInterest (String key, boolean value) {
        if (value && !interests.containsKey(key))
            interests.put(key, true);
        else if (!value && interests.containsKey(key))
            interests.remove(key);
    }

    /* public void setLocation (GeoLocation location) {
        this.location = location;
    } */

    public String getName () {
        return this.name;
    }

    public String getBio () {
        return this.bio;
    }

    public String getLeaderId() {
        return this.leaderId;
    }

    public String getLeaderName () {
        return this.leaderName;
    }

    public Map<String, Double> getLocation () {
        return location;
    }

    public String getPhoto() {
        return photo;
    }

    @Exclude
    public List<String> getInterestsAsList () {
        return new ArrayList<String>(interests.keySet());
    }

    public Map<String, Boolean> getInterests() {
        return this.interests;
    }

    /*
    public GeoLocation getLocationAsGeoFire () {
        return location;
    } */

    @Exclude
    public boolean hasInterest (String key) {
        return interests.containsKey(key);
    }

    @Exclude
    public boolean hasLocation () {
        if (location != null) return !location.isEmpty();;
        return false;
    }

    @Exclude
    public boolean isMine () {
        return leaderId.equals(App.getApp().me().getUid());
    }

    public void setLocationEnabled (boolean enabled) {
        onMap = enabled;
    }

    public boolean onMap () {
        return onMap;
    }

    @Exclude
    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.DELETE) {
            FirebaseDatabase.getInstance().getReference("groups").child(meta.getUid()).setValue(null);
            FirebaseDatabase.getInstance().getReference("users").child(App.getApp().me().getUid()).child("group").setValue(null);
            OldGroup old = new OldGroup(this);
            old.save(SaveKeys.CREATE);
            App.getApp().setHasCurrentGroup(false);
            App.getApp().setCurrentGroupUid(null);
            App.getApp().setCurrentGroupNameOffline(null);
        }

        else {
            DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("groups").push();
            switch (code) {
                case CREATE:
                    super.meta = new Metadata(groupReference.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
                    break;
                case UPDATE:
                    meta.setUpdated(ServerValue.TIMESTAMP);
                    break;
            }

            try {
                this.photo = uploadPhoto(new BufferedInputStream(new FileInputStream(Compressor.compress(photo))));;
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            finally {
                groupReference.setValue(this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Success.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                });
                App.getApp().setHasCurrentGroup(true);
                App.getApp().setCurrentGroupUid(super.meta.getUid());
                App.getApp().setCurrentGroupNameOffline(name);
                FirebaseDatabase.getInstance().getReference("users").child(App.getApp().me().getUid()).child("group").setValue(meta.getUid());
            }
        }
    }

    private String uploadPhoto (InputStream stream) {
        StorageReference reference = FirebaseStorage.getInstance()
                                                    .getReference("group-photos")
                                                    .child(App.getApp().me().getUid())
                                                    .child(super.meta.getUid());
        reference.putStream(stream);
        return reference.toString();
    }

    @Exclude
    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Group)) return false;
        return getMeta().getUid().equals(((Group) o).getMeta().getUid());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeString(name);
        out.writeString(bio);
        out.writeString(leaderId);
        out.writeMap(interests);
        out.writeParcelable(meta, 0);
        //out.writeDouble(location.latitude);
        //out.writeDouble(location.longitude);
        out.writeMap(location);
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };


}