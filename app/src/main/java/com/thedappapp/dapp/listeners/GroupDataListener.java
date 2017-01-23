package com.thedappapp.dapp.listeners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.app.Application;
import com.thedappapp.dapp.events.GroupDataEvent;
import com.thedappapp.dapp.events.UserDataEvent;
import com.thedappapp.dapp.objects.group.Group;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by jackson on 1/18/17.
 */

public class GroupDataListener {

    private static GroupDataListener singleton;

    public static GroupDataListener getDefault () {
        if (singleton == null)
            singleton = new GroupDataListener(Application.getApplication().getDatabaseReference(Application.References.GROUP));
        return singleton;
    }

    private DatabaseReference mReference;
    private boolean isEnabled;

    private GroupDataListener (DatabaseReference ref) {
        mReference = ref;
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Group group = dataSnapshot.getValue(Group.class);
            EventBus.getDefault().post(new GroupDataEvent(group));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void enable () {
        isEnabled = true;
        mReference.addValueEventListener(listener);
        EventBus.getDefault().register(this);
    }

    public void disable () {
        isEnabled = false;
        mReference.removeEventListener(listener);
        EventBus.getDefault().unregister(this);
    }

    private void refresh () {
        disable();
        enable();
    }

    public boolean isEnabled () {
        return isEnabled;
    }

    @Subscribe
    public void onGroupUidChanged (UserDataEvent event) {
        if (event.getChangedAttribute() != UserDataEvent.Attributes.CURRENT_GROUP)
            return;

        mReference = FirebaseDatabase.getInstance().getReference("groups").child(event.getAttributeValue());
        refresh();
        Application.getApplication().setDatabaseReference(Application.References.GROUP, mReference);
    }
}
