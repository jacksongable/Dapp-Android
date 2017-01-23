package com.thedappapp.dapp.app;

import android.content.Intent;
import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.services.DatabaseInitService;

/**
 * Created by jackson on 7/5/16.
 */
public final class Application extends android.support.multidex.MultiDexApplication {

    private static Application singleton;

    public static Application getApplication () {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        Intent init = new Intent(this, DatabaseInitService.class);

        init.setAction(DatabaseInitService.GENERATE_USER_DB_REFERENCE);
        startService(init);

        init.setAction(DatabaseInitService.GENERATE_CURRENT_GROUP_DB_REFERENCE);
        startService(init);
    }

    private volatile DatabaseReference CURRENT_USER;
    private volatile DatabaseReference CURRENT_GROUP;
    private volatile Group currentGroup;

    public enum References {
        USER,
        GROUP
    }

    public DatabaseReference getDatabaseReference (References code) {
        switch (code) {
            case USER:
                return CURRENT_USER;
            case GROUP:
                return CURRENT_GROUP;
            default: throw new IllegalArgumentException("Illegal reference code. Check the switch.");
        }
    }

    public void setDatabaseReference (References code, DatabaseReference ref) {
        switch (code) {
            case USER:
                CURRENT_USER = ref;
                break;
            case GROUP:
                CURRENT_GROUP = ref;
                break;
            default: throw new IllegalArgumentException("Illegal reference code. Check the switch.");
        }
    }

    public Group getCurrentGroup () {
        return currentGroup;
    }

    public void setCurrentGroup (Group g) {
        currentGroup = g;
    }

    public boolean hasCurrentGroup () {
        return currentGroup != null;
    }
}



