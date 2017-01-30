package com.thedappapp.dapp.objects;

import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by jackson on 1/24/17.
 */

public class PhotoReference<T> {

    private T photo;

    public PhotoReference (StorageReference reference) {
        photo = (T) reference;
    }

    public PhotoReference (File file) {
        photo = (T) file;
    }

    public T getTypedPhoto  () {
        return photo;
    }
}
