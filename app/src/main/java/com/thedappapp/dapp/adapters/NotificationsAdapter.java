package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.activities.NotificationsActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.Notification;
import com.thedappapp.dapp.objects.chat.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackson on 4/22/17.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<com.thedappapp.dapp.adapters.NotificationsAdapter.ViewHolder> {

    private List<Notification> mDataset;
    private NotificationsActivity context;
    private List<String> idList;

    public NotificationsAdapter (List<Notification> data, NotificationsActivity context) {
        this.mDataset = data;
        this.context = context;
        idList = new ArrayList<>();

        for (Notification n : mDataset)
            idList.add(n.getUid());
    }

    public void add (Notification notif) {
        mDataset.add(notif);
        idList.add(notif.getUid());
        notifyDataSetChanged();
    }

    public boolean hasNotification (Notification n) {
        return idList.contains(n.getUid());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.content_notification, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);

        Notification notification = mDataset.get(position);
        if (! notification.isRead()) holder.container.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
        holder.notifText.setText(notification.getMessage());

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(notification.getPhoto());
        Glide.with(context).using(new FirebaseImageLoader()).load(ref).into(holder.pic);

    }

    public List<Notification> getData() {
        return mDataset;
    }



    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout container;
        private TextView notifText;
        private ImageView pic;
        private int postition;

        private ViewHolder (View view) {
            super(view);
            container = (RelativeLayout) view.findViewById(R.id.container);
            notifText = (TextView) view.findViewById(R.id.message);
            pic = (ImageView) view.findViewById(R.id.group_pic);

            view.setOnClickListener(this);
        }

        private void bind (int position) {
            this.postition = position;
        }

        @Override
        public void onClick(View view) {
            Notification notification = mDataset.get(postition);
            String gid = (String) notification.getData("from_group");
            Intent details = new Intent(context, GroupDetailsActivity.class);
            details.putExtra("gid", gid);
            context.startActivity(details);
        }
    }


}
