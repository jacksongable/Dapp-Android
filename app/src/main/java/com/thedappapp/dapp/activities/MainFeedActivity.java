package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.FeedAdapter;
import com.thedappapp.dapp.objects.group.Group;
import java.util.ArrayList;
import java.util.List;

import net.cachapa.expandablelayout.ExpandableLayout;

public class MainFeedActivity extends DappActivity {

    private static final String TAG = MainFeedActivity.class.getSimpleName();

    private RecyclerView mRecycler;
    private FeedAdapter adapter;
    private List<Group> list;
    private Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        listener = new Listener();
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
        FirebaseDatabase.getInstance().getReference("groups").addChildEventListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopped.");
        FirebaseDatabase.getInstance().getReference("groups").removeEventListener(listener);
    }

    private class Listener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "$Listener", "Child added.");
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
}
