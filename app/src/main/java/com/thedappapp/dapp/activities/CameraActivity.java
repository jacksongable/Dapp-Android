package com.thedappapp.dapp.activities;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.camera.CameraOld;
import com.thedappapp.dapp.app.camera.PreviewSurfaceOld;
import com.thedappapp.dapp.app.camera.SupportCamera;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.interfaces.NoOptionsMenu;
import com.thedappapp.dapp.interfaces.NoToolbar;

import java.io.IOException;

public class CameraActivity extends DappActivity implements NoDrawer, NoOptionsMenu, NoToolbar {

    //Change to SupportCamera eventually
    private CameraOld mCamera;

    private FrameLayout cameraPreview;
    private RelativeLayout overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Optional: Hide the status bar at the top of the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the content view and get references to our views
        setContentView(R.layout.layout_camera_preview);
        mCamera = (CameraOld) SupportCamera.getSupportCamera(this);
        cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        overlay = (RelativeLayout) findViewById(R.id.bottom_overlay);
        PreviewSurfaceOld previewSurface = new PreviewSurfaceOld(this, mCamera.getmCamera());

        cameraPreview.addView(previewSurface);

        ((Button) findViewById(R.id.snap)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.snap();
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the preview size
        int previewWidth = cameraPreview.getMeasuredWidth(),
            previewHeight = cameraPreview.getMeasuredHeight();

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
        overlayParams.height = previewHeight - previewWidth;
        overlay.setLayoutParams(overlayParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.onActivityPaused();
    }

    @Override
    protected void onResume () {
        super.onResume();
        mCamera.onActivityResumed();
    }


}
