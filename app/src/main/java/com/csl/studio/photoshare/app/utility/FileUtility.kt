package com.csl.studio.photoshare.app.utility


import android.os.Environment
import android.util.Base64

import java.io.File
import java.io.FileInputStream
import java.io.IOException

object FileUtility {

    @Throws(IOException::class)
    fun getPublicPictureDir(dir_name: String): File {

        if (!isExternalStorageWritable) {
            throw IOException("External Storage doesn't mounted!")
        }

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + dir_name)

        if (!file.isDirectory) {
            if (!file.mkdirs()) {
                throw IOException("Make directory failure")
            }
        }
        return file
    }

    val isExternalStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    val isExternalStorageReadable: Boolean
        get() {
            val storage_state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == storage_state || Environment.MEDIA_MOUNTED_READ_ONLY == storage_state
        }

    @Throws(IOException::class)
    fun convertFileToString(file: File): String {

        val image_file = FileInputStream(file)
        val file_bytes = ByteArray(file.length().toInt())

        val read_num = image_file.read(file_bytes)
        image_file.close()

        if (read_num < 0) {
            throw IOException("No file content!")
        }

        return Base64.encodeToString(file_bytes, Base64.DEFAULT)
    }

    @Throws(IOException::class)
    fun convertFileToString(file_path: String): String {
        return convertFileToString(File(file_path))
    }

}
