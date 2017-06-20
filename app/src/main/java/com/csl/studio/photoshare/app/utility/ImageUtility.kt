package com.csl.studio.photoshare.app.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

object ImageUtility {

    @JvmOverloads fun decodeBitmapFromFile(path: String, req_width: Int = 0, req_height: Int = 0): Bitmap {

        val options = getBitmapOptions(path)
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = calculateInSampleSize(options, req_width, req_height)
        options.inJustDecodeBounds = false

        val dest_bitmap = BitmapFactory.decodeFile(path, options)

        Log.d("ImageUtility", "Small Image Width: " + options.outWidth)
        Log.d("ImageUtility", "Small Image Height: " + options.outHeight)

        return dest_bitmap
    }

    private fun getBitmapOptions(path: String): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        return options
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, req_width: Int, req_height: Int): Int {

        Log.d("ImageUtility", "Source Image Width: " + options.outWidth)
        Log.d("ImageUtility", "Source Image Height: " + options.outHeight)

        if (req_height <= 0 || req_width <= 0) {
            return 1
        }

        val height = options.outHeight
        val width = options.outWidth

        val width_sample_size = width / req_width
        val height_sample_size = height / req_height

        var in_sample_size = Math.min(width_sample_size, height_sample_size)
        in_sample_size = if (in_sample_size > 0) in_sample_size else 1

        Log.d("ImageUtility", "Sample Rate: " + in_sample_size)

        return in_sample_size
    }

}
