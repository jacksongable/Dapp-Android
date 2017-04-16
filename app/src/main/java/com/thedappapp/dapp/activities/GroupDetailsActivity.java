package com.thedappapp.dapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.thedappapp.dapp.interfaces.NoToolbar;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.Request;

public class GroupDetailsActivity extends DappActivity implements NoToolbar { /*

    private static final String TAG = GroupDetailsActivity.class.getSimpleName();

    private Listener listener;
    private DatabaseReference reference;
    private TextView vGroupName, vLeader, vBio, vInterests;
    private ImageView vGroupPic;
    private Button cancel, dapp;

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

        reference = FirebaseDatabase.getInstance().getReference("groups").child(getIntent().getExtras().getString("gid"));
        listener = new Listener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        reference.addValueEventListener(listener);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void configureDappButton (Group theGroup) {
        RequestStorage storage = App.getApp().getRequestStorage();

        if (storage.hasDappedUp(theGroup))
            configureDappButtonForPendingOutgoingRequest();

        else if (storage.isFriends(theGroup))
            configureDappButtonForFriends();

        else if (storage.isDappedUpBy(theGroup))
            configureDappButtonForPendingIncomingRequest(theGroup);

        else configureDappButtonForNoCurrentRelationship(theGroup);
    }

    private void configureDappButtonForPendingOutgoingRequest() {
        dapp.setText("Dapped!");
        dapp.setEnabled(false);
    }

    private void configureDappButtonForFriends () {
        dapp.setText("Friends");
        dapp.setEnabled(false);
    }

    private void configureDappButtonForPendingIncomingRequest (final Group theGroup) {
        dapp.setText("Accept");
        dapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final App myApp = App.getApp();
                myApp.USER_REQUEST.child(theGroup.getLeaderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String requestId = dataSnapshot.child("id").getValue(String.class);
                        String from = dataSnapshot.child("from").getValue(String.class);

                        myApp.getRequestStorage().get(from).accept();
                        Conversation conversation = new Conversation(from);
                        conversation.save(SaveKeys.CREATE);

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

    private void configureDappButtonForNoCurrentRelationship (final Group theGroup) {
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

    @Override
    protected void onStop() {
        super.onStop();
        reference.removeEventListener(listener);
    }

    private class Listener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Group group = dataSnapshot.getValue(Group.class);
            GroupDetailsActivity context = GroupDetailsActivity.this;

            vGroupName.setText(group.getName());
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

            configureDappButton(group);
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
    }*/
}
