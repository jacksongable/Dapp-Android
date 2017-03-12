package com.thedappapp.dapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 9/25/16.
 */
public class CurrentGroupFragment extends Fragment {

    private static final String TAG = CurrentGroupFragment.class.getSimpleName();

    public static CurrentGroupFragment newInstance () {
        return new CurrentGroupFragment();
    }

    public interface Callback {
        void onEditRequest(Group group);
        void onDeleteRequest(Group group);
    }

    //UI members
    private ImageView vGroupPic;
    private TextView vName, vBio;
    private Button delete, edit;
    private LinearLayout interestContainer;

    //Data members
    private Callback callback;
    private DatabaseReference groupReference;
    private Listener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callback))
            throw new ClassCastException(context.getClass().getSimpleName() + " must implement Callback.");
        this.callback = (Callback) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listener = new Listener();
        String gid = getActivity().getSharedPreferences(App.USER_DATA, Context.MODE_PRIVATE).getString("gid", "empty");
        if (gid.equals("empty"))
            throw new RuntimeException("For some reason, this fragment was loaded even though there is no current group.");
        groupReference = FirebaseDatabase.getInstance().getReference("groups").child(gid);

        return inflater.inflate(R.layout.fragment_current_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        vGroupPic = (ImageView) view.findViewById(R.id.group_pic);
        vName = (TextView) view.findViewById(R.id.group_name);
        vBio = (TextView) view.findViewById(R.id.group_bio);
        interestContainer = (LinearLayout) view.findViewById(R.id.interest_holder);
        delete = (Button) view.findViewById(R.id.delete_button);
        edit = (Button) view.findViewById(R.id.edit_button);

    }

    @Override
    public void onStart () {
        super.onStart();
        groupReference.addValueEventListener(listener);
    }

    private void showInterest(String text, int drawable) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);

        float density = getActivity().getResources().getDisplayMetrics().density;
        int margin = (int) (10 * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin, margin, margin);

        ImageView image = new ImageView(getActivity());
        image.setImageResource(drawable);

        TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);

        layout.setLayoutParams(params);
        layout.addView(image);
        layout.addView(textView);

        interestContainer.addView(layout);
    }

    @Override
    public void onStop() {
        super.onStop();
        groupReference.removeEventListener(listener);
    }

    private class Listener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final Group theGroup = dataSnapshot.getValue(Group.class);
            if (theGroup == null) return;

            vName.setText(theGroup.getName());
            vBio.setText(theGroup.getBio());

            if (theGroup.hasInterest("food"))
                showInterest("Food", R.drawable.profile_food);

            if (theGroup.hasInterest("entertainment"))
                showInterest("Events", R.drawable.profile_events);

            if (theGroup.hasInterest("music"))
                showInterest("Music", R.drawable.profile_music);

            if (theGroup.hasInterest("gaming"))
                showInterest("Gaming", R.drawable.profile_gaming);

            if (theGroup.hasInterest("party"))
                showInterest("Parties", R.drawable.profile_party);

            if (theGroup.hasInterest("sports"))
                showInterest("Sports", R.drawable.profile_sports);

            StorageReference pic = FirebaseStorage.getInstance().getReferenceFromUrl(theGroup.getPhotoPath());
            Glide.with(CurrentGroupFragment.this).using(new FirebaseImageLoader()).load(pic).into(vGroupPic);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Are you sure?");
                    builder.setMessage("Are you sure you'd like to delete your group?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.onDeleteRequest(theGroup);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d(TAG, "Negative option clicked.");
                        }
                    });
                    builder.create().show();
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onEditRequest(theGroup);
                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Cancelled.");
        }
    }
}
