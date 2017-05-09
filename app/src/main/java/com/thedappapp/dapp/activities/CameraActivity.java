package com.thedappapp.dapp.activities;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.camera.CameraPreview;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.interfaces.NoOptionsMenu;
import com.thedappapp.dapp.interfaces.NoToolbar;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends DappActivity implements NoDrawer, NoOptionsMenu, NoToolbar {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private RelativeLayout bottomOverlay;
    private FrameLayout previewFrame;
    private Button snap;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.Parameters mParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        bottomOverlay = (RelativeLayout) findViewById(R.id.bottom_overlay);
        snap = (Button) findViewById(R.id.snap);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the preview size
        int previewWidth = previewFrame.getMeasuredWidth(),
                previewHeight = previewFrame.getMeasuredHeight();

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) bottomOverlay.getLayoutParams();
        overlayParams.height = previewHeight - previewWidth;
        bottomOverlay.setLayoutParams(overlayParams);
    }

    private void setCameraParameters () {
        Camera.Parameters parameters = mCamera.getParameters();

        //Set autofocus if available.
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(parameters);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            mCamera = getCameraInstance();
            setCameraParameters();
        }
        if (mPreview == null)
            mPreview = new CameraPreview(this, mCamera);

        previewFrame.addView(mPreview);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {

                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            Log.d(TAG, "Releasing camera.");
            mCamera.release();
            mCamera = null;
        }
    }

    private static Camera getCameraInstance () {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance.
        }
        catch (Exception e){
            App.exception(TAG, e);
        }
        return c; // returns null if camera is unavailable
    }
}
