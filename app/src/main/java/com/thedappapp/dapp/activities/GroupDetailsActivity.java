package com.thedappapp.dapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.app.RequestStorage;
import com.thedappapp.dapp.interfaces.NoToolbar;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.Request;

public class GroupDetailsActivity extends DappActivity implements NoToolbar {

    private TextView vGroupName, vLeader, vBio, vInterests;
    private ImageView vGroupPic;
    private Button cancel, dapp;
    private Group theGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        theGroup = getIntent().getExtras().getParcelable("group");
    }

    @Override
    protected void onStart() {
        vGroupName = (TextView) findViewById(R.id.group_name);
        vLeader = (TextView) findViewById(R.id.group_leader);
        vBio = (TextView) findViewById(R.id.group_bio);
        vGroupPic = (ImageView) findViewById(R.id.group_pic);

        cancel = (Button) findViewById(R.id.group_details_cancel);
        dapp = (Button) findViewById(R.id.dapp_up_group);

        if (theGroup.isMine()) {
            dapp.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cancel.setLayoutParams(params);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        configureDappButton();

        vGroupName.setText(theGroup.getName());
        vLeader.setText(theGroup.getLeaderId());
        vBio.setText(theGroup.getBio());

        StorageReference pic = FirebaseStorage.getInstance().getReference(theGroup.getPhotoPath());
        Glide.with(this).using(new FirebaseImageLoader()).load(pic).into(vGroupPic);

        LinearLayout interestHolder = (LinearLayout) findViewById(R.id.interest_holder);

        if (theGroup.hasInterest("food")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_food);
            interestHolder.addView(view);
        }
        if (theGroup.hasInterest("entertainment")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_events);
            interestHolder.addView(view);
        }
        if (theGroup.hasInterest("music")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_music);
            interestHolder.addView(view);
        }
        if (theGroup.hasInterest("gaming")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_gaming);
            interestHolder.addView(view);
        }
        if (theGroup.hasInterest("party")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_party);
            interestHolder.addView(view);
        }
        if (theGroup.hasInterest("sports")) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.profile_sports);
            interestHolder.addView(view);
        }

    }

    private void configureDappButton () {
        RequestStorage storage = App.getApp().getRequestStorage();

        if (storage.hasDappedUp(theGroup))
            configureDappButtonForPendingOutgoingRequest();

        else if (storage.isFriends(theGroup))
            configureDappButtonForFriends();

        else if (storage.isDappedUpBy(theGroup))
            configureDappButtonForPendingIncomingRequest();

        else configureDappButtonForNoCurrentRelationship();
    }

    private void configureDappButtonForPendingOutgoingRequest() {
        dapp.setText("Dapped!");
        dapp.setEnabled(false);
    }

    private void configureDappButtonForFriends () {
        dapp.setText("Friends");
        dapp.setEnabled(false);
    }

    private void configureDappButtonForPendingIncomingRequest () {
        dapp.setText("Accept");
        dapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final App myApp = App.getApp();
                myApp.USER_REQUEST_ROOT.child(theGroup.getLeaderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String requestId = dataSnapshot.child("id").getValue(String.class);
                        String from = dataSnapshot.child("from").getValue(String.class);

                        myApp.getRequestStorage().get(from).accept();

                        Conversation conversation = new Conversation(from);
                        conversation.save(DatabaseOperationCodes.CREATE);

                        configureDappButtonForFriends();
                        Intent intent = new Intent(GroupDetailsActivity.this, ChatThreadActivity.class);
                        intent.putExtra("conversation", conversation);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void configureDappButtonForNoCurrentRelationship () {
        dapp.setText("Dapp Up!");
        dapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!App.getApp().hasCurrentGroup()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
                    builder.setTitle("Oops!").setMessage("You must create a group before you dapp others up.");

                    builder.setPositiveButton("Create...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(GroupDetailsActivity.this, CreateGroupActivity.class);
                            intent.setAction(CreateGroupActivity.ACTION_CREATE);
                            GroupDetailsActivity.this.startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Cancelled
                        }
                    });

                    builder.create().show();
                }
                else {
                    configureDappButtonForPendingOutgoingRequest();
                    Request request = new Request(App.getApp().me().getUid(), theGroup.getLeaderId());
                    App.getApp().sendRequest(request);
                }
            }
        });
    }
}
