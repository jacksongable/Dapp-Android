package com.thedappapp.dapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.CameraActivity;
import com.thedappapp.dapp.activities.CameraNewActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.group.Group;

import java.io.File;

public class CreateGroupPage1Fragment extends Fragment {

    private static final String TAG = CreateGroupPage1Fragment.class.getSimpleName();
    private static final int CAMERA_FILE_READ_WRITE_REQUEST_CODE = 0;
    private static final int RC_CAMERA_ACTIVITY = 12;

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

    private Button vNext;
    private ImageButton vCaptureImage;
    private EditText vGroupName, vGroupBio;
    private TextView error;
    private boolean hasTakenPicture;
    private Group editGroup;
    private Page1FragmentInteractionListener mListener;
    private File photo;

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
        hasTakenPicture = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group_page1, container, false);
    }

    private void dispatchCameraActivity () {
        if (App.hasCameraPermission() && App.hasFilePermissions()) {
            Intent camera = new Intent(getActivity(), CameraNewActivity.class);
            startActivityForResult(camera, RC_CAMERA_ACTIVITY);
        }
        else ActivityCompat.requestPermissions(getActivity(), new String [] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, CAMERA_FILE_READ_WRITE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length <= 0) {
            Log.w(TAG, "Permission result array has 0 indicies.");
        }
        else if (requestCode == CAMERA_FILE_READ_WRITE_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults)
                if (result != PackageManager.PERMISSION_GRANTED)
                    granted = false;
            if (granted)
                dispatchCameraActivity();
            else Log.w(TAG, "PERMISSSION DENIED."); //TODO: Make this a better, safer check.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            hasTakenPicture = true;
            photo = new File (data.getStringExtra("file"));
            Glide.with(this).load(photo).into(vCaptureImage);
        }
        else {
            Toast.makeText(getActivity(), "We couldn't take our selfie!", Toast.LENGTH_LONG).show();
            Log.w(TAG, "CameraActivity result is not RESULT_OK.");
        }
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
                dispatchCameraActivity();
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

                mListener.onPage1Interaction();
            }
        });

    }

    public Bundle pullInfo() {
        Bundle bundle = new Bundle();
        bundle.putString("name", vGroupName.getText().toString());
        bundle.putString("bio", vGroupBio.getText().toString());
        bundle.putString("photo", photo.getAbsolutePath());
        return bundle;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Page1FragmentInteractionListener {
        void onPage1Interaction();
    }
}
