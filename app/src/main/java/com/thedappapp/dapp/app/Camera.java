package com.thedappapp.dapp.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.objects.PhotoReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jackson on 8/4/16.
 */
public class Camera {

    private static final String TAG = Camera.class.getSimpleName();;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private Activity context;
    private File mImageFile;

    public Camera(Activity context) {
        this.context = context;
    }

    public void dispatch() {
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
                context.startActivityForResult(imageCaptureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    private File createJPEG () throws IOException {
        if (App.getApp().hasFilePermissions()) {
            File directory = getApplicationPhotoDirectory();
            directory.mkdirs();
            File jpeg = new File(directory, uniqueName());
            Log.d(TAG, jpeg.getAbsolutePath());
            Log.d(TAG, directory.getAbsolutePath());
            jpeg.createNewFile();
            return jpeg;
        }
        else App.getApp().requestFilePermissions(context);
        if (App.getApp().hasFilePermissions())
            createJPEG();
        else
            Toast.makeText(context, "You must allow us to read and write to external storage so we can take your picture!",
                    Toast.LENGTH_LONG).show();
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

    static File getApplicationPhotoDirectory() {
        File externalDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File (externalDirectory, "App");
    }
}

