package com.csl.studio.photoshare.app.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.csl.studio.photoshare.app.BuildConfig
import com.csl.studio.photoshare.app.R
import com.csl.studio.photoshare.app.model.PhotoAttribute
import com.csl.studio.photoshare.app.model.PostItem
import com.csl.studio.photoshare.app.utility.FileUtility
import com.csl.studio.photoshare.app.utility.ImageUtility
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility
import com.csl.studio.photoshare.app.utility.firebase.storage.StorageUtility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_take_photo.*
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class UploadPostActivity : BaseActivity() {

    private inner class TakePhotoListener internal constructor(private val request_code: Int, img_name: String) : View.OnClickListener {

        private var img_name = ""

        init {
            this.img_name = img_name
        }

        override fun onClick(view: View) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this@UploadPostActivity,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@UploadPostActivity,
                            Manifest.permission.CAMERA)) {

                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(this@UploadPostActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                MY_PERMISSIONS_REQUEST_READ_CAMERA)
                    }
                } else {
                    ActivityCompat.requestPermissions(this@UploadPostActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            MY_PERMISSIONS_REQUEST_READ_CAMERA)

                    takePhotoAction()
                }

            } else {
                takePhotoAction()
            }
        }

        internal fun takePhotoAction() {

            val take_photo_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            take_photo_intent.resolveActivity(packageManager)?.let {
                startActivityForResult(take_photo_intent, TAKE_PHOTO_CODE)
            }

            val photo_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            photo_intent.resolveActivity(packageManager)?.let {

                try {
                    val dir_name = BuildConfig.APPLICATION_ID
                    val img_file = File(FileUtility.getPublicPictureDir(dir_name), img_name)

                    _photo_path = img_file.absolutePath

                    Log.d(javaClass.name, "Image Path: " + _photo_path)

                    photo_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img_file))
                    startActivityForResult(photo_intent, this.request_code)
                } catch (e: Exception) {
                    Log.d(javaClass.name, "Take Photo Error:\n" + e.toString())
                }
            }

        }
    }

    private inner class ChooseGalleryPhotoListener : View.OnClickListener {

        override fun onClick(view: View) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this@UploadPostActivity,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@UploadPostActivity,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions(this@UploadPostActivity,
                                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                    }
                } else {
                    ActivityCompat.requestPermissions(this@UploadPostActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                    startChooseGallery()
                }

            } else {
                startChooseGallery()
            }

        }

        internal fun startChooseGallery() {
            val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, CHOOSE_GALLERY_CODE)
        }

    }

    internal var _photo_path: String = ""
    internal var _photo_thumbnail_path: String = ""

    private var _storage_ref = FirebaseStorage.getInstance().reference
    private var _database_ref = FirebaseDatabase.getInstance()
    private var _upload_count = 0
    private var _upload_success_count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_photo)

        choose_gallery_photo_btn.setOnClickListener(ChooseGalleryPhotoListener())
        take_photo_camera_btn.setOnClickListener(TakePhotoListener(TAKE_PHOTO_CODE, PHOTO_NAME))

        val user = FirebaseAuth.getInstance().currentUser
        post_user_view.text = user?.email

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.navigation_take_photo_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        val id = item.itemId
        if (R.id.nav_action_post == id) {
            submitPost()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (TAKE_PHOTO_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            // Show Progress View
            val img_file = File(_photo_path)
            if (img_file.exists()) {
                val origin_bmp = ImageUtility.decodeBitmapFromFile(_photo_path)
                photo_view.setImageBitmap(origin_bmp)

                convertToThumbnail(origin_bmp, 200)
            }
        } else if (CHOOSE_GALLERY_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            val image_uri = data.data

            Log.d(TAG, "Image URI: " + image_uri)

            val file_path_column = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(image_uri, file_path_column, null, null, null)

            if (null != cursor) {
                cursor.moveToFirst()
                val column_index = cursor.getColumnIndex(file_path_column[0])
                _photo_path = cursor.getString(column_index)
                Log.d(TAG, "Image Path" + _photo_path)

                val bitmap = BitmapFactory.decodeFile(_photo_path)
                photo_view.setImageBitmap(bitmap)
                convertToThumbnail(bitmap, 200)
                cursor.close()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CAMERA) {
            Log.d(TAG, "Receive MY_PERMISSIONS_REQUEST_READ_CAMERA")
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            Log.d(TAG, "Receive MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE")
        }
    }

    private fun convertToThumbnail(origin_bmp: Bitmap, max_size: Int) {

        val ratio = origin_bmp.width / origin_bmp.height.toFloat()

        val new_width: Int
        val new_height: Int

        if (origin_bmp.width >= origin_bmp.height) {
            new_width = max_size
            new_height = Math.round(new_width / ratio)
        } else if (origin_bmp.height > origin_bmp.width) {
            new_height = max_size
            new_width = Math.round(new_height * ratio)
        } else {
            new_width = origin_bmp.width
            new_height = origin_bmp.height
        }

        val resize_bmp = Bitmap.createScaledBitmap(origin_bmp, new_width, new_height, true)
        Log.d(javaClass.name, "Resize Width: " + resize_bmp.width + ", Height: " + resize_bmp.height)

        var out: FileOutputStream? = null
        try {
            val dir_name = BuildConfig.APPLICATION_ID
            _photo_thumbnail_path = FileUtility.getPublicPictureDir(dir_name).absolutePath + "/" + PHOTO_RESIZE_NAME
            out = FileOutputStream(_photo_thumbnail_path)
            resize_bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (out != null) {
                    out.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        Log.d(javaClass.name, "Resize Photo Path: " + _photo_thumbnail_path)
    }

    private fun submitPost() {

        val user = FirebaseAuth.getInstance().currentUser

        if (null == user) {
            Toast.makeText(this, "Please Sign", Toast.LENGTH_SHORT).show()
            return
        }

        if (_photo_path.isEmpty()) {
            Toast.makeText(this, "No shared photo", Toast.LENGTH_SHORT).show()
            return
        }

        if (_photo_thumbnail_path.isEmpty()) {
            Toast.makeText(this, "No thumbnail photo", Toast.LENGTH_SHORT).show()
            return
        }

        try {

            initUploadCount()
            showProgressDialog()

            val post_ref = _database_ref!!.reference
            val key = post_ref.child("posts").push().key
            val photo_name = sha1_file(File(_photo_path))
            val thumbnail_name = sha1_file(File(_photo_thumbnail_path))
            val auth_uid = FirebaseAuth.getInstance().currentUser!!.uid
            val post_map = createPostMap(photo_name, thumbnail_name)
            val post_path = "/" + DatabaseUtility.PATH.POSTS + "/" + key
            val user_posts_path = "/" + DatabaseUtility.PATH.USER_POSTS + "/" + auth_uid + "/" + key
            val image_info_path = "/" + DatabaseUtility.PATH.IMAGE_INFO + "/"

            val update_list = HashMap<String, Any>()
            update_list.put(post_path, post_map)
            update_list.put(user_posts_path, post_map)
            update_list.put(image_info_path + photo_name, createImageInfo(_photo_path))
            update_list.put(image_info_path + thumbnail_name, createImageInfo(_photo_thumbnail_path))

            post_ref.updateChildren(update_list) { databaseError, _ ->
                Log.d(TAG, "Update Post complete")
                uploadComplete(null == databaseError)
            }

            // Upload original image to Firebase Storage
            val image_ref = _storage_ref
                    .child(StorageUtility.PATH.PHOTOS).child(photo_name)

            image_ref.putFile(Uri.fromFile(File(_photo_path)))
                    .addOnCompleteListener { task ->
                        Log.d(TAG, "Update Image Complete")
                        uploadComplete(task.isSuccessful)
                    }

            // Upload thumbnail of image to Firebase Storage
            val thumbnail_ref = _storage_ref.child(StorageUtility.PATH.THUMBNAILS)
                    .child(thumbnail_name)

            thumbnail_ref.putFile(Uri.fromFile(File(_photo_thumbnail_path)))
                    .addOnCompleteListener { task ->
                        Log.d(TAG, "Update Thumbnail Complete")
                        uploadComplete(task.isSuccessful)
                    }

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            Toast.makeText(this, "Upload Post Error!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Exception Error!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun createPostMap(photo_name: String, thumbnail_name: String): Map<String, Any> {
        val post = PostItem()
        post.auth_uid = FirebaseAuth.getInstance().currentUser!!.uid
        post.message = post_content_view.text.toString()
        post.photo = photo_name
        post.thumbnail = thumbnail_name
        post.time = currentTimeString

        return post.toMap()
    }

    private fun createImageInfo(file_path: String): Map<String, Any> {
        val attr = PhotoAttribute()
        attr.ext = file_path.substring(file_path.lastIndexOf(".") + 1)
        attr.upload_time = currentTimeString

        return attr.toMap()
    }

    private fun initUploadCount() {
        _upload_count = 0
        _upload_success_count = 0
    }

    private fun checkUploadCount() {

        if (_upload_count >= UPLOAD_MAX_TASK) {
            if (_upload_count > _upload_success_count) {
                Toast.makeText(this, "Upload Error!", Toast.LENGTH_SHORT).show()
                initUploadCount()
            } else {
                hideProgressDialog()
                finish()
            }
        }
    }

    private fun uploadComplete(is_success: Boolean?) {
        _upload_count++
        if (is_success!!) {
            _upload_success_count++
        }
        checkUploadCount()
    }

    private val currentTimeString: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            return sdf.format(Date())
        }

    @Throws(NoSuchAlgorithmException::class, IOException::class)
    private fun sha1_file(file: File): String {
        val messageDigest = MessageDigest.getInstance("SHA1")

        BufferedInputStream(FileInputStream(file)).use { in_stream ->
            val buffer = ByteArray(1024)

            var read: Int = in_stream.read(buffer)
            while (read != -1) {
                messageDigest.update(buffer, 0, read)
                read = in_stream.read(buffer)
            }
        }

        // Convert the byte to hex format
        Formatter().use { formatter ->
            for (b in messageDigest.digest()) {
                formatter.format("%02x", b)
            }
            return formatter.toString()
        }
    }

    companion object {

        private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 299
        private val MY_PERMISSIONS_REQUEST_READ_CAMERA = 299

        private val TAG = "UploadPostActivity"
        private val PHOTO_EXT = "jpg"
        private val PHOTO_NAME = "photo_name" + "." + PHOTO_EXT
        private val PHOTO_RESIZE_NAME = "photo_resize_name" + "." + PHOTO_EXT
        private val TAKE_PHOTO_CODE = 1
        private val CHOOSE_GALLERY_CODE = 2
        private val UPLOAD_MAX_TASK = 3
    }

}
