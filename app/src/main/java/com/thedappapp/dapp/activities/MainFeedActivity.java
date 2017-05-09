package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.FeedAdapter;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.group.Group;
import java.util.ArrayList;
import java.util.List;

public class MainFeedActivity extends DappActivity {

    private static final String TAG = MainFeedActivity.class.getSimpleName();

    private RecyclerView mRecycler;
    private FeedAdapter adapter;
    private List<Group> list;
    private GroupsNodeListener groupsNodeListener;
    private PendingListener outgoingListener, incomingListener;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        startService(App.bellService());

        bar = (ProgressBar) findViewById(R.id.progressBar);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);

        groupsNodeListener = new GroupsNodeListener();
        outgoingListener = new PendingListener();
        incomingListener = new PendingListener();

        list = new ArrayList<>();
        adapter = new FeedAdapter(this, mRecycler, list);

        LinearLayoutManager manager = new LinearLayoutManager(MainFeedActivity.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Started.");
        FirebaseDatabase.getInstance().getReference("groups").addChildEventListener(groupsNodeListener);
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("pending_requests/outgoing").addChildEventListener(outgoingListener);
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("pending_requests/incoming").addChildEventListener(incomingListener);
    }

    public boolean isOutgoingPending (Group group) {
        return outgoingListener.isPending(group);
    }

    public boolean isIncomingPending (Group group) {
        return incomingListener.isPending(group);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopped.");
        if (! App.hasUser()) return; //TODO: Figure this out.
        FirebaseDatabase.getInstance().getReference("groups").removeEventListener(groupsNodeListener);
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("pending_requests/outgoing").removeEventListener(outgoingListener);
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("pending_requests/incoming").removeEventListener(incomingListener);
    }

    private class GroupsNodeListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Group g = dataSnapshot.getValue(Group.class);

            if (adapter.hasGroup(g)) return;

            if (bar.getVisibility() == View.VISIBLE && mRecycler.getVisibility() == View.GONE) {
                bar.setVisibility(View.GONE);
                mRecycler.setVisibility(View.VISIBLE);
            }
            Log.d(TAG.concat("$GroupsNodeListener"), "Child added.");
            if (!g.getLeaderId().equals(App.me().getUid()))
                adapter.add(dataSnapshot.getValue(Group.class));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            adapter.remove(dataSnapshot.getValue(Group.class));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(MainFeedActivity.class.getSimpleName(), Log.getStackTraceString(databaseError.toException()));
        }
    }

    private class PendingListener implements ChildEventListener {

        private List<String> pending;

        private PendingListener () {
            pending = new ArrayList<>();
        }

        private boolean isPending (Group group) {
            return pending.contains(group.getUid());
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            pending.add(dataSnapshot.getKey());
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
            App.handleDbErr(databaseError);
        }
    }
}
