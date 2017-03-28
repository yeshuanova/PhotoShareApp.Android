package com.csl.studio.photoshare.app.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageUtility {

    public static Bitmap decodeBitmapFromFile(String path) {
        return decodeBitmapFromFile(path, 0, 0);
    }

    public static Bitmap decodeBitmapFromFile(String path, int req_width, int req_height) {

        BitmapFactory.Options options = getBitmapOptions(path);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = calculateInSampleSize(options, req_width, req_height);
        options.inJustDecodeBounds = false;

        Bitmap dest_bitmap = BitmapFactory.decodeFile(path, options);

        Log.d("ImageUtility", "Small Image Width: " + options.outWidth);
        Log.d("ImageUtility", "Small Image Height: " + options.outHeight);

        return dest_bitmap;
    }

    private static BitmapFactory.Options getBitmapOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int req_width, int req_height) {

        Log.d("ImageUtility", "Source Image Width: " + options.outWidth);
        Log.d("ImageUtility", "Source Image Height: " + options.outHeight);

        if (req_height <= 0 || req_width <= 0) {
            return 1;
        }

        final int height = options.outHeight;
        final int width = options.outWidth;

        final int width_sample_size = width / req_width;
        final int height_sample_size = height / req_height;

        int in_sample_size = Math.min(width_sample_size, height_sample_size);
        in_sample_size = in_sample_size > 0 ? in_sample_size : 1;

        Log.d("ImageUtility", "Sample Rate: " + in_sample_size);

        return in_sample_size;
    }

}
