package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.AbstractDappActivity;
import com.thedappapp.dapp.activities.CreateGroupP1Activity;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.app.StaticGroupReference;
import com.thedappapp.dapp.app.Configuration;
import com.thedappapp.dapp.async.AsyncImageDownloader;
import com.thedappapp.dapp.objects.group.Group;
import com.parse.ParseCloud;
import com.thedappapp.dapp.objects.invite.Invite;
import com.thedappapp.dapp.objects.invite.InviteFactory;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class RecyclerFeedAdapter extends RecyclerView.Adapter<RecyclerFeedAdapter.ViewHolder> {

    private Context mContext;
    private List<Group> mDataset;

    public RecyclerFeedAdapter (DappActivity context, List<Group> dataset) {
        mContext = context;
        mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_feed_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Group currentGroup = mDataset.get(position);

        AsyncImageDownloader downloader = new AsyncImageDownloader(mContext, currentGroup, holder.pic);
        downloader.execute();

        configureDappButton(holder, currentGroup);

        holder.name.setText(currentGroup.getName());
        holder.bio.setText(currentGroup.getBio());
        holder.viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewDetails = new Intent(mContext, GroupDetailsActivity.class);
                viewDetails.putExtra("group", currentGroup);
                mContext.startActivity(viewDetails);
            }
        });
    }

    private void configureDappButton (ViewHolder holder, Group current) {
        if (Configuration.hasDappedUp(current))
            configureDappButtonForPendingOutgoingRequest(holder);
        else if (Configuration.isFriendsWith(current))
            configureDappButtonForFriends(holder);
        else if (Configuration.isIncomingGroupPendingRequestAccept(current))
            configureDappButtonForPendingIncomingRequest(holder, current);
        else configureDappButtonForNoCurrentRelationship(holder, current);
    }

    private void configureDappButtonForPendingOutgoingRequest(ViewHolder holder) {
        holder.dappUp.setText("Dapped!");
        holder.dappUp.setEnabled(false);
    }

    private void configureDappButtonForFriends (ViewHolder holder) {
        holder.dappUp.setText("Friends");
        holder.dappUp.setEnabled(false);
    }

    private void configureDappButtonForPendingIncomingRequest (final ViewHolder holder, final Group current) {
        holder.dappUp.setText("Accept");
        holder.dappUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Configuration.acceptRequest(holder.dappUp, current);
            }
        });
    }

    private void configureDappButtonForNoCurrentRelationship (final ViewHolder holder, final Group current) {
        holder.dappUp.setText("Dapp Up!");
        holder.dappUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Configuration.getCurrentGroup() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Oops!").setMessage("You must create a group before you dapp others up.");

                    builder.setPositiveButton("Create...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(mContext, CreateGroupP1Activity.class);
                            intent.putExtra("edit", false);
                            mContext.startActivity(intent);
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
                    configureDappButtonForPendingOutgoingRequest(holder);

                    Configuration.onDappRequestSent(current);

                    InviteFactory factory = new InviteFactory();
                    factory.withFromGroup(Configuration.getCurrentGroup());
                    factory.withToGroup(current);
                    factory.withToUser(current.getLeaderAsUser());
                    factory.withStatus("Invited");

                    factory.build().saveInBackground();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) return 0;
        return mDataset.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView pic;
        public TextView name;
        public TextView bio;
        public Button viewDetails;
        public Button dappUp;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = (ImageView) itemView.findViewById(R.id.group_pic);
            name = (TextView) itemView.findViewById(R.id.group_name);
            bio = (TextView) itemView.findViewById(R.id.group_bio);
            viewDetails = (Button) itemView.findViewById(R.id.view_details);
            dappUp = (Button) itemView.findViewById(R.id.dapp);
        }
    }
}
