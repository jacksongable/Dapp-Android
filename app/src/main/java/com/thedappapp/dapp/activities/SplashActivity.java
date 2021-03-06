package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.thedappapp.dapp.interfaces.NoToolbar;

public class SplashActivity extends DappActivity implements NoToolbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent main;
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            main = new Intent(this, SignInActivity.class);
        else main = new Intent(this, MainFeedActivity.class);

        startActivity(main);
        finish();
    }
}
