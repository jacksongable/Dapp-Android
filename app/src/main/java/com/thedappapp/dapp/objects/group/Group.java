package com.thedappapp.dapp.objects.group;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.ArrayList;
import java.util.List;

import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.async.Locator;
import com.thedappapp.dapp.objects.DappObject;
import com.thedappapp.dapp.objects.Metadata;

/**
 * Created by jackson on 8/20/16.
 */
public class Group extends DappObject {

    private String name, bio, leaderId, photoPath;
    private List<String> interests;
    private GeoLocation location;

    public Group () {}

    Group (String name, String bio, String leaderId, List<String> interests) {
        this.name = name;
        this.bio = bio;
        this.leaderId = leaderId;
        this.interests = interests;
    }

    private Group(Parcel in) {
        name = in.readString();
        bio = in.readString();
        leaderId = in.readString();
        interests = new ArrayList<>();
        in.readStringList(interests);
        meta = in.readParcelable(Metadata.class.getClassLoader());

        double lat = in.readDouble();
        double lon = in.readDouble();

        location = new GeoLocation(lat, lon);
    }

    public void setName (String newName) {
        this.name = newName;
    }
    public void setBio (String newBio) {
        this.bio = newBio;
    }
    public void setInterest (String key, boolean value) {
        if (value && !interests.contains(key))
            interests.add(key);
        else if (!value && interests.contains(key))
            interests.remove(key);
    }

    public void setLocation (GeoLocation location) {
        this.location = location;
    }

    public String getName () {
        return this.name;
    }

    public String getBio () {
        return this.bio;
    }

    public String getLeaderId() {
        return this.leaderId;
    }

    public String getPhotoPath () {
        return photoPath;
    }

    public List<String> getInterests () {
        return interests;
    }

    @Exclude
    public boolean hasInterest (String key) {
        return interests.contains(key);
    }

    @Exclude
    public boolean isMine () {
        return leaderId.equals(App.getApp().me().getUid());
    }

    @Exclude
    @Override
    protected void saveInternal(@NonNull DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DO_NOTHING)
            return;

        if (meta == null && code != DatabaseOperationCodes.CREATE)
            throw new IllegalStateException("You must create the group object in the database before deleting or updating it.");

        else if (code == DatabaseOperationCodes.DELETE) {
            FirebaseDatabase.getInstance().getReference("groups").child(meta.getUid()).setValue(null);
            return;
        } else {
            DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("groups").push();
            switch (code) {
                case CREATE:
                    super.meta = new Metadata(groupReference.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
                    break;
                case UPDATE:
                    meta.setUpdated(ServerValue.TIMESTAMP);
                    break;
            }
            groupReference.setValue(this);
        }
    }

    public void fetchLocationAndSave(Context context, DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DELETE)
            save(code);
        else {
            Locator locator = new Locator(context, code);
            locator.execute(this);
        }
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
        out.writeStringList(interests);
        out.writeParcelable(meta, 0);
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
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