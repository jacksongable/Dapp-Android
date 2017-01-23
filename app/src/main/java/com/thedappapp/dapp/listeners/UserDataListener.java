package com.thedappapp.dapp.listeners;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.app.Application;
import com.thedappapp.dapp.events.UserDataEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jackson on 1/18/17.
 */

public class UserDataListener {

    private static UserDataListener singleton;

    public static UserDataListener getDefault () {
        if (singleton == null)
            singleton = new UserDataListener(FirebaseAuth.getInstance().getCurrentUser().getUid());
        return singleton;
    }

    private String userId;

    private UserDataListener (String id) {
        userId = id;
    }

    private ValueEventListener groupChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDataEvent.Attributes attribute = UserDataEvent.Attributes.CURRENT_GROUP;
            String value = dataSnapshot.getValue(String.class);
            UserDataEvent event = new UserDataEvent(attribute, value);

            EventBus.getDefault().post(event);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener displayNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDataEvent.Attributes attribute = UserDataEvent.Attributes.DISPLAY_NAME;
            String value = dataSnapshot.getValue(String.class);
            UserDataEvent event = new UserDataEvent(attribute, value);

            EventBus.getDefault().post(event);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void enable () {
        Application.getApplication().getDatabaseReference(Application.References.USER).child("group").addValueEventListener(groupChangeListener);
        Application.getApplication().getDatabaseReference(Application.References.USER).child("first_name").addValueEventListener(displayNameListener);
    }

    public void disable () {
        Application.getApplication().getDatabaseReference(Application.References.USER).child("group").addValueEventListener(groupChangeListener);
        Application.getApplication().getDatabaseReference(Application.References.USER).child("first_name").addValueEventListener(displayNameListener);
    }

}
