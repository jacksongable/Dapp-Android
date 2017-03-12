package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.fragments.CurrentGroupFragment;
import com.thedappapp.dapp.fragments.NoCurrentGroupFragment;
import com.thedappapp.dapp.objects.group.Group;

/**
 * The main activity of the application. This is the user's "home page" if you will.
 *
 * @author Jackson Gable
 * @version Alpha
 */
public class MainActivity extends DappActivity
        implements NoCurrentGroupFragment.Callback, CurrentGroupFragment.Callback {

    private FrameLayout mFrame;
    private DatabaseReference db;
    private Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrame = (FrameLayout) findViewById(R.id.fragment);
        listener = new Listener();

        db = FirebaseDatabase.getInstance().getReference("user")
                                          .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                          .child("group");
    }

    @Override
    protected void onStart() {
        super.onStart();
        db.addValueEventListener(listener);
    }

    private void resetFrameLayout() {
        mFrame.removeAllViewsInLayout();
    }

    @Override
    public void onCreateGroupRequestReceived() {
        Intent intent = new Intent (this, CreateGroupActivity.class);
        intent.setAction(CreateGroupActivity.ACTION_CREATE);
        startActivity(intent);
        resetFrameLayout();
    }

    public void onHasCurrentGroup() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, CurrentGroupFragment.newInstance()).commitAllowingStateLoss();
    }

    public void onNoCurrentGroup() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, NoCurrentGroupFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    public void onEditRequest(Group group) {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        intent.setAction(CreateGroupActivity.ACTION_EDIT);
        intent.putExtra("edit", group);
        startActivity(intent);
    }

    @Override
    public void onDeleteRequest(Group group) {
        group.save(SaveKeys.DELETE);
        App.getApp().setCurrentGroup(null);
        onNoCurrentGroup();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.removeEventListener(listener);
    }

    private class Listener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String gid = dataSnapshot.getValue(String.class);

            if (gid != null) {
                SharedPreferences preferences = getSharedPreferences(App.USER_DATA, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("gid", gid);
                editor.apply();

                onHasCurrentGroup();
            }
            else onNoCurrentGroup();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(MainActivity.class.getSimpleName(), "Cancelled.");
        }
    }
}
