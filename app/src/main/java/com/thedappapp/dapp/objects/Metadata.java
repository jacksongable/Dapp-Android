package com.thedappapp.dapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackson on 1/11/17.
 */

public class Metadata implements Parcelable {

    private String uid;
    private Object created, updated;
    private Map<String, Object> misc_data;

    public Metadata () {}

    public Metadata (String uid, Object created, Object updated) {
        this.uid = uid;
        this.created = created;
        this.updated = updated;
        misc_data = new HashMap<>();
    }

    private Metadata(Parcel in) {
        uid = in.readString();
        created = in.readValue(Object.class.getClassLoader());
        updated = in.readValue(Object.class.getClassLoader());
    }

    public Object getCreated () {
        return created;
    }

    public Object getUpdated () {
        return updated;
    }

    @Exclude
    public long getCreatedLong () {
        return (long) created;
    }

    @Exclude
    public long getUpdatedLong () {
        return (long) updated;
    }

    public String getUid () {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public void setCreated (Object created) {
        this.created = created;
    }

    public void setUpdated (Object updated) {
        this.updated = updated;
    }

    public void addMiscellaneousData (String key, Object value) {
        misc_data.put(key, value);
    }

    public Object getMiscellaneousAttribute (String key) {
        return misc_data.get(key);
    }

    public Map<String, Object> getMisc_data() {
        return misc_data;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(uid);
        out.writeValue(created);
        out.writeValue(updated);
    }

    public static final Parcelable.Creator<Metadata> CREATOR = new Parcelable.Creator<Metadata>() {
        public Metadata createFromParcel(Parcel in) {
            return new Metadata(in);
        }

        public Metadata[] newArray(int size) {
            return new Metadata[size];
        }
    };


}
