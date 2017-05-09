package com.thedappapp.dapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.objects.Notification;
import com.thedappapp.dapp.objects.chat.ChatMetaShell;
import com.thedappapp.dapp.objects.group.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends DappActivity implements NoDrawer {

    private static final String TAG = GroupDetailsActivity.class.getSimpleName();
    private static final String STATUS_NO_RELATIONSHIP = "status:no-relationship",
            STATUS_FRIENDS = "status:friends", STATUS_INCOMING_PENDING = "status:incoming-pending",
            STATUS_REQUEST_SENT = "status:request-sent";

    private GroupDataListener groupDataListener;
    private DatabaseReference groupReference;
    private TextView vGroupName, vLeader, vBio;
    private ImageView vGroupPic;
    private Button cancel, dapp;
    private RelationshipListener incoming, outgoing, friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        vGroupName = (TextView) findViewById(R.id.group_name);
        vLeader = (TextView) findViewById(R.id.group_leader);
        vBio = (TextView) findViewById(R.id.group_bio);
        vGroupPic = (ImageView) findViewById(R.id.group_pic);
        cancel = (Button) findViewById(R.id.group_details_cancel);
        dapp = (Button) findViewById(R.id.dapp_up_group);

        groupReference = FirebaseDatabase.getInstance().getReference("groups").child(getIntent().getExtras().getString("gid"));
        groupDataListener = new GroupDataListener();
        friends = new RelationshipListener();
        incoming = new RelationshipListener();
        outgoing = new RelationshipListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupReference.addValueEventListener(groupDataListener);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference("users")
                                      .child(App.me().getUid())
                                      .child("pending_requests/outgoing")
                                      .addChildEventListener(outgoing);

        FirebaseDatabase.getInstance().getReference("users")
                .child(App.me().getUid())
                .child("pending_requests/incoming")
                .addChildEventListener(incoming);

        FirebaseDatabase.getInstance().getReference("users")
                .child(App.me().getUid())
                .child("friends")
                .addChildEventListener(friends);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    private class DappButtonListener implements View.OnClickListener {

        private Group theGroup;

        private DappButtonListener(Group group) {
            theGroup = group;
        }

        @Override
        public void onClick(View view) {
            if (view.getTag().equals(STATUS_NO_RELATIONSHIP))
                App.sendRequest(theGroup, GroupDetailsActivity.this);
            else if (view.getTag().equals(STATUS_INCOMING_PENDING))
                App.acceptRequest(theGroup);
            else
                throw new IllegalStateException("Either the view has been assigned an illegal tag, or the view's tag is status:friends or status:request-sent");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        groupReference.removeEventListener(groupDataListener);
    }

    private class GroupDataListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Group group = dataSnapshot.getValue(Group.class);
            setTitle(group.getName());
            dapp.setOnClickListener(new DappButtonListener(group));
            GroupDetailsActivity context = GroupDetailsActivity.this;

            //vGroupName.setText(group.getName());
            vLeader.setText(group.getLeaderName());
            vBio.setText(group.getBio());

            StorageReference pic = FirebaseStorage.getInstance().getReferenceFromUrl(group.getPhoto());
            Glide.with(context).using(new FirebaseImageLoader()).load(pic).into(vGroupPic);

            LinearLayout interestHolder = (LinearLayout) findViewById(R.id.interest_holder);

            interestHolder.removeAllViews();

            if (group.hasInterest("food")) {
                interestHolder.addView(getSingleInterestView(context, "Food", R.drawable.profile_food));
            }
            if (group.hasInterest("entertainment")) {
                interestHolder.addView(getSingleInterestView(context, "Events", R.drawable.profile_events));
            }
            if (group.hasInterest("music")) {
                interestHolder.addView(getSingleInterestView(context, "Music", R.drawable.profile_music));
            }
            if (group.hasInterest("gaming")) {
                interestHolder.addView(getSingleInterestView(context, "Gaming", R.drawable.profile_gaming));
            }
            if (group.hasInterest("party")) {
                interestHolder.addView(getSingleInterestView(context, "Party", R.drawable.profile_party));
            }
            if (group.hasInterest("sports")) {
                interestHolder.addView(getSingleInterestView(context, "Sports", R.drawable.profile_sports));
            }

            if (group.isMine()) {
                dapp.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cancel.setLayoutParams(params);
            }

            else if (incoming.hasRelationship(group)) {
                dapp.setTag(STATUS_INCOMING_PENDING);
                dapp.setText("Accept Chat Request");
            }
            else if (outgoing.hasRelationship(group)) {
                dapp.setTag(STATUS_REQUEST_SENT);
                dapp.setText("Request Sent");
                dapp.setEnabled(false);
            }
            else if (friends.hasRelationship(group)) {
                dapp.setTag(STATUS_FRIENDS);
                dapp.setText("Friends");
                dapp.setEnabled(false);
            }
            else {
                dapp.setTag(STATUS_NO_RELATIONSHIP);
                dapp.setText("Request to Chat");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Cancelled.");
        }

        private View getSingleInterestView(Context context, String text, int drawable) {
            LinearLayout layout = new LinearLayout(context);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);

            float density = context.getResources().getDisplayMetrics().density;
            int margin = (int) (10 * density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(margin, margin, margin, margin);

            ImageView image = new ImageView(context);
            image.setImageResource(drawable);

            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(text);

            layout.setLayoutParams(params);
            layout.addView(image);
            layout.addView(textView);

            return layout;
        }
        }

    private class RelationshipListener implements ChildEventListener {

        private List<String> list;

        private RelationshipListener () {
            list = new ArrayList<>();
        }

        private List<String> getAllGroupKeys() {
            return list;
        }

        private boolean hasRelationship (Group group) {
            return list.contains(group.getUid());
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            list.add(dataSnapshot.getKey());
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

