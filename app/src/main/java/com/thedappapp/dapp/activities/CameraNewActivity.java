package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.interfaces.NoOptionsMenu;
import com.thedappapp.dapp.interfaces.NoToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraNewActivity extends DappActivity implements NoToolbar, NoDrawer, NoOptionsMenu {

    private static final String TAG = CameraNewActivity.class.getSimpleName();

    private CameraView cameraView;
    private FrameLayout previewContainer;
    private ImageButton snap;
    private Button retake, ok;
    private ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_new);

        snap = (ImageButton) findViewById(R.id.snap);
        cameraView = (CameraView) findViewById(R.id.camera);
        preview = (ImageView) findViewById(R.id.image_preview);
        previewContainer = (FrameLayout) findViewById(R.id.preview_container);
        retake = (Button) findViewById(R.id.retake);
        ok = (Button) findViewById(R.id.ok);

        Point size = new Point(0, 0);
        getWindowManager().getDefaultDisplay().getSize(size);
        int square = size.x;
        previewContainer.setLayoutParams(new RelativeLayout.LayoutParams(square, square));

        ImageListener listener = new ImageListener();

        cameraView.setCameraListener(listener);
        ok.setOnClickListener(listener);
        retake.setOnClickListener(listener);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.captureImage();
            }
        });

    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    private class ImageListener extends CameraListener implements View.OnClickListener {
        private Bitmap picture;

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.retake:
                    onRetake();
                    break;
                case R.id.ok:
                    onOk();
                    break;
            }
        }

        private void onRetake () {
            cameraView.setVisibility(View.VISIBLE);
            preview.setImageBitmap(null);
            preview.setVisibility(View.GONE);
            snap.setVisibility(View.VISIBLE);
            ok.setVisibility(View.INVISIBLE);
            retake.setVisibility(View.INVISIBLE);
        }

        private void onOk () {
            File outputFile = null;
            int resultCode = RESULT_OK;

            try {
                outputFile = getOutputMediaFile();
                FileOutputStream out = new FileOutputStream(outputFile);
                picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                App.exception(TAG, e);
                resultCode = RESULT_CANCELED;
            } finally {
                Intent activityResult = getIntent();
                activityResult.putExtra("file", outputFile.getAbsolutePath());
                setResult(resultCode, activityResult);
                finish();
            }
        }

        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);

            // Create a bitmap
            picture = getProcessedBitmap(jpeg);


            cameraView.setVisibility(View.GONE);
            preview.setImageBitmap(picture);
            preview.setVisibility(View.VISIBLE);
            snap.setVisibility(View.INVISIBLE);
            retake.setVisibility(View.VISIBLE);
            ok.setVisibility(View.VISIBLE);


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
                    "IMG_" + timeStamp + ".jpg");

            return mediaFile;
        }

        private Bitmap getProcessedBitmap (byte[] data) {
            Matrix matrix = new Matrix();
            matrix.setScale(-1F, 1F);
            Bitmap jpeg = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap mirror = Bitmap.createBitmap(jpeg, 0, 0, jpeg.getWidth(), jpeg.getHeight(), matrix, true);
            return mirror;
        }
    }
}
