package com.thedappapp.dapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.Camera;
import com.thedappapp.dapp.objects.group.Group;

public class CreateGroupPage1Fragment extends Fragment {


    public CreateGroupPage1Fragment() {
        // Required empty public constructor
    }

    public static CreateGroupPage1Fragment newInstance(Group toEdit) {
        CreateGroupPage1Fragment fragment = new CreateGroupPage1Fragment();
        if (toEdit != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable("edit", toEdit);
            fragment.setArguments(arguments);
        }
        return fragment;
    }

    boolean downloadFlag = true;
    private Button vNext;
    private ImageButton vCaptureImage;
    private EditText vGroupName, vGroupBio;
    private Camera mCamera;
    private TextView error;
    private boolean hasTakenPicture = false;
    private Group editGroup;
    private Page1FragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Page1FragmentInteractionListener)
            mListener = (Page1FragmentInteractionListener) context;
        else throw new RuntimeException(context.toString() + " must implement Page1FragmentInteractionListener.");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            editGroup = getArguments().getParcelable("edit");
        mCamera = new Camera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group_page1, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vNext = (Button) view.findViewById(R.id.create_group_next);
        vCaptureImage = (ImageButton) view.findViewById(R.id.camera_button);
        vGroupName = (EditText) view.findViewById(R.id.group_name);
        vGroupBio = (EditText) view.findViewById(R.id.bio);
        error = (TextView) view.findViewById(R.id.error);

        vCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPage1Interaction(Page1FragmentInteractionListener.RequestCode.DISPATCH_CAMERA);
            }
        });

        vNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vGroupName.getText().toString().isEmpty()) {
                    error.setText("Name yourselves first!");
                    error.setVisibility(View.VISIBLE);
                    return;
                } else if (vGroupBio.getText().toString().isEmpty()) {
                    error.setText("Write a bio first!");
                    error.setVisibility(View.VISIBLE);
                    return;
                } else if (!hasTakenPicture) {
                    error.setText("Take a selfie first!");
                    error.setVisibility(View.VISIBLE);
                    return;
                } else error.setVisibility(View.INVISIBLE);

                mListener.onPage1Interaction(Page1FragmentInteractionListener.RequestCode.NEXT);
            }
        });

    }

    public Bundle pullInformation() {
        Bundle bundle = new Bundle();
        bundle.putString("name", vGroupName.getText().toString());
        bundle.putString("bio", vGroupBio.getText().toString());
        return bundle;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Page1FragmentInteractionListener {
        enum RequestCode {
            DISPATCH_CAMERA,
            DONE
        }
        void onPage1Interaction(RequestCode code);
    }
}
