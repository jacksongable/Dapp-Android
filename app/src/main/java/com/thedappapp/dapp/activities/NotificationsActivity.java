package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.NotificationsAdapter;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.interfaces.NoOptionsMenu;
import com.thedappapp.dapp.objects.Notification;

import java.util.ArrayList;

public class NotificationsActivity extends DappActivity implements NoDrawer, NoOptionsMenu {

    private static final String TAG = NotificationsActivity.class.getSimpleName();

    private RecyclerView mRecycler;
    private NotificationsAdapter mAdapter;
    private DatabaseReference notificationNode;
    private NotificationsListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new NotificationsAdapter(new ArrayList<Notification>(), this);
        notificationNode = FirebaseDatabase.getInstance().getReference("notifications").child(App.getApp().me().getUid());
        listener = new NotificationsListener();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(manager);

        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        notificationNode.addChildEventListener(listener);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        notificationNode.removeEventListener(listener);
    }

    private class NotificationsListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Notification notification = dataSnapshot.getValue(Notification.class);
            mAdapter.add(notification);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, Log.getStackTraceString(databaseError.toException()));
        }
    }

}
