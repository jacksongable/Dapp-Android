package com.thedappapp.dapp.objects;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.thedappapp.dapp.app.DatabaseOperationCodes;

/**
 * Created by jackson on 1/22/17.
 */

public abstract class DappObject implements Parcelable {

    protected Metadata meta;

    public Metadata getMeta () {
        return meta;
    }

    public final void save (@NonNull DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DO_NOTHING)
            return;
        else if (meta == null && code != DatabaseOperationCodes.CREATE)
            throw new IllegalStateException("You must create the object in the database before deleting or updating it.");
        else saveInternal(code);
    }

    protected abstract void saveInternal(@NonNull DatabaseOperationCodes code);
}
