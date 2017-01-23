package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;

import com.thedappapp.dapp.interfaces.NoToolbar;

public class SplashActivity extends DappActivity implements NoToolbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
    }
}
