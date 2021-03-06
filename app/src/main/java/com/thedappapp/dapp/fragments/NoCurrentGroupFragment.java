package com.thedappapp.dapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;

public class NoCurrentGroupFragment extends Fragment {

    public static NoCurrentGroupFragment newInstance () {
        return new NoCurrentGroupFragment();
    }

    public interface Callback {
        void onCreateGroupRequestReceived();
    }

    private Callback mListener;
    private Button vCreateGroupButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (! (context instanceof Callback))
            throw new ClassCastException(context.getClass().getSimpleName() + " must implement Callback.");

        mListener = (Callback) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_current_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vCreateGroupButton = (Button) view.findViewById(R.id.create_group);

        /*
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("Jane Q. User").build();

        App.getApp().me().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(NoCurrentGroupFragment.class.getSimpleName(), "User profile updated.");
                        }
                    }
                });
                */

        vCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCreateGroupRequestReceived();
            }
        });
    }



}
