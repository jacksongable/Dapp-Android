package com.thedappapp.dapp.app;

import android.graphics.*;
import android.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jackson on 8/1/16.
 */
public class Compressor {

    public static File compress (String toCompress) throws IOException {
        ExifInterface oldExif = new ExifInterface(toCompress);
        String orientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);

        byte[] compressedBytes = rawCompress(toCompress);
        File compressedFile = File.createTempFile("dapp_group_", ".jpg", Camera.getApplicationPhotoDirectory());
        compressedFile.deleteOnExit();

        if (orientation != null) {
            //writes compressed jpeg bytes to file
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(compressedFile));
            bos.write(compressedBytes);
            bos.flush();
            bos.close();

            //copies exif orientation of original, uncompressed jpeg to compressed jpeg.
            ExifInterface newExif = new ExifInterface(compressedFile.getAbsolutePath());
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
            newExif.saveAttributes();
        }
        return compressedFile;
    }

    private static byte[] rawCompress (String path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap uncompressed = decodePath(path);
        uncompressed.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return baos.toByteArray();
    }

    private static Bitmap decodePath(String toDecode) {
        return BitmapFactory.decodeFile(toDecode);
    }

    private Compressor() {}

}
