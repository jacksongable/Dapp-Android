package com.thedappapp.dapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.InterestGridAdapter;
import com.thedappapp.dapp.objects.group.Group;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupPage2Fragment extends Fragment {

    private Button done;
    private GridView grid;
    private InterestGridAdapter adapter;

    public CreateGroupPage2Fragment() {
        // Required empty public constructor
    }

    public static CreateGroupPage2Fragment newInstance(Group toEdit) {
        CreateGroupPage2Fragment fragment = new CreateGroupPage2Fragment();
        Bundle arguments = new Bundle();
        if (toEdit != null) {
            arguments.putBoolean("editMode", true);
            arguments.putParcelable("edit", toEdit);
        }
        else arguments.putBoolean("editMode", false);
        fragment.setArguments(arguments);
        return fragment;
    }

    private Page2FragmentInteractionListener mListener;
    private Group theGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Page2FragmentInteractionListener) {
            mListener = (Page2FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            theGroup = getArguments().getParcelable("edit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group_page2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        done = (Button) view.findViewById(R.id.submit);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getInterests().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Hold up!")
                            .setMessage("You have to select at least one interest to continue.")
                            .setNeutralButton("Continue", null)
                            .show();
                }
                else {
                    done.setEnabled(false);
                    done.setText("Saving...");
                    mListener.onPage2Interaction();
                }
            }
        });

        grid = (GridView) view.findViewById(R.id.gridview);

        if (getArguments().getBoolean("editMode")) {
            Group edit = getArguments().getParcelable("edit");

            boolean[] interests = {edit.hasInterest("food"), edit.hasInterest("entertainment"),
                                   edit.hasInterest("music"), edit.hasInterest("gaming"),
                                   edit.hasInterest("sports"), edit.hasInterest("party")};

            adapter = new InterestGridAdapter(getActivity(), interests);
        }

        else adapter = new InterestGridAdapter(getActivity());


        grid.setAdapter(adapter);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Bundle pullInfo() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("interests", (ArrayList<String>) adapter.getInterests());
        return bundle;
    }

    public interface Page2FragmentInteractionListener {
        void onPage2Interaction();
    }
}
