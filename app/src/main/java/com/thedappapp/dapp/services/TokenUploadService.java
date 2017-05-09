package com.thedappapp.dapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.app.App;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TokenUploadService extends IntentService {

    private static final String TAG = TokenUploadService.class.getSimpleName();

    public static final String REGISTER_TOKEN = "com.thedappapp.dapp.services.action.REGISTER_TOKEN";
    public static final String DELETE_TOKEN = "com.thedappapp.dapp.services.action.DELETE_TOKEN";

    public TokenUploadService() {
        super("TokenUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!App.getApp().hasUser()) throw new IllegalStateException("User must be signed in before this service can be invoked.");

        if (intent != null) {
            final String action = intent.getAction();
            if (REGISTER_TOKEN.equals(action)) {
                handleRegisterToken();
            } else if (DELETE_TOKEN.equals(action)) {
                handleDeleteToken();
            }
        }
    }

    private void handleRegisterToken() {
        SharedPreferences preferences = getSharedPreferences(App.PREFERENCES, MODE_PRIVATE);
        String token = preferences.getString("push_token", "null");

        if (token.equals("null")) {
            Log.w(TAG, "No token in preferences. Token must be generated by FcmTokenService first. Exiting service...");
            return;
        }

        else {
            FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("push_token").setValue(token);
            Log.d(TAG, "Registered token to server.");
        }
    }

    private void handleDeleteToken() {
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("device_token").setValue(null);
        Log.d(TAG, "Purged token from server.");
    }
}
