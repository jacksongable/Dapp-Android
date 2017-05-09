package com.thedappapp.dapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.thedappapp.dapp.activities.MainFeedActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.Notification;
import com.thedappapp.dapp.objects.chat.ChatMetaShell;
import com.thedappapp.dapp.objects.group.Group;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class FeedAdapter extends RecyclerView.Adapter<com.thedappapp.dapp.adapters.FeedAdapter.ViewHolder> {

    private static final int UNSELECTED = -1;

    private static final String STATUS_NO_RELATIONSHIP = "status:no-relationship",
            STATUS_FRIENDS = "status:friends", STATUS_INCOMING_PENDING = "status:incoming-pending",
            STATUS_REQUEST_SENT = "status:request-sent";

    private int selectedItem = UNSELECTED;

    private static final String TAG = FeedAdapter.class.getSimpleName();

    private MainFeedActivity mContext;
    private RecyclerView recycler;
    private List<Group> mDataset;
    private List<String> idList;

    public FeedAdapter(MainFeedActivity context, RecyclerView recycler, List<Group> dataset) {
        mContext = context;
        this.recycler = recycler;
        mDataset = dataset;
        idList = new ArrayList<>();

        for (Group group : mDataset)
            idList.add(group.getUid());
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) return 0;
        else return mDataset.size();
    }

    public boolean hasGroup (Group g) {
        return idList.contains(g.getUid());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_feed_card, parent, false);
        return new ViewHolder(itemView, parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Group currentGroup = mDataset.get(position);
        String path = currentGroup.getPhoto();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.pic);
        holder.name.setText(currentGroup.getName());
        holder.bio.setText(currentGroup.getBio());
        holder.leader.setText("Led by ".concat(currentGroup.getLeaderName()));
        holder.bind(position);
    }

    public void add(Group group) {
        mDataset.add(group);
        idList.add(group.getUid());
        notifyDataSetChanged();
    }

    public void remove(Group group) {
        mDataset.remove(group);
        idList.remove(group.getUid());
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExpandableLayout expandableLayout;
        private int position;

        private ViewGroup parent;

        public ImageView pic;
        public TextView name;
        public TextView bio;
        public TextView leader;
        public View theView;


        public ViewHolder(View itemView, ViewGroup parent) {
            super(itemView);

            this.parent = parent;

            pic = (ImageView) itemView.findViewById(R.id.group_pic);
            name = (TextView) itemView.findViewById(R.id.group_name);
            leader = (TextView) itemView.findViewById(R.id.leader);
            bio = (TextView) itemView.findViewById(R.id.group_bio);

            theView = itemView;

            expandableLayout = (ExpandableLayout) itemView.findViewById(R.id.expandable_layout);
            expandableLayout.setInterpolator(new OvershootInterpolator());

            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            this.position = position;
            theView.setSelected(false);
            expandableLayout.collapse(false);
        }

        @Override
        public void onClick(View view) {

            ViewHolder holder = (ViewHolder) recycler.findViewHolderForAdapterPosition(selectedItem);
            final Group theGroup = mDataset.get(position);

            if (holder != null) {
                holder.theView.setSelected(false);
                holder.expandableLayout.collapse();
            }

            if (position == selectedItem) {
                selectedItem = UNSELECTED;
            } else {
                theView.setSelected(true);

                FrameLayout frame = (FrameLayout) expandableLayout.findViewById(R.id.frame);
                frame.removeAllViews();

                View expanded = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.content_feed_expanded, parent, false);

                LinearLayout interestHolder = (LinearLayout) expanded.findViewById(R.id.interest_holder);

                if (theGroup.hasInterest("food"))
                    showInterest("Food", R.drawable.profile_food, interestHolder);

                if (theGroup.hasInterest("entertainment"))
                    showInterest("Events", R.drawable.profile_events, interestHolder);

                if (theGroup.hasInterest("music"))
                    showInterest("Music", R.drawable.profile_music, interestHolder);

                if (theGroup.hasInterest("gaming"))
                    showInterest("Gaming", R.drawable.profile_gaming, interestHolder);

                if (theGroup.hasInterest("party"))
                    showInterest("Parties", R.drawable.profile_party, interestHolder);

                if (theGroup.hasInterest("sports"))
                    showInterest("Sports", R.drawable.profile_sports, interestHolder);

                //INSERT CHECKS TO VERIFY RELATION WITH GROUP HERE, THEN ACT ACCORDINGLY. ALSO MAKE SURE CURRENT USER HAS A GROUP
                //Dummy check. No current relationship
                FrameLayout innerFrame = (FrameLayout) expanded.findViewById(R.id.dapp_option_frame);
                innerFrame.removeAllViews();
                final View dappOptions = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_dapp_options, parent, false);

                final Button dapp = (Button) dappOptions.findViewById(R.id.dappUp);

                if (mContext.isOutgoingPending(theGroup)) {
                    dapp.setText("Request Sent!");
                    dapp.setEnabled(false);
                    dapp.setTag(STATUS_REQUEST_SENT);
                }
                if (mContext.isIncomingPending(theGroup)) {
                    dapp.setText("Accept Chat Request");
                    dapp.setTag(STATUS_INCOMING_PENDING);
                }
                //if (isFriends) dapp.setTag(STATUS_FRIENDS);
                else dapp.setTag(STATUS_NO_RELATIONSHIP);

                dapp.setOnClickListener(new DappButtonListener(theGroup));

                innerFrame.addView(dappOptions);

                frame.addView(expanded);

                expandableLayout.expand();
                selectedItem = position;
            }
        }

        private void showInterest(String text, int drawable, LinearLayout interestContainer) {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.VERTICAL);

            float density = mContext.getResources().getDisplayMetrics().density;
            int margin = (int) (10 * density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(margin, margin, margin, margin);

            ImageView image = new ImageView(mContext);
            image.setImageResource(drawable);

            TextView textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER);
            textView.setText(text);

            layout.setLayoutParams(params);
            layout.addView(image);
            layout.addView(textView);

            interestContainer.addView(layout);
        }


    }



    private class DappButtonListener implements View.OnClickListener {

        private Group theGroup;

        private DappButtonListener (Group group) {
            theGroup = group;
        }

        @Override
        public void onClick(View view) {
            if (view.getTag().equals(STATUS_NO_RELATIONSHIP)) App.sendRequest(theGroup, mContext);
            else if (view.getTag().equals(STATUS_INCOMING_PENDING)) App.acceptRequest(theGroup);
            else throw new IllegalStateException("Either the view has been assigned an illegal tag, or the view's tag is status:friends or status:request-sent");
        }

    }

}

