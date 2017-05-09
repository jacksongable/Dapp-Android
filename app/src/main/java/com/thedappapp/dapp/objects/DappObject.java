package com.thedappapp.dapp.objects;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.thedappapp.dapp.app.SaveKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackson on 1/22/17.
 */

public abstract class DappObject {

    private final Map<String, Object> meta;
    //protected Metadata meta;

    /*public Metadata getMeta () {
        return meta;
    } */

    protected DappObject () {
        meta = new HashMap<>();
    }

    public String getUid () {
        return meta.get("uid") != null ? (String) meta.get("uid") : null;
    }

    public Map<String, Object> getMeta () {
        return meta;
    }

    public void putMetadata (String key, Object value) {
        meta.put(key, value);
    }

    @Exclude
    public Object getData (String key) {
        return meta.get(key);
    }

    public final void save (@NonNull SaveKeys code) {
        if (code == SaveKeys.DO_NOTHING)
            return;
        else if (meta == null && code != SaveKeys.CREATE)
            throw new IllegalStateException("You must create the object in the database before deleting or updating it.");
        else saveInternal(code);
    }

    protected abstract void saveInternal(@NonNull SaveKeys code);
}
