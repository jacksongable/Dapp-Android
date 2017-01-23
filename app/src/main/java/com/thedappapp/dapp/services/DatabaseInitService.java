package com.thedappapp.dapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.app.Application;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DatabaseInitService extends IntentService {

    //Actions
    public static final String GENERATE_USER_DB_REFERENCE;
    public static final String GENERATE_CURRENT_GROUP_DB_REFERENCE;

    //Firebase
    private static final FirebaseAuth sAuth;
    private static final FirebaseDatabase sDatabase;

    static {
        GENERATE_USER_DB_REFERENCE = "com.thedappapp.dapp.services.action.GENERATE_USER_DB_REFERENCE";
        GENERATE_CURRENT_GROUP_DB_REFERENCE = "com.thedappapp.dapp.services.action.GENERATE_CURRENT_GROUP_DB_REFERENCE";
        sAuth = FirebaseAuth.getInstance();
        sDatabase = FirebaseDatabase.getInstance();
    }

    public DatabaseInitService() {
        super("DatabaseInitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        final String action = intent.getAction();

        if (GENERATE_USER_DB_REFERENCE.equals(action))
            generateUserReference();
        else if (GENERATE_CURRENT_GROUP_DB_REFERENCE.equals(action))
            generateGroupReference();
    }

    private void generateUserReference() {
        if (sAuth.getCurrentUser() == null) return; //do something

        DatabaseReference reference = sDatabase.getReference("user").child(sAuth.getCurrentUser().getUid());
        Application.getApplication().setDatabaseReference(Application.References.USER, reference);
    }

    private void generateGroupReference() {
        Application.getApplication()
                   .getDatabaseReference(Application.References.USER).child("group")
                   .addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           String gid = dataSnapshot.getValue(String.class);
                           DatabaseReference reference = sDatabase.getReference("groups").child(gid);
                           Application.getApplication().setDatabaseReference(Application.References.GROUP, reference);
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
    }

}
