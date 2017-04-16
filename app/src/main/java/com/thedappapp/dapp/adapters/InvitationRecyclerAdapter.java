package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.Request;
import com.thedappapp.dapp.objects.group.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackson on 11/25/16.
 */
public class InvitationRecyclerAdapter extends RecyclerView.Adapter<InvitationRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Request> mDataset;

    private ArrayList<Request> acceptedInvites;

    public InvitationRecyclerAdapter (Context context, List<Request> dataset) {
        mContext = context;
        mDataset = dataset;
        acceptedInvites = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_invite_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String fromUser = mDataset.get(position).getFrom_id();
        App.getApp().USER.child(fromUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupId = dataSnapshot.child("group").getValue(String.class);
                App.getApp().GROUPS.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        StorageReference pic = FirebaseStorage.getInstance().getReference(group.getPhoto());
                        Glide.with(mContext).using(new FirebaseImageLoader()).load(pic).into(holder.pic);

                        holder.name.setText(group.getName());
                        holder.bio.setText(group.getBio());

                        ActionListener listener = new ActionListener(group, holder.accept, mDataset.get(holder.getAdapterPosition()));

                        holder.viewDetails.setOnClickListener(listener);
                        holder.accept.setOnClickListener(listener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) return 0;
        return mDataset.size();
    }

    public List<Request> getAcceptedInvites () {
        return acceptedInvites;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView pic;
        public TextView name;
        public TextView bio;
        public Button viewDetails;
        public Button accept;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = (ImageView) itemView.findViewById(R.id.group_pic);
            name = (TextView) itemView.findViewById(R.id.group_name);
            bio = (TextView) itemView.findViewById(R.id.group_bio);
            viewDetails = (Button) itemView.findViewById(R.id.view);
            accept = (Button) itemView.findViewById(R.id.accept);
        }
    }

    private class ActionListener implements Button.OnClickListener {

        private Group mSelected;
        private Button dappButton;
        private Request request;

        private ActionListener (Group item, Button dappButton, Request request) {
            mSelected = item;
            this.dappButton = dappButton;
            this.request = request;
        }

        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.view:
                    view();
                    break;
                case R.id.accept:
                    dappButton.setText("Friends");
                    dappButton.setEnabled(false);
                    request.accept();
                    break;
                default: throw new IllegalArgumentException("Button does not exist.");
            }
        }

        private void view () {
            Intent viewDetails = new Intent(mContext, GroupDetailsActivity.class);
            viewDetails.putExtra("group", mSelected);
            mContext.startActivity(viewDetails);
        }
    }
}

