package com.thedappapp.dapp.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.thedappapp.dapp.app.App;

public class FcmTokenService extends FirebaseInstanceIdService {

    private static final String TAG = FcmTokenService.class.getSimpleName();

    public FcmTokenService() {
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        SharedPreferences preferences = getSharedPreferences(App.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("push_token", refreshedToken);
        editor.commit();

        if (App.getApp().hasUser()) {
            Intent register = new Intent(this, TokenUploadService.class);
            register.setAction(TokenUploadService.REGISTER_TOKEN);
            startService(register);
        }
        else Log.w(TAG, "Cannot register device token until user signs in. Token saved locally.");

    }

}
