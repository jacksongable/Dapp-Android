package com.thedappapp.dapp.app.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.thedappapp.dapp.app.App;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jackson on 8/4/16.
 */
public class DappCamera {

    private static final String TAG = DappCamera.class.getSimpleName();;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private Activity context;
    private File mImageFile;

    public DappCamera(Activity context) {
        this.context = context;
    }

    public void dispatch() {
       /* android.hardware.Camera camera = null;  // object that use
        Camera.CameraInfo info = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx<count; camIdx++) {
            Camera.getCameraInfo(camIdx, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    App.exception(TAG, e);
                }
            }
        } */

        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (imageCaptureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            mImageFile = null;
            try {
                mImageFile = createJPEG();
            }
            catch (IOException ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }
            // Continue only if the File was successfully created
            if (mImageFile != null) {
                imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
                imageCaptureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                context.startActivityForResult(imageCaptureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    private File createJPEG () throws IOException {
        if (App.getApp().hasFilePermissions()) {
            File directory = getApplicationPhotoDirectory();
            directory.mkdirs();
            File jpeg = new File(directory, uniqueName());
            jpeg.createNewFile();
            return jpeg;
        }
        else Toast.makeText(context, "In ord", Toast.LENGTH_LONG).show();
        /*else {
            App.getApp().requestFilePermissions(context);
            if (App.getApp().hasFilePermissions())
                createJPEG();
            else
                Toast.makeText(context, "You must allow us to read and write to external storage so we can take your picture!",
                        Toast.LENGTH_LONG).show();
        } */
        return null;
    }

    private String uniqueName () {
        String timestamp = new SimpleDateFormat ("yyyyMMdd_HHmmss").format(new Date());
        return "Dapp_" + timestamp + ".jpeg";
    }

    public boolean hasImage () {
        return mImageFile != null;
    }

    public File getCapturedImageFile () {
        return mImageFile;
    }

    public String getCapturedImagePath () {
        return mImageFile.getAbsolutePath();
    }

    public static File getApplicationPhotoDirectory() {
        File externalDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File (externalDirectory, "Dapp");
    }
}

