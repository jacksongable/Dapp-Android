package com.thedappapp.dapp.objects;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.thedappapp.dapp.app.DatabaseOperationCodes;

/**
 * Created by jackson on 1/22/17.
 */

public abstract class AbstractFirebaseObject implements Parcelable {

    protected Metadata meta;

    public Metadata getMeta () {
        return meta;
    }

    public abstract void saveToFirebase (@NonNull DatabaseOperationCodes code);
}
