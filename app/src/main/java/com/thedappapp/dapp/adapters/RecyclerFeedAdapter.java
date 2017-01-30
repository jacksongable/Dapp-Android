package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.CreateGroupActivity;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.app.RequestStorage;
import com.thedappapp.dapp.objects.Request;
import com.thedappapp.dapp.objects.group.Group;

import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class RecyclerFeedAdapter extends RecyclerView.Adapter<RecyclerFeedAdapter.ViewHolder> {

    private static final String TAG = RecyclerFeedAdapter.class.getSimpleName();

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

        StorageReference ref = FirebaseStorage.getInstance().getReference(currentGroup.getPhotoPath());
        Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.pic);

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
        RequestStorage storage = App.getApp().getRequestStorage();
        if (storage.hasDappedUp(current))
            configureDappButtonForPendingOutgoingRequest(holder);
        else if (storage.isFriends(current))
            configureDappButtonForFriends(holder);
        else if (storage.isDappedUpBy(current))
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
                holder.dappUp.setText("Friends");
                holder.dappUp.setEnabled(false);
                App.getApp().getRequestStorage().get(current.getLeaderId()).accept();
            }
        });
    }

    private void configureDappButtonForNoCurrentRelationship (final ViewHolder holder, final Group current) {
        holder.dappUp.setText("Dapp Up!");
        holder.dappUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.getApp().getCurrentGroup() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Oops!").setMessage("You must create a group before you dapp others up.");

                    builder.setPositiveButton("Create...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(mContext, CreateGroupActivity.class);
                            intent.setAction(CreateGroupActivity.ACTION_CREATE);
                            mContext.startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "Cancelled");
                        }
                    });

                    builder.create().show();
                }
                else {
                    configureDappButtonForPendingOutgoingRequest(holder);
                    Request request = new Request(App.getApp().me().getUid(), current.getLeaderId());
                    request.save(DatabaseOperationCodes.CREATE);
                    App.getApp().getRequestStorage().putOutgoingRequest(request);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) return 0;
        else return mDataset.size();
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
