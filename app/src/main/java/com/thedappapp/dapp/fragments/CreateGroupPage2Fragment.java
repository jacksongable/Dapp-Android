package com.thedappapp.dapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.group.Group;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupPage2Fragment extends Fragment {

    public CreateGroupPage2Fragment() {
        // Required empty public constructor
    }

    public static CreateGroupPage2Fragment newInstance(Group toEdit) {
        CreateGroupPage2Fragment fragment = new CreateGroupPage2Fragment();
        if (toEdit != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable("edit", toEdit);
            fragment.setArguments(arguments);
        }
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
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Bundle pullInformation () {
        Bundle bundle = new Bundle();
        ArrayList<String> interests = new ArrayList<>();

        bundle.putStringArrayList("interests", interests);
        return bundle;
    }

    public interface Page2FragmentInteractionListener {
        enum RequestCode {
            DONE
        }
        void onPage2Interaction(RequestCode code);
    }
}
