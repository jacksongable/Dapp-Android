package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.FeedAdapter;
import com.thedappapp.dapp.objects.group.Group;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends DappActivity {

    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);

        FirebaseDatabase.getInstance().getReference().child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> list = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getValue(Group.class));
                }

                LinearLayoutManager manager = new LinearLayoutManager(FeedActivity.this);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecycler.setLayoutManager(manager);
                mRecycler.setAdapter(new FeedAdapter(FeedActivity.this, list));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(FeedActivity.class.getSimpleName(), Log.getStackTraceString(databaseError.toException()));
            }
        });

    }
}
