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

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.interfaces.NoToolbar;
import com.thedappapp.dapp.objects.chat.Chatroom;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.invite.Invite;

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
        vLeader.setText(theGroup.getLeader());
        vBio.setText(theGroup.getBio());
        AsyncImageDownloader downloader = new AsyncImageDownloader(this, theGroup, vGroupPic);
        downloader.execute();

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
        if (Configuration.hasDappedUp(theGroup))
            configureDappButtonForPendingOutgoingRequest();
        else if (Configuration.isFriendsWith(theGroup))
            configureDappButtonForFriends();
        else if (Configuration.isIncomingGroupPendingRequestAccept(theGroup))
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
                //Invite i = Configuration.parseInviteFromGroup(theGroup);
                Chatroom room = Configuration.acceptRequest(dapp, theGroup);
                configureDappButtonForFriends();
                StaticChatRoomReference.setReference(room);
                Intent i = new Intent(GroupDetailsActivity.this, ChatThreadActivity.class);
                startActivity(i);
            }
        });
    }

    private void configureDappButtonForNoCurrentRelationship () {
        dapp.setText("Dapp Up!");
        dapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Configuration.getCurrentGroup() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
                    builder.setTitle("Oops!").setMessage("You must create a group before you dapp others up.");

                    builder.setPositiveButton("Create...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(GroupDetailsActivity.this, CreateGroupP1Activity.class);
                            intent.putExtra("edit", false);
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
                    Configuration.sendDappRequest(theGroup);
                }
            }
        });
    }
}
