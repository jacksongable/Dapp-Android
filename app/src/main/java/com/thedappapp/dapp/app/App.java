package com.thedappapp.dapp.app;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.Request;
import com.thedappapp.dapp.services.DatabaseInitService;
import android.support.multidex.MultiDexApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackson on 7/5/16.
 */
public final class App extends MultiDexApplication {

    private static App singleton;

    public static App getApp() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        Intent initUserDb = new Intent(this, DatabaseInitService.class);
        initUserDb.setAction(DatabaseInitService.GENERATE_USER_DB_REFERENCE);
        startService(initUserDb);

        Intent initGroupDb = new Intent(this, DatabaseInitService.class);
        initGroupDb.setAction(DatabaseInitService.GENERATE_CURRENT_GROUP_DB_REFERENCE);
        startService(initGroupDb);
    }


    public FirebaseUser me () {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private volatile DatabaseReference CURRENT_USER;
    private volatile DatabaseReference CURRENT_GROUP;
    private volatile Group currentGroup;

    private final Object dbReferenceLock = new Object();
    private final Object currentGroupLock = new Object();

    public final DatabaseReference GROUP_ROOT = FirebaseDatabase.getInstance().getReference("groups");
    public final DatabaseReference USER_ROOT = FirebaseDatabase.getInstance().getReference("user");
    public final DatabaseReference CONVO_ROOT = FirebaseDatabase.getInstance().getReference("chat");
    public final DatabaseReference USER_REQUEST_ROOT = FirebaseDatabase.getInstance().getReference("user-requests").child(me().getUid());
    public final DatabaseReference REQUEST_ROOT = FirebaseDatabase.getInstance().getReference("requests");

    private final RequestStorage requestStorage = new RequestStorage();
    private final ChatStorage chatStorage = new ChatStorage();


    public enum References {
        USER,
        GROUP
    }

    public DatabaseReference getDatabaseReference (References code) {
        synchronized (dbReferenceLock) {
            switch (code) {
                case USER:
                    return CURRENT_USER;
                case GROUP:
                    return CURRENT_GROUP;
                default:
                    throw new IllegalArgumentException("Illegal reference code. Check the switch.");
            }
        }
    }

    public void setDatabaseReference (References code, DatabaseReference ref) {
        synchronized (dbReferenceLock) {
            switch (code) {
                case USER:
                    CURRENT_USER = ref;
                    break;
                case GROUP:
                    CURRENT_GROUP = ref;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal reference code. Check the switch.");
            }
        }
    }

    public Group getCurrentGroup () {
        synchronized (currentGroupLock) {
            return currentGroup;
        }
    }

    public void setCurrentGroup (Group g) {
        synchronized (currentGroupLock) {
            currentGroup = g;
        }
    }

    public boolean hasCurrentGroup () {
        return currentGroup != null;
    }

    public ChatStorage getChatStorage () {
        return chatStorage;
    }

    public RequestStorage getRequestStorage () {
        return requestStorage;
    }

    public void sendRequest (Request request) {
        Map<String, Object> val;
        DatabaseReference outgoing = FirebaseDatabase.getInstance()
                                                     .getReference("user-requests")
                                                     .child(me().getUid())
                                                     .child("outgoing-pending")
                                                     .push();
        val = new HashMap<>();
        val.put("to", request.getTo());
        val.put("id", request.getMeta().getUid());
        outgoing.setValue(val);

        DatabaseReference incoming = FirebaseDatabase.getInstance()
                                                     .getReference("user-requests")
                                                     .child(request.getTo())
                                                     .child("incoming-pending")
                                                     .push();
        val = new HashMap<>();
        val.put("from", request.getFrom());
        val.put("id", request.getMeta().getUid());
        incoming.setValue(val);

        DatabaseReference requests = FirebaseDatabase.getInstance()
                                                     .getReference("requests")
                                                     .push();
        requests.setValue(request);
    }
}



