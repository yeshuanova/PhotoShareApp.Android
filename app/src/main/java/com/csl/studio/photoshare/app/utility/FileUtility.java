package com.csl.studio.photoshare.app.utility;


import android.os.Environment;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtility {

    public static File getPublicPictureDir(String dir_name) throws IOException {

        if (!isExternalStorageWritable()) {
            throw new IOException("External Storage doesn't mounted!");
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + dir_name);

        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new IOException("Make directory failure");
            }
        }
        return file;
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String storage_state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(storage_state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storage_state);
    }

    public static String convertFileToString(File file) throws IOException {

        FileInputStream image_file = new FileInputStream(file);
        byte file_bytes[] = new byte[(int) file.length()];

        final int read_num = image_file.read(file_bytes);
        image_file.close();

        if (read_num < 0) {
            throw new IOException("No file content!");
        }

        return Base64.encodeToString(file_bytes, Base64.DEFAULT);
    }

    public static String convertFileToString(String file_path) throws IOException {
        return convertFileToString(new File(file_path));
    }

}
