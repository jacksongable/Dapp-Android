package com.thedappapp.dapp.async;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.app.App;

/**
 * Created by jackson on 1/28/17.
 */

public class FriendsTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(@NonNull String... id) {
        App.getApp().REQUEST_ROOT.child(id[0]).child("status").setValue("Accepted");

        return null;
    }
}
