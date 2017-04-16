package com.thedappapp.dapp.objects.group;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.DappObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackson on 3/4/17.
 */

public class OldGroup extends DappObject {

    private Object deleted, created;
    private String name, uid;
    private Map<String, Double> location;

    public OldGroup () {}

    public OldGroup (Group old) {
        deleted = ServerValue.TIMESTAMP;
        created = old.getMeta().getCreated();
        uid = old.getMeta().getUid();
        name = old.getName();
        location = old.getLocation();
    }

    private OldGroup (Parcel in) {
        deleted = in.readValue(Object.class.getClassLoader());
        created = in.readValue(Object.class.getClassLoader());
        name = in.readString();
        uid = in.readString();
        location = in.readHashMap(HashMap.class.getClassLoader());
    }

    public Object getDeleted () {
        return deleted;
    }

    @Exclude
    public long getDeletedLong () {
        return (long) deleted;
    }

    public Object getCreated () {
        return created;
    }

    @Exclude
    public long getCreatedLong () {
        return (long) created;
    }

    public String getName () {
        return name;
    }

    public String getUid () {
        return uid;
    }

    public Map<String, Double> getLocation () {
        return location;
    }

    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("old_groups").child(App.getApp().me().getUid()).child(uid);

        if (code == SaveKeys.DELETE)
            ref.setValue(null);
        else ref.setValue(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(deleted);
        parcel.writeValue(created);
        parcel.writeString(name);
        parcel.writeString(uid);
        parcel.writeMap(location);
    }

    public static final Parcelable.Creator<OldGroup> CREATOR = new Parcelable.Creator<OldGroup>() {
        public OldGroup createFromParcel(Parcel in) {
            return new OldGroup(in);
        }

        public OldGroup[] newArray(int size) {
            return new OldGroup[size];
        }
    };
}
