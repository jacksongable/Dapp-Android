package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.CameraSource;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.camera.CameraPreview;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.interfaces.NoOptionsMenu;
import com.thedappapp.dapp.interfaces.NoToolbar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends DappActivity implements NoDrawer, NoOptionsMenu, NoToolbar {

    public static final String IMAGE_FILE_PATH = "image_file_path";

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int IMAGE_SIZE = 1024, IMAGE_ORIENTATION = 90;

    private RelativeLayout bottomOverlay, topOverlay;
    private FrameLayout previewFrame;
    private Button snap, retake, ok;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.Parameters mParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        bottomOverlay = (RelativeLayout) findViewById(R.id.bottom_overlay);
        topOverlay = (RelativeLayout) findViewById(R.id.top_overlay);
        snap = (Button) findViewById(R.id.snap);
        retake = (Button) findViewById(R.id.retake);
        ok = (Button) findViewById(R.id.use_this_picture);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the preview size
        int previewWidth = previewFrame.getMeasuredWidth(),
            previewHeight = previewFrame.getMeasuredHeight();

        int overlayHeight = (previewHeight - previewWidth) / 2;

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) bottomOverlay.getLayoutParams();
        overlayParams.height = overlayHeight;
        bottomOverlay.setLayoutParams(overlayParams);

        overlayParams = (RelativeLayout.LayoutParams) topOverlay.getLayoutParams();
        overlayParams.height = overlayHeight;
        topOverlay.setLayoutParams(overlayParams);
    }

    private void setCameraParameters () {
        Camera.Parameters parameters = mCamera.getParameters();

        //Set autofocus if available.
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        // Find a preview size that is at least the size of our IMAGE_SIZE
        Camera.Size previewSize = parameters.getSupportedPreviewSizes().get(0);
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width >= IMAGE_SIZE && size.height >= IMAGE_SIZE) {
                previewSize = size;
                break;
            }
        }
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        // Try to find the closest picture size to match the preview size.
        Camera.Size pictureSize = parameters.getSupportedPictureSizes().get(0);
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (size.width == previewSize.width && size.height == previewSize.height) {
                pictureSize = size;
                break;
            }
        }
        parameters.setPictureSize(pictureSize.width, pictureSize.height);


        mCamera.setDisplayOrientation(IMAGE_ORIENTATION);
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

        mCamera.startPreview();
        previewFrame.addView(mPreview);
        snap.setEnabled(true);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snap.setEnabled(false);
                mCamera.takePicture(null, null, new JpegCallback());
            }
        });
        retake.setVisibility(View.INVISIBLE);
        ok.setVisibility(View.INVISIBLE);

    }



    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            Log.d(TAG, "Releasing camera.");
            mCamera.stopPreview();
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

    private class JpegCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File output = null;
            Bitmap picture = null;
            int resultCode = RESULT_OK;

            try {
                output = getOutputMediaFile();
                picture = processImage(bytes);
                FileOutputStream out = new FileOutputStream(output);
                picture.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();

            } catch (IOException e) {
                App.exception(TAG, e);
                resultCode = RESULT_CANCELED;
            } finally {
                final int FINAL_RESULT = resultCode;
                final File FINAL_OUTPUT = output;

                ok.setVisibility(View.VISIBLE);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent resultIntent  = getIntent();
                        resultIntent.setData(Uri.fromFile(FINAL_OUTPUT));
                        setResult(FINAL_RESULT, resultIntent);
                        finish();
                    }
                });

                retake.setVisibility(View.VISIBLE);
                retake.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        }

        /** Create a File for saving an image or video */
        private File getOutputMediaFile() {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Dapp");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".png");

            return mediaFile;
        }

        private Bitmap processImage(byte[] data) throws IOException {
            // Determine the width/height of the image
            int width = mCamera.getParameters().getPictureSize().width;
            int height = mCamera.getParameters().getPictureSize().height;

            // Load the bitmap from the byte array
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            // Rotate and crop the image into a square
            int croppedWidth = (width > height) ? height : width;
            int croppedHeight = (width > height) ? height : width;

            Matrix matrix = new Matrix();
            matrix.postRotate(IMAGE_ORIENTATION * 3);
            Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, croppedWidth, croppedHeight, matrix, true);
            bitmap.recycle();

            // Scale down to the output size
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropped, IMAGE_SIZE, IMAGE_SIZE, true);
            cropped.recycle();

            return scaledBitmap;
        }
    }
}
